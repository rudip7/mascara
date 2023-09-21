package de.tub.dima.mascara.dataMasking.medical.alphabets;

import de.tub.dima.mascara.dataMasking.Alphabet;

public class IntegerAlphabet extends Alphabet {

    public int lowerBound;
    public int upperBound;
    public IntegerAlphabet(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
    public IntegerAlphabet(){
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }


    public long indexOf(Integer value) {
        if (value > upperBound || value < lowerBound){
            return -1;
        }
        return value - lowerBound;
    }
    @Override
    public long indexOf(String value) {
        Integer val = Integer.getInteger(value);
        return val == null ? -1 : indexOf(val);
    }
}
