package de.tub.dima.mascara.modifier;

import com.google.common.collect.ImmutableList;
import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.policies.AccessControlPolicy;
import de.tub.dima.mascara.policies.AttributeMapping;
import javolution.testing.AssertionException;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelVisitor;
import org.apache.calcite.rel.core.*;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.util.ImmutableBitSet;
import org.apache.calcite.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class CompliantPlanner extends RelVisitor {
    public RelBuilder builder;
    public Map<RelOptTable, AccessControlPolicy> policies;
    public List<AttributeMapping> attributeMappings;
    public FrameworkConfig frameworkConfig;
    public boolean needsModification = true;


    public CompliantPlanner(FrameworkConfig frameworkConfig, Map<RelOptTable, AccessControlPolicy> policies) {
        this.frameworkConfig = frameworkConfig;
        this.builder = RelBuilder.create(frameworkConfig);
        this.attributeMappings = null;
        this.policies = policies;
    }

    @Override
    public void visit(RelNode node, int ordinal, @Nullable RelNode parent) {
        if (node instanceof TableScan){
            TableScan scan = (TableScan) node;
            RelOptTable table = scan.getTable();
            AccessControlPolicy policy = policies.get(table);
            if (policy == null){
//                builder.scan(node.getTable().getQualifiedName());
                builder.push(scan);
                this.needsModification = false;
                createAttributesMapping(scan);
            } else {
                builder.scan(policy.name);
                attributeMappings = policy.attributeMappings;
            }
        } else if (node instanceof Filter) {
            Filter filter = (Filter) node;
            super.visit(node, ordinal, parent);
            if (this.needsModification){
                RexNode condition = filter.getCondition();
                RexNode compliantCondition = getCompliantCondition((RexCall) condition);
                builder.filter(compliantCondition);
            } else {
                builder.push(filter);
            }
        } else if (node instanceof Project) {
            Project project = (Project) node;
            super.visit(node, ordinal, parent);
            if (this.needsModification){
                List<Pair<RexNode, String>> namedProjects = project.getNamedProjects();
                Pair<List<RexNode>, List<String>> compliantProjects = getCompliantProjects(namedProjects);
                builder.project(compliantProjects.left, compliantProjects.right);
            } else {
                builder.push(project);
                updateAttributesMapping(project);
            }
        } else if (node instanceof Aggregate) {
            Aggregate aggregate = (Aggregate) node;
            super.visit(node, ordinal, parent);
            if (this.needsModification){
                Pair<List<RexInputRef>, List<RelBuilder.AggCall>> compliantAggregate =
                        getCompliantAggregate(aggregate);
                List<RexInputRef> compliantGroupSet = compliantAggregate.left;
                List<RelBuilder.AggCall> compliantAggCalls = compliantAggregate.right;

                if (!compliantAggCalls.isEmpty()){
                    RelBuilder.GroupKey groupKey = builder.groupKey(compliantGroupSet);
                    builder.aggregate(groupKey, compliantAggCalls);
                } else {
                    // No aggregate is available. We have to transform the operator to a Projection
                    builder.project(compliantGroupSet);
                }
            } else {
                builder.push(aggregate);
                updateAttributesMapping(aggregate);
            }
        } else if (node instanceof Sort) {
            Sort sort = (Sort) node;
            super.visit(node, ordinal, parent);
            if (this.needsModification){
                List<RexNode> compliantSorts = getCompliantSorts(sort);
                if (!compliantSorts.isEmpty()){
                    builder.sort(compliantSorts);
                }
            } else {
                builder.push(sort);
            }
        } else if (node instanceof Join) {
            Join join = (Join) node;
            CompliantPlanner leftPlanner = new CompliantPlanner(this.frameworkConfig, this.policies);
            leftPlanner.go(join.getLeft());
            CompliantPlanner rightPlanner = new CompliantPlanner(this.frameworkConfig, this.policies);
            rightPlanner.go(join.getRight());

            RelNode leftPlan = leftPlanner.builder.build();
            RelNode rightPlan = rightPlanner.builder.build();
            builder.push(leftPlan);
            builder.push(rightPlan);
            builder.join(join.getJoinType(), join.getCondition());

            needsModification = leftPlanner.needsModification || rightPlanner.needsModification;
            combineAttributeMappings(leftPlanner, rightPlanner);
        }
    }

    private void combineAttributeMappings(CompliantPlanner leftPlanner, CompliantPlanner rightPlanner){
        this.attributeMappings = new ArrayList<>();
        int max = -1;
        for (AttributeMapping attr : leftPlanner.attributeMappings) {
            if (attr.newRef.getIndex() > max){
                max = attr.newRef.getIndex();
            }
            this.attributeMappings.add(attr);
        }
        max++;
        for (AttributeMapping attr : rightPlanner.attributeMappings) {
            this.attributeMappings.add(attr.increaseIdx(max));
        }
    }

    private void createAttributesMapping(TableScan scan) {
        this.attributeMappings = new ArrayList<>();
        List<RelDataTypeField> fieldList = scan.deriveRowType().getFieldList();
        List<String> fieldNames = scan.deriveRowType().getFieldNames();
        for (int i = 0; i < fieldList.size(); i++) {
            RexInputRef inputRef = new RexInputRef(i, fieldList.get(i).getType());
            attributeMappings.add(new AttributeMapping(inputRef, fieldNames.get(i)));
        }
    }

    private void updateAttributesMapping(Project project){
        List<AttributeMapping> newAttributeMappings = new ArrayList<>();
        List<Pair<RexNode, String>> namedProjects = project.getNamedProjects();
        for (int i = 0; i < namedProjects.size(); i++) {
            Pair<RexNode, String> namedProject = namedProjects.get(i);
            if (namedProject.left instanceof RexInputRef) {
                RexInputRef inputRef = new RexInputRef(i, namedProject.left.getType());
                newAttributeMappings.add(new AttributeMapping(inputRef,namedProject.right));
            } else {
                throw new RuntimeException("Queries with generalized projection are not supported yet.");
            }
        }
        this.attributeMappings = newAttributeMappings;
    }

    private void updateAttributesMapping(Aggregate aggregate){
        List<AttributeMapping> newAttributeMappings = new ArrayList<>();
        int i = 0;
        List<Integer> groupSet = aggregate.getGroupSet().asList();

        for (int index : groupSet) {
            AttributeMapping compliantAttribute = getCompliantAttribute(index);
            newAttributeMappings.add(compliantAttribute.project(i, i));
            i++;
        }
        List<AggregateCall> aggCalls = aggregate.getAggCallList();
        for (AggregateCall agg : aggCalls) {
            RexInputRef inputRef = new RexInputRef(i, agg.getType());
            newAttributeMappings.add(new AttributeMapping(inputRef, agg.getName()));
            i++;
        }
        this.attributeMappings = newAttributeMappings;
    }

    private List<RexNode> getCompliantSorts(Sort sort){
        List<RexNode> compliantSorts = new ArrayList<>();
        for (RexNode sortExp : sort.getSortExps()) {
            if (sortExp instanceof RexInputRef){
                AttributeMapping compliantAttribute = getCompliantAttribute((RexInputRef) sortExp);
                if (compliantAttribute != null){
                    compliantSorts.add(compliantAttribute.newRef);
                }
            }
        }
        return compliantSorts;
    }

    private Pair<List<RexInputRef>, List<RelBuilder.AggCall>> getCompliantAggregate(Aggregate aggregate){
        List<AttributeMapping> newAttributeMappings = new ArrayList<>();
        int i = 0;
        int j = 0;
        List<Integer> groupSet = aggregate.getGroupSet().asList();
        List<RexInputRef> compliantGroupSet = new ArrayList<>();
        for (int index : groupSet) {
            AttributeMapping compliantAttribute = getCompliantAttribute(index);
            if (compliantAttribute != null){
                compliantGroupSet.add(compliantAttribute.newRef);
                newAttributeMappings.add(compliantAttribute.project(i, j));
                j++;
            }
            i++;
        }

        List<AggregateCall> aggCalls = aggregate.getAggCallList();
        List<RelBuilder.AggCall> compliantAggCalls = new ArrayList<>();
        for (AggregateCall agg : aggCalls) {
            List<RexNode> operands = new ArrayList<>();
            for (Integer attr : agg.getArgList()) {
                AttributeMapping compliantAttribute = getCompliantAttribute(attr);
                if (compliantAttribute != null && compliantAttribute.isAggregable()){
                    operands.add(compliantAttribute.newRef);
                } else {
                    break;
                }
            }
            // Case when all attributes are aggregable
            if (operands.size() == agg.getArgList().size()){
                RelBuilder.AggCall aggCall = builder.aggregateCall(agg.getAggregation(), operands);
                compliantAggCalls.add(aggCall.as(agg.getName()));

                RexInputRef originalRef = new RexInputRef(i, agg.getType());
                RexInputRef newRef = new RexInputRef(j, agg.getType());
                newAttributeMappings.add(new AttributeMapping(originalRef, newRef, agg.getName()));
                j++;
            }
            i++;
        }
        this.attributeMappings = newAttributeMappings;
        return new Pair<>(compliantGroupSet, compliantAggCalls);
    }

    private Pair<List<RexNode>, List<String>> getCompliantProjects(List<Pair<RexNode, String>> namedProjects){
        List<String> names = new ArrayList<>();
        List<RexNode> compliantProjects = new ArrayList<>();
        List<AttributeMapping> newAttributeMappings = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < namedProjects.size(); i++) {
            Pair<RexNode, String> pair = namedProjects.get(i);

            if (pair.left instanceof RexInputRef) {
                RexInputRef project = (RexInputRef) pair.left;
                String name = pair.right;
                AttributeMapping compliantAttribute = getCompliantAttribute(project);
                if (compliantAttribute != null) {
                    compliantProjects.add(compliantAttribute.newRef);
                    names.add(name);
                    newAttributeMappings.add(compliantAttribute.project(i, j, name));
                    j++;
                }
            } else {
                throw new RuntimeException("Generalized Projections not supported yet.");
            }
        }
        this.attributeMappings = newAttributeMappings;
        return new Pair<>(compliantProjects, names);
    }

    private RexNode getCompliantCondition(RexCall condition){
        if (condition.op.kind == SqlKind.AND || condition.op.kind == SqlKind.OR){
            List<RexNode> compliantOperands = new ArrayList<>();
            for (RexNode operand : condition.operands) {
                RexNode compliantCondition = getCompliantCondition((RexCall) operand);
                if (compliantCondition != null){
                    compliantOperands.add(compliantCondition);
                }
            }
            if (compliantOperands.isEmpty()){
                // There are no complaint rewrites for the given condition
                return null;
            } else if (compliantOperands.size() == 1) {
                // Only one operand is compliant
                return compliantOperands.get(0);
            }
            return builder.call(condition.op, compliantOperands);
        } else if (condition.op.getKind().equals(SqlKind.EQUALS) || condition.op.getKind().equals(SqlKind.NOT_EQUALS)
                || condition.op.getKind().equals(SqlKind.GREATER_THAN) || condition.op.getKind().equals(SqlKind.LESS_THAN)
                || condition.op.getKind().equals(SqlKind.GREATER_THAN_OR_EQUAL) || condition.op.getKind().equals(SqlKind.LESS_THAN_OR_EQUAL)) {
            // For now make the assumption of simple conditions with literals
            assert condition.operands.size() == 2;

            // Join predicate.
            if (condition.operands.get(0) instanceof RexInputRef && condition.operands.get(1) instanceof RexInputRef){
                RexInputRef leftInputRef = (RexInputRef) condition.operands.get(0);
                RexInputRef rightInputRef = (RexInputRef) condition.operands.get(1);
                AttributeMapping leftCompliantAttribute = getCompliantAttribute(leftInputRef);
                AttributeMapping rightCompliantAttribute = getCompliantAttribute(rightInputRef);
                if (leftCompliantAttribute == null || rightCompliantAttribute == null){
                    return null;
                } else {
                    return builder.call(condition.getOperator(), leftCompliantAttribute.newRef, rightCompliantAttribute.newRef);
                }
            }

            RexLiteral literal;
            RexInputRef inputRef;
            boolean literalIsLeft = false;

            if (condition.operands.get(0) instanceof RexInputRef && condition.operands.get(1) instanceof RexLiteral){
                inputRef = (RexInputRef) condition.operands.get(0);
                literal = (RexLiteral) condition.operands.get(1);
            } else if (condition.operands.get(0) instanceof RexLiteral && condition.operands.get(1) instanceof RexInputRef) {
                literal = (RexLiteral) condition.operands.get(0);
                inputRef = (RexInputRef) condition.operands.get(1);
                literalIsLeft = true;
            } else {
                throw new AssertionException("Case not implemented yet");
            }

            AttributeMapping compliantAttribute = getCompliantAttribute(inputRef);

            if (compliantAttribute == null){
                // Attribute is suppressed
                return null;
            } else if (!compliantAttribute.isMasked()) {
                // Attribute is available
                // No modification is needed
                if (literalIsLeft) {
                    return builder.call(condition.getOperator(), literal, compliantAttribute.newRef);
                } else {
                    return builder.call(condition.getOperator(), compliantAttribute.newRef, literal);
                }

            } else {
                // Attribute is available but masked
                RexCall maskedLiteral = compliantAttribute.maskValue(literal, builder);
                if (literalIsLeft) {
                    return builder.call(condition.getOperator(), maskedLiteral, compliantAttribute.newRef);
                } else {
                    return builder.call(condition.getOperator(), compliantAttribute.newRef, maskedLiteral);
                }
            }
        }
        return null;
    }

    public AttributeMapping getCompliantAttribute(RexInputRef required){
        for (AttributeMapping mapping:
                this.attributeMappings) {
            if (mapping.originalRef.equals(required)) {
                return mapping;
            }
        }
        return null;
    }

    public AttributeMapping getCompliantAttribute(int requiredIndex){
        for (AttributeMapping mapping:
                this.attributeMappings) {
            if (mapping.originalRef.getIndex() == requiredIndex) {
                return mapping;
            }
        }
        return null;
    }

}



//    private void connectToParent(RelNode originalChild, RelNode newChild, RelNode parent){
//        Project p = (Project) parent;
//        parent.set
//
//    }

