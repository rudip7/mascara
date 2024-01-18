package de.tub.dima.mascara.dataMasking;

public abstract class DiscretizedAlphabet extends Alphabet {
    public boolean discretize = false;
    public abstract String getDiscretizedValue(String value);
    public abstract boolean isDiscretizeble(String sampleValue);

    public boolean shouldDiscretize() {
        return discretize;
    }

    public void setDiscretize(boolean discretize) {
        this.discretize = discretize;
    }
}
