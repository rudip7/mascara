package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;

import java.time.LocalDate;

public class GeneralizeSign extends Generalization {

    public GeneralizeSign() {
        this.name = "GENERALIZE_SIGN";
    }

    public static String eval(Number value) throws Exception {
        if (value.doubleValue() > 0.0) {
            return "POSITIVE";
        } else if (value.doubleValue() < 0.0) {
            return "NEGATIVE";
        } else {
            return "ZERO";
        }
    }
}
