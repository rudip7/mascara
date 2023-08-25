package de.tub.dima.mascara.dataMasking.alphabets;

public class DoubleAlphabet extends Alphabet{

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


    public int indexOf(Double value) {
        if (value > upperBound || value < lowerBound){
            return -1;
        }
        return (int) ((value - lowerBound) / accuracy);
    }
    @Override
    public int indexOf(String value) {
        Double val = Double.parseDouble(value);
        return val == null ? -1 : indexOf(val);
    }
}
