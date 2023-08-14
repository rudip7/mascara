package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.optimizer.iqMetadata.AttributeMetadata;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilder;

import java.util.ArrayList;
import java.util.List;

public class AttributeMapping {
    public RexInputRef originalRef;
    public RexInputRef newRef;
    public String name;
    public AttributeMetadata originalAttribute;
    public AttributeMetadata compliantAttribute;
    public boolean masked;
    public RexCall maskedAttribute;
    public MaskingFunction maskingFunction;
    public RexCall aggregate = null;

    public AttributeMapping(RexInputRef originalRef, RexInputRef newRef, String name, boolean masked, RexCall maskedAttribute, MaskingFunction maskingFunction) {
        if (masked && maskedAttribute == null){
            throw new IllegalArgumentException("If the attribute is masked, then provide also the maskedAttribute RexCall.");
        }
        this.originalRef = originalRef;
        this.newRef = newRef;
        this.name = name;
        this.masked = masked;
        this.maskedAttribute = maskedAttribute;
        this.maskingFunction = maskingFunction;
    }

    public AttributeMapping(RexInputRef originalRef, RexInputRef newRef, String name, boolean masked, RexCall maskedAttribute, MaskingFunction maskingFunction) {
        if (masked && maskedAttribute == null){
            throw new IllegalArgumentException("If the attribute is masked, then provide also the maskedAttribute RexCall.");
        }
        this.originalRef = originalRef;
        this.newRef = newRef;
        this.name = name;
        this.masked = masked;
        this.maskedAttribute = maskedAttribute;
        this.maskingFunction = maskingFunction;
    }

    public AttributeMapping(RexInputRef originalRef, RexInputRef newRef, String name) {
        this(originalRef, newRef, name,false, null, null);
    }

    public AttributeMapping(RexInputRef inputRef, String name) {
        this(inputRef, inputRef, name,false, null, null);
    }
    public boolean isMasked() {
        return masked;
    }
    public RexCall maskValue(RexLiteral literal, RelBuilder builder){
        if (!masked){
            return null;
        }
        List<RexNode> operands = new ArrayList<>();
        for (RexNode operand : maskedAttribute.operands) {
            if (operand instanceof RexInputRef){
                operands.add(literal);
            } else {
                operands.add(operand);
            }
        }
        return (RexCall) builder.call(maskedAttribute.op, operands);
    }

    public RelDataType getType(){
        if (masked){
            return maskedAttribute.getType();
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
        return new AttributeMapping(originalRef, newRef, name, this.masked, this.maskedAttribute, this.maskingFunction);
    }
    public AttributeMapping increaseIdx(int idxIncrease){
        RexInputRef originalRef = new RexInputRef(this.originalRef.getIndex() + idxIncrease, this.originalRef.getType());
        RexInputRef newRef = new RexInputRef(this.newRef.getIndex() + idxIncrease, this.getType());
        return new AttributeMapping(originalRef, newRef, name, this.masked, this.maskedAttribute, this.maskingFunction);
    }
    public boolean isAggregable(){
        return !this.isMasked() || this.maskingFunction.aggregable;
    }

    public void setAggregate(RexCall aggregate) {
        this.aggregate = aggregate;
    }
}
