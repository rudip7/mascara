package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseBucketize;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseBucketizeLow;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseGeneralizeDate;
import org.apache.calcite.util.NlsString;

import java.util.List;

public class BucketizeLow extends MaskingFunction {

    public BucketizeLow() {
        this.name = "BUCKETIZE_LOW";
        this.inverseMaskingFunction = null;
        this.parametrizedInverse = true;
    }

    @Override
    public void setInverseMaskingFunction(List<Object> parameters) {
        if (parameters.size() == 1){
            Double bSize;
            Object param = parameters.get(0);
            this.inverseMaskingFunction = new InverseGeneralizeDate((String) parameters.get(0));
        }
    }

    public static Double eval(double value, double bSize) {
        double l = Math.floor((value) / bSize);
        return Math.floor((value) / bSize);
    }
}

