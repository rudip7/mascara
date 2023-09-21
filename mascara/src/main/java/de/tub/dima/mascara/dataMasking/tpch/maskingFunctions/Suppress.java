package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;

import java.util.Random;

public class Suppress extends MaskingFunction {
    public Suppress() {
        this.name = "SUPPRESS";
    }

    public static String eval(Object value) {
        return "XXXXX";
    }
}
