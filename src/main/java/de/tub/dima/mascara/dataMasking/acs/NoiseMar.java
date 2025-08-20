package de.tub.dima.mascara.dataMasking.acs;

import de.tub.dima.mascara.dataMasking.Perturbation;

public class NoiseMar extends Perturbation {
    public NoiseMar() {
        this.name = "NOISE_MAR";
    }

    public static float eval(float value, float noise) {
        // TODO
        return value;
    }

}
