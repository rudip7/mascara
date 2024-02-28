package de.tub.dima.mascara.dataMasking;

public abstract class AggregableFunction {
    public String name;

    public String getName() {
        return name;
    }

    @Override
    public AggregableFunction clone(){
        return this;
    }
}
