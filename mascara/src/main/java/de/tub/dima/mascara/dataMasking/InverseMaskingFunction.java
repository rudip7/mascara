package de.tub.dima.mascara.dataMasking;

import java.util.List;

public abstract class InverseMaskingFunction {
    public String name;
    public Alphabet alphabet;
    public boolean discretize = false;

    public boolean highGeneralization(int nValuesToUnmask) {
        return false;
    }

    public abstract List<String> eval(String maskedValue);

    public String getName() {
        return name;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(Alphabet alphabet) {
        this.alphabet = alphabet;
    }
}
