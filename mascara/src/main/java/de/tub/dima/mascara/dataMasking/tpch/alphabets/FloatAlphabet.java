package de.tub.dima.mascara.dataMasking.tpch.alphabets;

import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;

public class FloatAlphabet extends DiscretizedAlphabet {

    public double lowerBound;
    public double upperBound;
    public int precision;
    public double accuracy;
    public FloatAlphabet(float lowerBound, float upperBound, int precision) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.precision = precision;
        this.accuracy = Math.pow(10, -precision);
        this.discretize = true;
    }
    public FloatAlphabet(){
        this(Float.MIN_VALUE, Float.MAX_VALUE, 1);
    }


    public long indexOf(Double value) {
        if (value > upperBound || value < lowerBound){
            return -1;
        }
        return (long) ((value - lowerBound) / accuracy);
    }
    @Override
    public long indexOf(String value) {
        try {
            Double val = Double.parseDouble(value);
            return indexOf(val);
        } catch (NumberFormatException e){
            return -1;
        }
    }

    @Override
    public String getDiscretizedValue(String value) {
        try {
            Double doubleValue = Double.parseDouble(value);
            double bNumber = Math.floor(doubleValue / accuracy);
            Double discretizedValue = bNumber * accuracy;
            return discretizedValue.toString();
        } catch (NumberFormatException e){
            return value;
        }
    }

    @Override
    public boolean isDiscretizeble(String sampleValue) {
        try {
            Double doubleValue = Double.parseDouble(sampleValue);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public double getAccuracy() {
        return accuracy;
    }
}
