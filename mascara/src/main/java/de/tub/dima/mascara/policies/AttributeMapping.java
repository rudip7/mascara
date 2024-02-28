package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.Perturbation;
import de.tub.dima.mascara.optimizer.iqMetadata.AttributeMetadata;
import de.tub.dima.mascara.optimizer.statistics.AttributeStatistics;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilder;

import java.util.ArrayList;
import java.util.List;

public class AttributeMapping {
    public AttributeMetadata originalAttribute;
    public AttributeMetadata compliantAttribute;
    public RexInputRef originalRef;
    public RexInputRef newRef;
    public String name;
    public boolean masked;
    public RexCall maskedRexCall;
    public MaskingFunction maskingFunction;
    public boolean aggregate = false;

    public AttributeMapping(AttributeMetadata originalAttribute, RexInputRef originalRef, AttributeMetadata compliantAttribute, RexInputRef newRef, String name, boolean masked, RexCall maskedRexCall, MaskingFunction maskingFunction) {
        if (masked && (maskedRexCall == null || compliantAttribute == null)){
            throw new IllegalArgumentException("If the attribute is masked, then provide also the compliantAttribute AttributeMetadata and the masking RexCall.");
        }
        this.originalAttribute = originalAttribute;
        this.compliantAttribute = compliantAttribute;
        this.originalRef = originalRef;
        this.newRef = newRef;
        this.name = name;
        this.masked = masked;
        this.maskedRexCall = maskedRexCall;
        this.maskingFunction = maskingFunction;

    }

    public AttributeMapping(AttributeMetadata originalAttribute, RexInputRef originalRef, RexInputRef newRef, String name) {
        this(originalAttribute, originalRef, null, newRef, name,false, null, null);
    }

    public AttributeMapping(AttributeMetadata originalAttribute, RexInputRef inputRef, String name) {
        this(originalAttribute, inputRef, inputRef, name);
    }

    public AttributeMapping(RexInputRef originalRef, RexInputRef newRef, String name) {
        this(null, originalRef, null, newRef, name, false, null, null);
        this.aggregate = true;
    }

    /**
     * Creates an attribute mapping for an aggregate over an unmasked attribute.
     *
     * @param inputRef
     * @param name
     */
    public AttributeMapping(RexInputRef inputRef, String name) {
        this(null, inputRef, null, inputRef, name, false, null, null);
        this.aggregate = true;
    }
    public boolean isMasked() {
        return masked;
    }

    public boolean dataTypeChanged() {
        return masked && (maskingFunction != null && maskingFunction instanceof Generalization);
    }

    public RexCall maskValue(RexNode literal, RelBuilder builder){
        if (!masked){
            return null;
        }
        List<RexNode> operands = new ArrayList<>();
        for (RexNode operand : maskedRexCall.operands) {
            if (operand instanceof RexInputRef){
                operands.add(literal);
            } else {
                operands.add(operand);
            }
        }
        return (RexCall) builder.call(maskedRexCall.op, operands);
    }

    public RelDataType getType(){
        if (masked){
            return maskedRexCall.getType();
        }
        return originalRef.getType();
    }

    public String getName() {
        return name;
    }

    public AttributeMapping project(int originalIdx, int newIdx){
        return project(originalIdx, newIdx, this.name);
    }

    public AttributeMapping project(int originalIdx, int newIdx, String name){
        RexInputRef originalRef = new RexInputRef(originalIdx, this.originalRef.getType());
        RexInputRef newRef = new RexInputRef(newIdx, this.getType());
        return new AttributeMapping(this.originalAttribute, originalRef, this.compliantAttribute, newRef, name, this.masked, this.maskedRexCall, this.maskingFunction);
    }
    public AttributeMapping increaseIdx(int originalIdxIncrease, int newIdxIncrease){
        RexInputRef originalRef = new RexInputRef(this.originalRef.getIndex() + originalIdxIncrease, this.originalRef.getType());
        RexInputRef newRef = new RexInputRef(this.newRef.getIndex() + newIdxIncrease, this.getType());
        return new AttributeMapping(this.originalAttribute, originalRef, this.compliantAttribute, newRef, this.name, this.masked, this.maskedRexCall, this.maskingFunction);
    }
    public boolean isAggregable(){
        return !this.isMasked() || this.maskingFunction instanceof Perturbation || (this.maskingFunction instanceof Generalization && ((Generalization) this.maskingFunction).isAggregable());
    }

    public void setAggregate() {
        this.aggregate = true;
    }

    public void setGrouping() {
        if (originalAttribute != null){
            originalAttribute.setGrouping();
        }
        if (compliantAttribute != null){
            compliantAttribute.setGrouping();
        }
    }

    public AttributeStatistics getOriginalStats() {
        return originalAttribute.getStats();
    }

    public AttributeStatistics getCompliantStats() {
        return compliantAttribute == null ? null : compliantAttribute.getStats();
    }

    public MaskingFunction getMaskingFunction() {
        return maskingFunction;
    }



    @Override
    public AttributeMapping clone() {
        AttributeMapping clonedMapping = new AttributeMapping(
                this.originalAttribute,
                new RexInputRef(this.originalRef.getIndex(), this.originalRef.getType()),
                this.compliantAttribute,
                new RexInputRef(this.newRef.getIndex(), this.newRef.getType()),
                this.name,
                this.masked,
                this.maskedRexCall,
                this.maskingFunction
        );
        clonedMapping.aggregate = this.aggregate;

        return clonedMapping;
    }
}
