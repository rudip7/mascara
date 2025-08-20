package de.tub.dima.mascara.dataMasking;

public abstract class TransformationFunction {
    public String name;

    public String getName() {
        return name;
    }

    @Override
    public TransformationFunction clone(){
        return this;
    }
}
