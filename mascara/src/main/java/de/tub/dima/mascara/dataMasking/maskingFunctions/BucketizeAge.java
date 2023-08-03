package de.tub.dima.mascara.dataMasking.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;

public class BucketizeAge extends MaskingFunction {

    public BucketizeAge() {
        this.name = "BUCKETIZE_AGE";
    }

    public static String eval(int age, int bSize) {
        int l = age / bSize;
        int h = l + 1;
        return "[" + (l * bSize) + " - " + ((h * bSize) - 1) + "]";
    }
}

