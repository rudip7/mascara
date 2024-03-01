package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.optimizer.iqMetadata.AttributeMetadata;
import de.tub.dima.mascara.optimizer.statistics.AttributeStatistics;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.core.AggregateCall;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.util.Pair;

import java.util.*;

public class AttributeMappings {

    private List<AttributeMapping> mappings;

    public List<List<AttributeMapping>> relevantAttributes;

    public List<AttributeMapping> filterMappings;

    public int maxOriginalRef;

    public AttributeMappings() {
        this.mappings = new ArrayList<>();
        this.relevantAttributes = new ArrayList<>();
        this.filterMappings = new ArrayList<>();
    }

    public AttributeMappings(TableScan scan) {
        this.mappings = new ArrayList<>();
        List<RelDataTypeField> fieldList = scan.deriveRowType().getFieldList();
        List<String> fieldNames = scan.deriveRowType().getFieldNames();

        for (int i = 0; i < fieldList.size(); i++) {
            RexInputRef inputRef = new RexInputRef(i, fieldList.get(i).getType());
            AttributeMetadata attributeMetadata = new AttributeMetadata(scan.getTable().getQualifiedName(), i, fieldList.get(i).getName());
            AttributeMapping mapping = new AttributeMapping(attributeMetadata, inputRef, fieldNames.get(i));
            this.mappings.add(mapping);
            this.relevantAttributes.add(Arrays.asList(mapping));
        }
        this.maxOriginalRef = fieldList.size()-1;
    }

    public void update(List<Pair<RexNode, String>> namedProjects){
        // TODO: Consider missing attributes as suppressed
        List<AttributeMapping> newAttributeMappings = new ArrayList<>();
        List<List<AttributeMapping>> newRelevantAttributes = new ArrayList<>();
        int newIdx = 0;
        for (int i = 0; i < namedProjects.size(); i++) {
            Pair<RexNode, String> namedProject = namedProjects.get(i);
            if (namedProject.left instanceof RexInputRef) {
                // getCompliantAttribute returns null if the attribute is not available, we should consider this in the future
                AttributeMapping attributeMapping = getCompliantAttribute((RexInputRef) namedProject.left);
                newAttributeMappings.add(attributeMapping.project(i, newIdx, namedProject.right));
                newIdx++;
                newRelevantAttributes.add(relevantAttributes.get(attributeMapping.newRef.getIndex()));

            } else if (namedProject.left instanceof RexCall){
                List<AttributeMapping> callMappings = new ArrayList<>();
                collectMappings((RexCall) namedProject.left, callMappings);
                RexInputRef originalRef = new RexInputRef(i, namedProject.left.getType());
                RexInputRef newRef = new RexInputRef(newIdx, namedProject.left.getType());
                AttributeMapping mapping = new AttributeMapping(originalRef, newRef, namedProject.right);
                newAttributeMappings.add(mapping);
                newRelevantAttributes.add(callMappings);
                newIdx++;
            } else {
                throw new RuntimeException("Case not supported yet.");
            }
        }
        this.mappings = newAttributeMappings;
        this.relevantAttributes = newRelevantAttributes;
    }

    private void collectMappings(RexCall call, List<AttributeMapping> callMappings) {
        for (RexNode operand : call.operands) {
            if (operand instanceof RexInputRef) {
                RexInputRef projected = (RexInputRef) operand;
                AttributeMapping mapping = getCompliantAttribute(projected);
                callMappings.addAll(relevantAttributes.get(mapping.newRef.getIndex()));
            } else if (operand instanceof RexCall) {
                collectMappings((RexCall) operand, callMappings);
            }
        }

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
        this.relevantAttributes.add(Arrays.asList(mapping));
        return this.mappings.add(mapping);
    }

    public boolean addWithRelevant(AttributeMapping mapping, List<AttributeMapping> relevant) {
        this.relevantAttributes.add(relevant);
        return this.mappings.add(mapping);
    }

    public boolean addFilterMapping(AttributeMapping mapping){
        return this.filterMappings.add(mapping);
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

    public Pair<AttributeMapping, List<AttributeMapping>> getCompliantAttributeWithRelevants(RexInputRef required) {
        for (int i = 0; i < this.mappings.size(); i++) {
            AttributeMapping mapping = this.mappings.get(i);
            if (mapping.originalRef.equals(required)) {
                return new Pair<>(mapping, this.relevantAttributes.get(i));
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
            combinedMappings.add(attr.increaseIdx(maxOriginalRef + 1, max));
        }
        combinedMappings.setMaxOriginalRef(maxOriginalRef + other.getMaxOriginalRef() + 1);
        return combinedMappings;
    }

    public void setMaxOriginalRef(int maxOriginalRef) {
        this.maxOriginalRef = maxOriginalRef;
    }

    public int getMaxOriginalRef() {
        return maxOriginalRef;
    }

    public List<AttributeMapping> getRelevantMappings(){
        List<AttributeMapping> relevantMappings = new ArrayList<>();
        for(List<AttributeMapping> mappings : this.relevantAttributes){
            relevantMappings.addAll(mappings);
        }
//        relevantMappings.addAll(this.mappings);
//        for (AttributeMapping mapping : this.filterMappings) {
//            if (stillRelevant(mapping)){
//                relevantMappings.add(mapping);
//            }
//        }
        return relevantMappings;
    }

    public boolean stillRelevant(AttributeMapping mapping){
        AttributeStatistics stats = mapping.getCompliantStats();
        for (AttributeMapping relevantMapping : this.mappings) {
            AttributeStatistics tempStats = relevantMapping.getCompliantStats();
            if (tempStats != null && tempStats.equals(stats)){
                return false;
            }
        }
        return true;
    }


    @Override
    public AttributeMappings clone() {
        AttributeMappings clonedMappings = new AttributeMappings();
        for (AttributeMapping mapping : this.mappings) {
            clonedMappings.add(mapping.clone());
        }

        for (AttributeMapping mapping : this.filterMappings) {
            clonedMappings.addFilterMapping(mapping.clone());
        }
        clonedMappings.setMaxOriginalRef(this.maxOriginalRef);
        return clonedMappings;
    }



}
