package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Perturbation;

import java.util.Random;

public class AddLaplaceNoise extends Perturbation {
    public AddLaplaceNoise() {
        this.name = "ADD_LAPLACE_NOISE";
    }

    public static float eval(float value, float sensitivity, float epsilon) {
        // TODO: Add Laplace noise to the value
        return value;
    }

}
