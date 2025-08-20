package de.tub.dima.mascara.optimizer.statistics;

import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;

import java.util.List;

public class SuppressedAttributeStatistics extends MaskedAttributeStatistics{
    public boolean stillSuppressed = false;

    public SuppressedAttributeStatistics(List<String> tableName, String attname, float nDistinct, String[] mostCommonVals, Float[] mostCommonFreqs, String[] histogramBounds, long size) {
        super(tableName, attname, nDistinct, mostCommonVals, mostCommonFreqs, histogramBounds, size);
    }

    public SuppressedAttributeStatistics(AttributeStatistics stats) {
        super(stats);
    }

    @Override
    public void unmaskStatistics(InverseMaskingFunction inverseMF) {
        super.unmaskStatistics(inverseMF);
        if (this.alphabet == null || this.alphabet.getAlphabet() == null){
            stillSuppressed = true;
        }
    }

    public boolean isStillSuppressed() {
        return stillSuppressed;
    }
}
