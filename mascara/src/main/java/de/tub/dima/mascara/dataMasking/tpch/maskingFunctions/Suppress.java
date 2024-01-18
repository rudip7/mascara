package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.Suppression;

import java.util.Random;

public class Suppress extends Suppression {
    public Suppress() {
        this.name = "SUPPRESS";
    }

    public static String eval(Object value) {
        return "XXXXX";
    }
}
