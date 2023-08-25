package de.tub.dima.mascara.dataMasking.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.inverseFunctions.InverseBucketizeAge;

public class BucketizeAge extends MaskingFunction {

    public BucketizeAge() {
        this.name = "BUCKETIZE_AGE";
        this.inverseMaskingFunction = new InverseBucketizeAge();
    }

    public static String eval(int age, int bSize) {
        int l = age / bSize;
        int h = l + 1;
        return "[" + (l * bSize) + " - " + ((h * bSize) - 1) + "]";
    }
}

