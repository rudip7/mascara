package de.tub.dima.mascara.dataMasking;

import java.util.List;

public abstract class MaskingFunction {
    public String name;
    public boolean aggregable = false;
    public InverseMaskingFunction inverseMaskingFunction = null;

    public boolean parametrizedInverse = false;

    public String getName() {
        return name;
    }

    public boolean isAggregable() {
        return aggregable;
    }

    public boolean hasParametrizedInverse() {
        return parametrizedInverse;
    }

    public InverseMaskingFunction getInverseMaskingFunction() {
        return inverseMaskingFunction;
    }
    public void setInverseMaskingFunction(List<Object> parameters) {}

    @Override
    public MaskingFunction clone(){
        return this;
    }
}
