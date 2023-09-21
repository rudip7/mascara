package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.inverseFunctions.InverseBucketizeAge;

public class Bucketize extends MaskingFunction {

    public Bucketize() {
        this.name = "BUCKETIZE";
        this.inverseMaskingFunction = new InverseBucketizeAge();
    }

    public static String eval(double value, double bSize, double shift) {
        double l = Math.floor((value - shift) / bSize);
        double h = l + 1.0;
        return "[" + (l * bSize + shift) + " - " + (h * bSize + shift) + ")";
    }
}

