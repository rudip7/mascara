package de.tub.dima.mascara.dataMasking;

import java.util.List;
import java.util.Map;

public abstract class Alphabet {
    public Map<String, Integer> alphabet;

    public long indexOf(String value){
        if (this.alphabet != null){
            Integer index = this.alphabet.get(value);
            if (index != null){
                return index;
            }
        }
        return -1;
    }

    public long binNDistinct(String lowerBound, String upperBound){
        long low = indexOf(lowerBound);
        long up = indexOf(upperBound);
        if (low >= 0 && up >= 0 && up > low){
            return up - low;
        }
        if (up == low){
            return 1;
        }
        return -1;
    }
}
