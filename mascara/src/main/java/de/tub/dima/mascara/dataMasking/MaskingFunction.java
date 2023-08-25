package de.tub.dima.mascara.dataMasking;

import java.util.List;

public abstract class MaskingFunction {
    public String name;
    public boolean aggregable = false;
    public InverseMaskingFunction inverseMaskingFunction = null;

    public String getName() {
        return name;
    }

    public boolean isAggregable() {
        return aggregable;
    }

    public InverseMaskingFunction getInverseMaskingFunction() {
        return inverseMaskingFunction;
    }
}
