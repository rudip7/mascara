package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.tpch.transformationFuntions.GetRangeMidpoint;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseBucketize;

import java.math.BigDecimal;
import java.util.List;

public class Bucketize extends Generalization {

    public Bucketize() {
        this.name = "BUCKETIZE";
        this.inverseMaskingFunction = new InverseBucketize();
        this.parametrizedInverse = true;
        this.transformationFunction = new GetRangeMidpoint();
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
        return "[" + (l * bSize) + "," + (h * bSize) + ")";
    }
}

