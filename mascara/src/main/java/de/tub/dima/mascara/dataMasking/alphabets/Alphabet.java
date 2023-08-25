package de.tub.dima.mascara.dataMasking.alphabets;

import java.util.List;
import java.util.Map;

public abstract class Alphabet {
    public Map<String, Integer> alphabet;

    public int indexOf(String value){
        if (this.alphabet != null){
            Integer index = this.alphabet.get(value);
            if (index != null){
                return index;
            }
        }
        return -1;
    }

    public int binNDistinct(String lowerBound, String upperBound){
        int low = indexOf(lowerBound);
        int up = indexOf(upperBound);
        if (low >= 0 && up >= 0 && up >= low){
            return up - low;
        }
        return -1;
    }
}
