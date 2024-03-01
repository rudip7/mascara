package de.tub.dima.mascara.modifier;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.Suppression;
import de.tub.dima.mascara.dataMasking.TransformationFunction;
import de.tub.dima.mascara.policies.AccessControlPolicy;
import de.tub.dima.mascara.policies.AttributeMapping;
import de.tub.dima.mascara.policies.AttributeMappings;
import javolution.testing.AssertionException;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelVisitor;
import org.apache.calcite.rel.core.*;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ScalarFunction;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.*;
import org.apache.calcite.sql.validate.SqlUserDefinedFunction;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.Util;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Function;

public class CompliantPlanner extends RelVisitor {
    public RelBuilder builder;
    public Map<RelOptTable, AccessControlPolicy> policies;
    public AttributeMappings attributeMappings;
    public RelNode cardinalityPlan;
//    public AttributeMappings queryAttributes;
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
                this.attributeMappings = new AttributeMappings(scan);
//                this.iqMetadata.getProjectedAttributes(this.attributeMappings, table.getQualifiedName(), policy.getPolicyName());
            } else {
                builder.scan(policy.policyName);
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
            if (this.needsModification){
                Pair<List<RexNode>, List<String>> transformationProjects = checkTransformationRequired(project);
                if (transformationProjects != null){
                    builder.project(transformationProjects.left, transformationProjects.right);
                    RelNode currentPlan = builder.build();
                    builder = RelBuilder.create(frameworkConfig);
                    builder.push(currentPlan);
                }
                List<Pair<RexNode, String>> namedProjects = project.getNamedProjects();
                Pair<List<RexNode>, List<String>> compliantProjects = getCompliantProjects(namedProjects);
                builder.project(compliantProjects.left, compliantProjects.right);
//                this.attributeMappings.update(project.getNamedProjects());
            } else {
                this.attributeMappings.update(project.getNamedProjects());
                builder.push(project);
            }
        } else if (node instanceof Aggregate) {
            Aggregate aggregate = (Aggregate) node;
            super.visit(node, ordinal, parent);
//            this.queryAttributes.update(aggregate);
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
//                this.attributeMappings = this.queryAttributes.clone();
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
//            this.queryAttributes = leftPlanner.queryAttributes.combineAttributeMappings(rightPlanner.queryAttributes);
            this.attributeMappings = leftPlanner.attributeMappings.combineAttributeMappings(rightPlanner.attributeMappings);
        }
    }

    private Pair<List<RexNode>, List<String>> checkTransformationRequired(Project project) {
        List<Pair<RexNode, String>> namedProjects = project.getNamedProjects();
        List<AttributeMapping> mappingsToTransform = new ArrayList<>();

        for (int i = 0; i < namedProjects.size(); i++) {
            Pair<RexNode, String> pair = namedProjects.get(i);
            if (pair.left instanceof RexCall) {
                checkTransformationsRexCall((RexCall) pair.left, mappingsToTransform);
            }
        }
        if (mappingsToTransform.isEmpty()){
            return null;
        }
//        this.attributeMappings.sortMappingsByNewRefIndex();
        List<String> names = new ArrayList<>();
        List<RexNode> newProjects = new ArrayList<>();
        for (int i = 0; i < this.attributeMappings.size(); i++) {
            AttributeMapping mapping = this.attributeMappings.get(i);
            if (! mappingsToTransform.contains(mapping)){
                newProjects.add(mapping.newRef);
                names.add(mapping.getName());
            } else if (mapping.transformationAvailable()) {
                mapping.setOriginalDatatype();
                TransformationFunction transformationFunction = ((Generalization) mapping.getMaskingFunction()).getTransformationFunction();
                ScalarFunction function = ScalarFunctionImpl.create(transformationFunction.getClass(), "eval");
                SqlIdentifier sqlIdentifier = new SqlIdentifier(Arrays.asList(transformationFunction.getName().toLowerCase()), null, SqlParserPos.ZERO, null);
                SqlOperator op = CalciteCatalogReader.toOp(sqlIdentifier, function);
                RexNode call = builder.call(op, mapping.newRef);
                newProjects.add(call);
                names.add(mapping.getName());
            }
        }
        return new Pair<>(newProjects, names);
    }

    private void checkTransformationsRexCall(RexCall call, List<AttributeMapping> mappingsToTransform) {
        for (RexNode operand : call.operands) {
            if (operand instanceof RexInputRef) {
                RexInputRef projected = (RexInputRef) operand;
                AttributeMapping compliantAttribute = this.attributeMappings.getCompliantAttribute(projected);
                if(compliantAttribute.dataTypeChanged() && !mappingsToTransform.contains(compliantAttribute)){
                    mappingsToTransform.add(compliantAttribute);
                }
            } else if (operand instanceof RexCall) {
                checkTransformationsRexCall((RexCall) operand, mappingsToTransform);
            }
        }
    }

    public SqlOperator getFunction(String functionName){
        List<SqlOperator> operatorList = builder.getRexBuilder().getOpTab().getOperatorList();
        for (SqlOperator operator : operatorList) {
            if (operator.getName().equalsIgnoreCase(functionName)){
                return operator;
            }
        }
        return null;
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
        newAttributeMappings.setMaxOriginalRef(attributeMappings.getMaxOriginalRef());
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
                // TODO: Convert to aggregable if necessary -> Generalizations
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
        newAttributeMappings.setMaxOriginalRef(attributeMappings.getMaxOriginalRef());
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
            } else if (pair.left instanceof RexCall) {
                List<AttributeMapping> relevantAttributes = new ArrayList<>();
                Pair<RexNode, List<AttributeMapping>> generalizedProjection = getCompliantGeneralizedProjection((RexCall) pair.left, relevantAttributes);
                if (generalizedProjection != null){
                    compliantProjects.add(generalizedProjection.left);
                    names.add(pair.right);
                    RexInputRef originalRef = new RexInputRef(i, pair.left.getType());
                    RexInputRef newRef = new RexInputRef(j, pair.left.getType());
                    newAttributeMappings.addWithRelevant(new AttributeMapping(originalRef, newRef, pair.right), generalizedProjection.right);
                    j++;
                }
            } else {
                    throw new RuntimeException("Case not supported yet.");
            }
        }
        this.attributeMappings = newAttributeMappings;
        return new Pair<>(compliantProjects, names);
    }

    public Pair<RexNode, List<AttributeMapping>> getCompliantGeneralizedProjection(RexCall call, List<AttributeMapping> relevantAttributes){
        List<RexNode> compliantOperands = new ArrayList<>();
        for (RexNode operand : call.operands) {
            if (operand instanceof RexInputRef) {
                RexInputRef projected = (RexInputRef) operand;
                Pair<AttributeMapping, List<AttributeMapping>> mappingWithRelevants = this.attributeMappings.getCompliantAttributeWithRelevants(projected);
                if (mappingWithRelevants == null){
                    return null;
                }
                compliantOperands.add(mappingWithRelevants.left.newRef);
                relevantAttributes.addAll(mappingWithRelevants.right);
            } else if (operand instanceof RexCall) {
                Pair<RexNode, List<AttributeMapping>> compliantCall = getCompliantGeneralizedProjection((RexCall) operand, relevantAttributes);
                if (compliantCall == null){
                    return null;
                }
                compliantOperands.add(compliantCall.left);
            } else {
                compliantOperands.add(operand);
            }
        }
        RexNode compliantCall = builder.call(call.op, compliantOperands);
        return new Pair<>(compliantCall, relevantAttributes);
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
                }

                if (leftCompliantAttribute.isMasked()){
                    this.attributeMappings.addFilterMapping(leftCompliantAttribute);
                }
                if (rightCompliantAttribute.isMasked()){
                    this.attributeMappings.addFilterMapping(rightCompliantAttribute);
                }

                if (leftCompliantAttribute.dataTypeChanged() && rightCompliantAttribute.dataTypeChanged()) {
                    return null;
                } else if (leftCompliantAttribute.dataTypeChanged() && !rightCompliantAttribute.dataTypeChanged()) {
                    RexCall maskedExpression = leftCompliantAttribute.maskValue(rightCompliantAttribute.newRef, builder);
                    return builder.call(condition.getOperator(), leftCompliantAttribute.newRef, maskedExpression);
                } else if (!leftCompliantAttribute.dataTypeChanged() && rightCompliantAttribute.dataTypeChanged()) {
                    RexCall maskedExpression = rightCompliantAttribute.maskValue(leftCompliantAttribute.newRef, builder);
                    return builder.call(condition.getOperator(), maskedExpression, rightCompliantAttribute.newRef);
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
            if (compliantAttribute.isMasked()){
                this.attributeMappings.addFilterMapping(compliantAttribute);
            }

            if (compliantAttribute == null || (compliantAttribute.isMasked() && compliantAttribute.getMaskingFunction() instanceof Suppression)){
                // Attribute is suppressed
                return null;
            } else if (!compliantAttribute.isMasked() || !(compliantAttribute.getMaskingFunction() instanceof Generalization)) {
                // Attribute is available or masking function is a perturbation (no schema changes)
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

    public AttributeMappings getAttributeMapping() {
        return attributeMappings;
    }
}


