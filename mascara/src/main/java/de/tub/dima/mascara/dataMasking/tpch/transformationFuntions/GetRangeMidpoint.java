package de.tub.dima.mascara.dataMasking.tpch.transformationFuntions;

import de.tub.dima.mascara.dataMasking.TransformationFunction;

public class GetRangeMidpoint extends TransformationFunction {
    public GetRangeMidpoint() {
        this.name = "GET_RANGE_MIDPOINT";
    }

    public static double eval(String range) {
        range = range.replace("(", "").replace(")", "");
        range = range.replace("[", "").replace("]", "");
        String[] rangeSplit = range.split(",");
        double l = Double.parseDouble(rangeSplit[0].substring(1));
        double h = Double.parseDouble(rangeSplit[1].substring(0, rangeSplit[1].length() - 1));
        return (l + h) / 2.0;
    }
}
