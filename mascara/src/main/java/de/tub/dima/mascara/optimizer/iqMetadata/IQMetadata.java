package de.tub.dima.mascara.optimizer.iqMetadata;

import de.tub.dima.mascara.policies.AttributeMapping;
import de.tub.dima.mascara.policies.AttributeMappings;

import java.util.ArrayList;
import java.util.List;

public class IQMetadata {
    public List<AttributeMetadata> projectedAttributes;
    public boolean aggregate;
    public List<AttributeMetadata> groupingAttributes;
    public List<AttributeMetadata> aggregates;
    public float selectivity;
    public float totalNRows;
    private List<AttributeMetadata> keepAttributes;

    public IQMetadata() {
        this.projectedAttributes = new ArrayList<>();
        this.aggregate = false;
        this.groupingAttributes = null;
        this.aggregates = null;
        this.selectivity = -1;
        this.totalNRows = -1;
        this.markedAttributes = null;
    }

    public void getProjectedAttributes(AttributeMappings mappings, List<String> tableName, List<String> policyName){
        for (int i = 0; i < mappings.size(); i++) {
            AttributeMapping mapping = mappings.get(i);
            if (mapping.isMasked()) {
                projectedAttributes.add(new AttributeMetadata(tableName, mapping.originalRef.getIndex(), policyName, mapping.newRef.getIndex()));
            } else {
                projectedAttributes.add(new AttributeMetadata(tableName, mapping.originalRef.getIndex()));
            }
        }
    }

    public void keepAttribute(int compliantIndex){
        if (markedAttributes == null){
            markedAttributes = new ArrayList<>();
        }
    }
}
