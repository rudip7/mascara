package de.tub.dima.mascara.dataMasking.medical.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.inverseFunctions.InverseBucketizeAge;

public class BucketizeAge extends Generalization {

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

