package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.optimizer.iqMetadata.AttributeMetadata;
import de.tub.dima.mascara.optimizer.statistics.TableStatistics;
import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;
import de.tub.dima.mascara.parser.Parser;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.util.Pair;

import java.util.List;


public class AccessControlPolicy {

    public RelRoot logicalPlan;
    public RelOptTable protectedTable;
    public List<String> policyName;
    public List<String> protectedTableName;
    public AttributeMappings attributeMappings;
    public TableStatistics protectedStats;
    public TableStatistics originalStats;



    public AccessControlPolicy(String policySql, List<String> policyName, Parser parser, MaskingFunctionsCatalog maskingFunctionsCatalog) throws Exception {
        this.attributeMappings = new AttributeMappings();
        this.policyName = policyName;
        this.logicalPlan = parser.getLogicalPlan(policySql);

        RelNode rel = this.logicalPlan.rel;
        Project project;
        if (rel instanceof Project) {
            project = (Project) rel;
            rel = project.getInput();
        } else {
            project = null;
        }
        Filter filter;
        if (rel instanceof Filter) {
            filter = (Filter) rel;
            rel = filter.getInput();
        } else {
            filter = null;
        }
        TableScan scan;
        if (rel instanceof TableScan) {
            scan = (TableScan) rel;
        } else {
            scan = null;
        }
        if (scan == null) {
            // Assert that view has only one table... FOR NOW
            throw new Exception("We currently only support policies protecting a single table at the time.");
        }

        this.protectedTable = scan.getTable();
        this.protectedTableName = scan.getTable().getQualifiedName();
        setStatistics();
        indexStats();
        createAttributesMapping(project, maskingFunctionsCatalog);
        parser.reset();
    }

    private void createAttributesMapping(Project project, MaskingFunctionsCatalog maskingFunctionsCatalog) {
        List<Pair<RexNode, String>> projects = project.getNamedProjects();
        for (int i = 0; i < projects.size(); i++) {
            Pair<RexNode, String> namedAttr = projects.get(i);
            RexNode attr = namedAttr.left;
            // RexInputRef -> unmasked attributes
            // RexCall -> masked attributes
            if (attr instanceof RexInputRef){
                RexInputRef inputRef = (RexInputRef) attr;
                RexInputRef newRef = new RexInputRef(i, inputRef.getType());
                this.protectedStats.indexAttribute(newRef.getIndex(), namedAttr.right);
                AttributeMetadata attributeMetadata = new AttributeMetadata(this.protectedTableName, inputRef.getIndex());
                this.attributeMappings.add(new AttributeMapping(attributeMetadata, inputRef, newRef, namedAttr.right));
            } else if (attr instanceof RexCall){
                RexCall maskedAttribute = (RexCall) attr;
                RexInputRef originalRef = null;
                for (RexNode operand : maskedAttribute.operands) {
                    if (operand instanceof RexInputRef){
                        if (originalRef != null) {
                            throw new IllegalArgumentException("Currently we only support masking functions over a single attribute.");
                        }
                        originalRef = (RexInputRef) operand;
                    }
                }
                if (originalRef == null){
                    throw new IllegalArgumentException("A masking function should be apply on an attribute.");
                }
                RexInputRef newRef = new RexInputRef(i, attr.getType());
                MaskingFunction maskingFunction = maskingFunctionsCatalog.getMaskingFunctionByName(maskedAttribute.getOperator().getName());
                this.protectedStats.indexAttribute(newRef.getIndex(), namedAttr.right);
                AttributeMetadata originalAttribute = new AttributeMetadata(this.protectedTableName, originalRef.getIndex());
                AttributeMetadata compliantAttribute = new AttributeMetadata(this.policyName, newRef.getIndex(), maskingFunction);
                this.attributeMappings.add(new AttributeMapping(originalAttribute, originalRef, compliantAttribute, newRef, namedAttr.right, true, maskedAttribute, maskingFunction));
            }
        }
    }

    public boolean applyToTable(RelOptTable baseTable){
        return baseTable.equals(this.protectedTable);
    }

    public List<String> getPolicyName() {
        return policyName;
    }

    public void setStatistics(){
        StatisticsManager statsManager = StatisticsManager.getInstance();
        this.protectedStats = statsManager.getStatistics(policyName);
        this.originalStats = statsManager.getStatistics(protectedTableName);
    }

    public void indexOriginalStats(){
        List<RelDataTypeField> fieldList = this.protectedTable.getRowType().getFieldList();
        for (int i = 0; i < fieldList.size(); i++) {
            RelDataTypeField field = fieldList.get(i);
            this.originalStats.indexAttribute(field.getIndex(), field.getName());
        }
    }

    public void indexProtectedStats(){
        for (int i = 0; i < this.attributeMappings.size(); i++) {
            AttributeMapping mapping = this.attributeMappings.get(i);
            this.protectedStats.indexAttribute(mapping.newRef.getIndex(), mapping.getName());
        }
    }

    public void indexStats(){
        indexOriginalStats();
        indexProtectedStats();
    }
}
