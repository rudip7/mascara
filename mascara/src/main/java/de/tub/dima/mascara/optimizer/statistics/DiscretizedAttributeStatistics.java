package de.tub.dima.mascara.optimizer.statistics;

import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;

import java.util.HashMap;
import java.util.List;

public class DiscretizedAttributeStatistics extends AttributeStatistics implements DiscretizedStatistics{

    public String[] discMostCommonVals;
    public String[] discHistogramBounds;
    public Float sumAbsFreq = 0.0f;

    public DiscretizedAttributeStatistics(List<String> tableName, String attname, float nDistinct, String[] mostCommonVals, Float[] mostCommonFreqs, String[] histogramBounds, long size) {
        super(tableName, attname, nDistinct, mostCommonVals, mostCommonFreqs, histogramBounds, size);
        assert this.alphabet != null && this.alphabet instanceof DiscretizedAlphabet && ((DiscretizedAlphabet) this.alphabet).shouldDiscretize();
        discretizeStatistics();
    }

    public DiscretizedAttributeStatistics(AttributeStatistics stats) {
        super(stats);
        assert this.alphabet != null && this.alphabet instanceof DiscretizedAlphabet && ((DiscretizedAlphabet) this.alphabet).shouldDiscretize();
        discretizeStatistics();
    }

    public void discretizeStatistics() {
        DiscretizedAlphabet discretizedAlphabet = (DiscretizedAlphabet) alphabet;
        // Check if values are already discretizable
        if (histogramBounds != null && histogramBounds.length > 0) {
            if(discretizedAlphabet.isDiscretizeble(histogramBounds[0])){
                discretized = true;
                histApprxNDistinct = new Long[histogramBounds.length - 1];
                discHistogramBounds = new String[this.histogramBounds.length];
                histIdx = new long[this.histogramBounds.length];
                for (int i = 0; i < this.histogramBounds.length; i++) {
                    // Discretize histogram bounds
                    discHistogramBounds[i] = discretizedAlphabet.getDiscretizedValue(histogramBounds[i]);
                    histIdx[i] = discretizedAlphabet.indexOf(discHistogramBounds[i]);
                    if (i > 0){
                        // Get number of distinct values per bin
                        histApprxNDistinct[i-1] = alphabet.binNDistinct(discHistogramBounds[i-1], discHistogramBounds[i]);
                    }
                }
            }
        }
        if (mostCommonVals != null && mostCommonVals.length > 0){
            if (discretizedAlphabet.isDiscretizeble(mostCommonVals[0])){
                discretized = true;
                // Discretize most common values
                discMostCommonVals = new String[mostCommonVals.length];
                for (int i = 0; i < this.mostCommonVals.length; i++) {
                    discMostCommonVals[i] = discretizedAlphabet.getDiscretizedValue(mostCommonVals[i]);
                }
                dist = new HashMap<>();
                for (int i = 0; i < discMostCommonVals.length; i++) {
                    if (dist.containsKey(discMostCommonVals[i])){
                        Float absFreq = Float.valueOf(Math.round(mostCommonFreqs[i] * size) + 1);
                        sumAbsFreq += absFreq;
                        dist.put(discMostCommonVals[i], dist.get(discMostCommonVals[i])+absFreq);
                    } else {
                        // Remove value from the corresponding bin
                        int idx = getBucketIdx(discMostCommonVals[i]);
                        if (idx >= 0 && histApprxNDistinct[idx] > 1) {
                            histApprxNDistinct[idx] -= 1;
                        }
                        Float absFreq = Float.valueOf(Math.round(mostCommonFreqs[i] * size) + 1);
                        sumAbsFreq += absFreq;
                        this.dist.put(discMostCommonVals[i], absFreq);
                    }
                }
            }
        }
        if (discHistogramBounds != null && discHistogramBounds.length > 0) {
            histApprxFreq = new Float[histogramBounds.length - 1];
            Float histBinFreq = Float.valueOf(Math.round(restFreq * size / histApprxFreq.length));
            for (int i = 0; i < this.histApprxFreq.length; i++) {
                histApprxFreq[i] = histBinFreq / histApprxNDistinct[i] + 1;
                sumAbsFreq += (histBinFreq + histApprxNDistinct[i]);
            }
        }
    }

    @Override
    public int getBucketIdx(String val) {
        if (alphabet != null && discHistogramBounds != null){
            long idx = alphabet.indexOf(val);
            if (alphabet.indexOf(discHistogramBounds[0]) <= idx){
                for (int i = 1; i < discHistogramBounds.length; i++) {
                    if (alphabet.indexOf(discHistogramBounds[i]) >= idx) {
                        return i - 1;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public Float getSumAbsoluteFreq() {
        return sumAbsFreq;
    }
}
