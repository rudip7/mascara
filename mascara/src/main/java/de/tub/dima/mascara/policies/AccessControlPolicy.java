package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.parser.Parser;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class AccessControlPolicy {

    public RelRoot logicalPlan;
    public RelOptTable protectedTable;
    public List<String> name;
    public List<AttributeMapping> attributeMappings;

    public AccessControlPolicy(String policySql, List<String> name, Parser parser, MaskingFunctionsCatalog maskingFunctionsCatalog) throws Exception {
        this.attributeMappings = new ArrayList<>();
        this.name = name;
        this.logicalPlan = parser.getLogicalPlan(policySql);

        RelNode rel = this.logicalPlan.rel;
        Project project;
        if (rel instanceof Project) {
            project = (Project) rel;
            createAttributesMapping(project, maskingFunctionsCatalog);
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
                RexInputRef newRef = new RexInputRef(i, attr.getType());
                this.attributeMappings.add(new AttributeMapping((RexInputRef) attr, newRef, namedAttr.right));
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
                MaskingFunction maskingFunction = maskingFunctionsCatalog.getByName(maskedAttribute.getOperator().getName());
                this.attributeMappings.add(new AttributeMapping(originalRef, newRef, namedAttr.right, true, maskedAttribute, maskingFunction));
            }
        }
    }

    public boolean applyToTable(RelOptTable baseTable){
        return baseTable.equals(this.protectedTable);
    }


}
