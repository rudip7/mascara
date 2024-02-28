package de.tub.dima.mascara.dataMasking;

import java.util.List;

public abstract class Generalization extends MaskingFunction{
    public boolean aggregable = false;
    public InverseMaskingFunction inverseMaskingFunction = null;

    public AggregableFunction aggregableFunction = null;

    public boolean parametrizedInverse = false;

    public boolean isAggregable() {
        return aggregable;
    }

    public AggregableFunction getAggregableFunction() {
        return aggregableFunction;
    }

    public boolean hasParametrizedInverse() {
        return parametrizedInverse;
    }

    public InverseMaskingFunction getInverseMaskingFunction() {
        return inverseMaskingFunction;
    }
    public void setInverseMaskingFunction(List<Object> parameters) {}
}
