package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.optimizer.iqMetadata.AttributeMetadata;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class AttributeMappings {

    private List<AttributeMapping> mappings;

    public AttributeMappings() {
        this.mappings = new ArrayList<>();
    }

    public AttributeMappings(TableScan scan) {
        this.mappings = new ArrayList<>();
        List<RelDataTypeField> fieldList = scan.deriveRowType().getFieldList();
        List<String> fieldNames = scan.deriveRowType().getFieldNames();
        for (int i = 0; i < fieldList.size(); i++) {
            RexInputRef inputRef = new RexInputRef(i, fieldList.get(i).getType());
            AttributeMetadata attributeMetadata = new AttributeMetadata(scan.getTable().getQualifiedName(), i, fieldList.get(i).getName());
            this.mappings.add(new AttributeMapping(attributeMetadata, inputRef, fieldNames.get(i)));
        }
    }

    public void update(Project project){
        List<AttributeMapping> newAttributeMappings = new ArrayList<>();
        List<Pair<RexNode, String>> namedProjects = project.getNamedProjects();
        int newIdx = 0;
        for (int i = 0; i < namedProjects.size(); i++) {
            Pair<RexNode, String> namedProject = namedProjects.get(i);
            if (namedProject.left instanceof RexInputRef) {
                AttributeMapping attributeMapping = getCompliantAttribute((RexInputRef) namedProject.left);
                newAttributeMappings.add(attributeMapping.project(i, newIdx, namedProject.right));
                newIdx++;
            } else {
                throw new RuntimeException("Queries with generalized projection are not supported yet.");
            }
        }
        this.mappings = newAttributeMappings;
    }

    public void update(Aggregate aggregate){
        List<AttributeMapping> newAttributeMappings = new ArrayList<>();
        int i = 0;
        List<Integer> groupSet = aggregate.getGroupSet().asList();

        for (int index : groupSet) {
            AttributeMapping compliantAttribute = getCompliantAttribute(index);
            compliantAttribute.setGrouping();
            newAttributeMappings.add(compliantAttribute.project(i, i));
            i++;
        }
        List<AggregateCall> aggCalls = aggregate.getAggCallList();
        for (AggregateCall agg : aggCalls) {
            RexInputRef inputRef = new RexInputRef(i, agg.getType());
            newAttributeMappings.add(new AttributeMapping(inputRef, agg.getName()));
//            newAttributeMappings.add(new AttributeMapping(inputRef, agg.getName(), agg));
            i++;
        }
        this.mappings = newAttributeMappings;
    }


    public int size(){
        return this.mappings.size();
    }

    public boolean add(AttributeMapping mapping){
        return this.mappings.add(mapping);
    }

    public AttributeMapping get(int index){
        return this.mappings.get(index);
    }

    public AttributeMapping getCompliantAttribute(RexInputRef required){
        for (AttributeMapping mapping:
                this.mappings) {
            if (mapping.originalRef.equals(required)) {
                return mapping;
            }
        }
        return null;
    }

    public AttributeMapping getCompliantAttribute(int requiredIndex){
        for (AttributeMapping mapping:
                this.mappings) {
            if (mapping.originalRef.getIndex() == requiredIndex) {
                return mapping;
            }
        }
        return null;
    }

    public AttributeMappings combineAttributeMappings(AttributeMappings other){
        AttributeMappings combinedMappings = new AttributeMappings();
        int max = -1;
        for (AttributeMapping attr : this.mappings) {
            if (attr.newRef.getIndex() > max){
                max = attr.newRef.getIndex();
            }
            combinedMappings.add(attr);
        }
        max++;
        for (AttributeMapping attr : other.mappings) {
            combinedMappings.add(attr.increaseIdx(max));
        }
        return combinedMappings;
    }

    @Override
    public AttributeMappings clone() {
        AttributeMappings clonedMappings = new AttributeMappings();
        for (AttributeMapping mapping : this.mappings) {
            clonedMappings.add(mapping.clone());
        }
        return clonedMappings;
    }
}
