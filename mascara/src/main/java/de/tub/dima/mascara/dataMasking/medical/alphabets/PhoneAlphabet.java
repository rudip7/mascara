package de.tub.dima.mascara.dataMasking.medical.alphabets;

import de.tub.dima.mascara.dataMasking.Alphabet;

public class PhoneAlphabet extends Alphabet {
    @Override
    public long indexOf(String value) {
        return Long.parseLong(value);
    }

    @Override
    public long binNDistinct(String lowerBound, String upperBound) {
        Long low = Long.parseLong(lowerBound);
        Long up = Long.parseLong(upperBound);
        return low != null && up != null && low <= up ? (up - low) : -1;
    }
}
