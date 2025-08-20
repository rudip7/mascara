package de.tub.dima.mascara.dataMasking.acs;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseBucketize;

import java.util.List;

public class BucketizeLow1 extends Generalization {

    public BucketizeLow1() {
        this.name = "BUCKETIZE_1_LOW";
        this.inverseMaskingFunction = null;
        this.parametrizedInverse = true;
    }

    @Override
    public void setInverseMaskingFunction(List<Object> parameters) {
        if (parameters.size() == 1){
            Double bSize = Double.parseDouble((String) parameters.get(0));
            this.inverseMaskingFunction = new InverseBucketize(bSize);
        }
    }

    public static Double eval(double value, double bSize) {
        double l = Math.floor((value) / bSize);
        return Math.floor((value) / bSize);
    }
}

