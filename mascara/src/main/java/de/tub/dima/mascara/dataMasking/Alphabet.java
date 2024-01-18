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

    /**
     * Computes the number of possible values within the bin where the upperbound is exclusive
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public long binNDistinct(String lowerBound, String upperBound){
        long low = indexOf(lowerBound);
        long up = indexOf(upperBound);
        if (low >= 0 && up >= 0 && up > low){
            return up - low - 1;
        }
        if (up == low){
            return 1;
        }
        return -1;
    }
}
