package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.inverseFunctions.InverseBucketizeAge;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseBucketize;

public class Bucketize extends MaskingFunction {

    public Bucketize() {
        this.name = "BUCKETIZE";
        this.inverseMaskingFunction = new InverseBucketize();
    }

    public static String eval(double value, double bSize) {
        double l = Math.floor((value) / bSize);
        double h = l + 1.0;
        return "[" + (l * bSize) + " - " + (h * bSize) + ")";
    }
}

