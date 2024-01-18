package de.tub.dima.mascara.modifier;

import de.tub.dima.mascara.optimizer.iqMetadata.IQMetadata;
import de.tub.dima.mascara.policies.AccessControlPolicy;
import de.tub.dima.mascara.policies.AttributeMapping;
import de.tub.dima.mascara.policies.AttributeMappings;
import javolution.testing.AssertionException;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelVisitor;
import org.apache.calcite.rel.core.*;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.util.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class CompliantPlanner extends RelVisitor {
    public RelBuilder builder;
    public Map<RelOptTable, AccessControlPolicy> policies;
    public AttributeMappings attributeMappings;
    public AttributeMappings queryAttributes;
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
                this.queryAttributes = new AttributeMappings(scan);
                this.attributeMappings = this.queryAttributes.clone();
//                this.iqMetadata.getProjectedAttributes(this.attributeMappings, table.getQualifiedName(), policy.getPolicyName());
            } else {
                builder.scan(policy.policyName);
                this.queryAttributes = policy.attributeMappings.clone();
                this.attributeMappings = policy.attributeMappings.clone();
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
            this.queryAttributes.update(project);
            if (this.needsModification){
                List<Pair<RexNode, String>> namedProjects = project.getNamedProjects();
                Pair<List<RexNode>, List<String>> compliantProjects = getCompliantProjects(namedProjects);
                builder.project(compliantProjects.left, compliantProjects.right);
            } else {
                builder.push(project);
                this.attributeMappings = this.queryAttributes.clone();
            }
        } else if (node instanceof Aggregate) {
            Aggregate aggregate = (Aggregate) node;
            super.visit(node, ordinal, parent);
            this.queryAttributes.update(aggregate);
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
                this.attributeMappings = this.queryAttributes.clone();
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
            this.queryAttributes = leftPlanner.queryAttributes.combineAttributeMappings(rightPlanner.queryAttributes);
            this.attributeMappings = leftPlanner.attributeMappings.combineAttributeMappings(rightPlanner.attributeMappings);
        }
    }

    private List<RexNode> getCompliantSorts(Sort sort){
        List<RexNode> compliantSorts = new ArrayList<>();
        for (RexNode sortExp : sort.getSortExps()) {
            if (sortExp instanceof RexInputRef){
                AttributeMapping compliantAttribute = this.attributeMappings.getCompliantAttribute((RexInputRef) sortExp);
                if (compliantAttribute != null){
                    compliantSorts.add(compliantAttribute.newRef);
                }
            }
        }
        return compliantSorts;
    }

    private Pair<List<RexInputRef>, List<RelBuilder.AggCall>> getCompliantAggregate(Aggregate aggregate){
        AttributeMappings newAttributeMappings = new AttributeMappings();
        int i = 0;
        int j = 0;
        List<Integer> groupSet = aggregate.getGroupSet().asList();
        List<RexInputRef> compliantGroupSet = new ArrayList<>();
        for (int index : groupSet) {
            AttributeMapping compliantAttribute = this.attributeMappings.getCompliantAttribute(index);
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
            AttributeMapping compliantAttribute;
            for (Integer attr : agg.getArgList()) {
                compliantAttribute = this.attributeMappings.getCompliantAttribute(attr);
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
                AttributeMapping mapping = new AttributeMapping(originalRef, newRef, agg.getName());
                newAttributeMappings.add(mapping);
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
        AttributeMappings newAttributeMappings = new AttributeMappings();
        int j = 0;
        for (int i = 0; i < namedProjects.size(); i++) {
            Pair<RexNode, String> pair = namedProjects.get(i);

            if (pair.left instanceof RexInputRef) {
                RexInputRef project = (RexInputRef) pair.left;
                String name = pair.right;
                AttributeMapping compliantAttribute = this.attributeMappings.getCompliantAttribute(project);
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
                AttributeMapping leftCompliantAttribute = this.attributeMappings.getCompliantAttribute(leftInputRef);
                AttributeMapping rightCompliantAttribute = this.attributeMappings.getCompliantAttribute(rightInputRef);
                if (leftCompliantAttribute == null || rightCompliantAttribute == null){
                    return null;
                } else if (leftCompliantAttribute.isMasked() || rightCompliantAttribute.isMasked()) {
                    throw new RuntimeException("Join predicate on masked attribute: Case not covered yet.");
                } else {
                    return builder.call(condition.getOperator(), leftCompliantAttribute.newRef, rightCompliantAttribute.newRef);
                }
            }

            RexNode literal;
            RexInputRef inputRef;
            boolean literalIsLeft = false;

            if (condition.operands.get(0) instanceof RexInputRef && !(condition.operands.get(1) instanceof RexInputRef)){
                inputRef = (RexInputRef) condition.operands.get(0);
                literal = condition.operands.get(1);
            } else if (!(condition.operands.get(0) instanceof RexInputRef) && condition.operands.get(1) instanceof RexInputRef) {
                literal = condition.operands.get(0);
                inputRef = (RexInputRef) condition.operands.get(1);
                literalIsLeft = true;
            } else {
                throw new AssertionException("Case not implemented yet");
            }

            AttributeMapping compliantAttribute = this.attributeMappings.getCompliantAttribute(inputRef);

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

    public AttributeMappings getQueryAttributes() {
        return queryAttributes;
    }
}


