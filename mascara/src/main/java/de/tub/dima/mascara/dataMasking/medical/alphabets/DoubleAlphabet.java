package de.tub.dima.mascara.dataMasking.medical.alphabets;

import de.tub.dima.mascara.dataMasking.Alphabet;

public class DoubleAlphabet extends Alphabet {

    public double lowerBound;
    public double upperBound;
    public double accuracy;
    public DoubleAlphabet(double lowerBound, double upperBound, double accuracy) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.accuracy = accuracy;
    }
    public DoubleAlphabet(){
        this(Double.MIN_VALUE, Double.MAX_VALUE, 0.1);
    }


    public long indexOf(Double value) {
        if (value > upperBound || value < lowerBound){
            return -1;
        }
        return (long) ((value - lowerBound) / accuracy);
    }
    @Override
    public long indexOf(String value) {
        Double val = Double.parseDouble(value);
        return val == null ? -1 : indexOf(val);
    }
}
