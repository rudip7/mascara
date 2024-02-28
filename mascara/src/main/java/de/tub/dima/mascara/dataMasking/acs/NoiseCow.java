package de.tub.dima.mascara.dataMasking.acs;

import de.tub.dima.mascara.dataMasking.Perturbation;

import java.util.Random;

public class NoiseCow extends Perturbation {
    public NoiseCow() {
        this.name = "NOISE_COW";
    }

    public static float eval(float value, float noise) {
        // TODO
        return value;
    }

}
