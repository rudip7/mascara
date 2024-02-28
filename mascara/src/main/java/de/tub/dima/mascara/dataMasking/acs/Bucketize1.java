package de.tub.dima.mascara.dataMasking.acs;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseBucketize;

import java.math.BigDecimal;
import java.util.List;

public class Bucketize1 extends Generalization {

    public Bucketize1() {
        this.name = "BUCKETIZE_1";
        this.inverseMaskingFunction = new InverseBucketize();
        this.parametrizedInverse = true;
    }

    @Override
    public void setInverseMaskingFunction(List<Object> parameters) {
        if (parameters.size() == 1){
            if (parameters.get(0) instanceof BigDecimal){
                double bSize = ((BigDecimal) parameters.get(0)).doubleValue();
                ((InverseBucketize) this.inverseMaskingFunction).setbSize(bSize);
            }
        }
    }

    public static String eval(double value, double bSize) {
        double l = Math.floor((value) / bSize);
        double h = l + 1.0;
        return "[" + (l * bSize + 1.0) + "," + (h * bSize + 1.0) + ")";
    }
}

