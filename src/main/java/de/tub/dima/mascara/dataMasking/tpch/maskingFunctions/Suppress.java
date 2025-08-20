package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.Suppression;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseSuppressed;

import java.util.Random;

public class Suppress extends Suppression {
    public Suppress() {
        this.name = "SUPPRESS";
        this.inverseMaskingFunction = new InverseSuppressed();
    }

    public static String eval(Object value) {
        return "XXXXX";
    }
}
