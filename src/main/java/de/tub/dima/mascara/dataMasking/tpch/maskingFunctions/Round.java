package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Perturbation;

import java.util.Random;

public class Round extends Perturbation {
    public Round() {
        this.name = "ROUND";
    }

    public static float eval(float value, int n) {
        double scale = Math.pow(10, n);
        return (float) (Math.round(value * 100.0) / 100.0);
    }

}
