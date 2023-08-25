package de.tub.dima.mascara.dataMasking.alphabets;

public class PhoneAlphabet extends Alphabet{
    @Override
    public int binNDistinct(String lowerBound, String upperBound) {
        Integer low = Integer.getInteger(lowerBound);
        Integer up = Integer.getInteger(upperBound);
        return low <= up ? up - low : -1;
    }
}
