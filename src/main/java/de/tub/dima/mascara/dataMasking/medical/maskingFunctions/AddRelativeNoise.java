package de.tub.dima.mascara.dataMasking.medical.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.Perturbation;

import java.util.Random;

public class AddRelativeNoise extends Perturbation {
    public AddRelativeNoise() {
        this.name = "ADD_RELATIVE_NOISE";
    }

    public static int eval(double value, double relNoise) {
        assert (relNoise > 0.0 && relNoise < 1.0);
        double lowerBound = value - (value * relNoise);
        double upperBound = value + (value * relNoise);

        Random random = new Random();
        double randomValue = lowerBound + (upperBound - lowerBound) * random.nextDouble();

        return (int) Math.round(randomValue);
    }

}
