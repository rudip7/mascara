package de.tub.dima.mascara.dataMasking;

import de.tub.dima.mascara.dataMasking.alphabets.Alphabet;

import java.util.List;

public abstract class InverseMaskingFunction {
    public String name;
    public Alphabet alphabet;

    public abstract List<String> eval(String maskedValue);

    public String getName() {
        return name;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }
}
