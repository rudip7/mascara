package de.tub.dima.mascara.dataMasking;

import java.util.List;

public abstract class MaskingFunction {
    public String name;

    public String getName() {
        return name;
    }

    @Override
    public MaskingFunction clone(){
        return this;
    }
}
