package de.tub.dima.mascara.dataMasking.medical.alphabets;

import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;

import java.util.HashMap;
import java.util.Map;

public class IntegerAlphabet extends DiscretizedAlphabet {

    public long lowerBound;
    public long upperBound;

    public IntegerAlphabet(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
    public IntegerAlphabet(){
        this(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
    }

    public IntegerAlphabet(boolean discretize){
        this();
        this.discretize = discretize;
    }

    public long indexOf(Integer value) {
        if (value > upperBound || value < lowerBound){
            return -1;
        }
        return value - lowerBound;
    }

    @Override
    public long indexOf(String value) {
        Double parsedDouble = Double.parseDouble(value);
        return parsedDouble == null || parsedDouble % 1 != 0 ? -1 : indexOf(parsedDouble.intValue());
    }

    @Override
    public String getDiscretizedValue(String value) {
        try {
            Double doubleValue = Double.parseDouble(value);
            Double discretizedValue = Math.floor(doubleValue);
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

    @Override
    public Map<String, Long> getAlphabet() {
        if (alphabet == null){
            alphabet = new HashMap<>();
            for (long i = lowerBound; i <= upperBound; i++) {
                alphabet.put(String.valueOf(i), i - lowerBound);
            }
        }
        return alphabet;
    }
}
