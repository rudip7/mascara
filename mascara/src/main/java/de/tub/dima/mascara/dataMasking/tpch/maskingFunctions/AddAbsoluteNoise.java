package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Perturbation;

import java.util.Random;

public class AddAbsoluteNoise extends Perturbation {
    public AddAbsoluteNoise() {
        this.name = "ADD_ABSOLUTE_NOISE";
    }

    public static float eval(float value, float noise) {
        Random random = new Random();
        double randomValue = (value - noise) + (2.0 * noise * random.nextDouble());

        return (float) (Math.round(randomValue * 100.0) / 100.0);
    }

}
