package de.tub.dima.mascara.optimizer.statistics;

import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscretizedMaskedAttributeStatistics extends MaskedAttributeStatistics implements DiscretizedStatistics{
    public Float sumAbsFreq = 0.0f;
    public DiscretizedMaskedAttributeStatistics(List<String> tableName, String attname, float nDistinct, String[] mostCommonVals, Float[] mostCommonFreqs, String[] histogramBounds, long size) {
        super(tableName, attname, nDistinct, mostCommonVals, mostCommonFreqs, histogramBounds, size);
    }

    public DiscretizedMaskedAttributeStatistics(AttributeStatistics stats) {
        super(stats);
    }


    @Override
    protected void inverseHistogramBounds(InverseMaskingFunction inverseMF) {
        super.inverseHistogramBounds(inverseMF);
        if (histogramBounds == null){
            return;
        }
        histApprxNDistinct = new Long[unmskHistogramBounds.length - 1];
        for (int i = 0; i < this.unmskHistogramBounds.length; i++) {
            if (i > 0){
                // Get number of distinct values per bin
                histApprxNDistinct[i-1] = alphabet.binNDistinct(unmskHistogramBounds[i-1], unmskHistogramBounds[i]);
            }
        }
    }

    @Override
    protected void inverseDistribution(InverseMaskingFunction inverseMF) {
        List<String> newVals = new ArrayList<>();
        List<Float> newFreqs = new ArrayList<>();

        for (String mskVal : dist.keySet()) {
            // Maybe discretization ingest the same value multiple times
            List<String> unmskVals = inverseMF.eval(mskVal);
            newVals.addAll(unmskVals);
            Float newFreq = Float.valueOf(Math.round(dist.get(mskVal) / unmskVals.size() * size) + 1);
            for (String unmskVal : unmskVals) {
                // Remove value from the corresponding bin
                int idx = getBucketIdx(unmskVal);
                if (idx >= 0) {
                    if (histApprxNDistinct[idx] < 1){
                        throw new RuntimeException("Bin has only values from the MCV. This should never happen.");
                    }
                    histApprxNDistinct[idx] -= 1;
                }
                newFreqs.add(newFreq);
            }
            sumAbsFreq += newFreq * unmskVals.size();
        }

        if (generalizationDegree < 1) {
            generalizationDegree = newVals.size() / mostCommonVals.length;
        }

        unmskDist = new HashMap<>();
        for (int i = 0; i < newVals.size(); i++) {
            unmskDist.put(newVals.get(i), newFreqs.get(i));
        }

        unmskMostCommonVals = newVals.toArray(new String[0]);
        unmskMostCommonFreqs = newFreqs.toArray(new Float[0]);

        unmskNDistinct = nDistinct * generalizationDegree;
    }

    @Override
    public void estimateHistFreq(boolean masked) {
        if (unmskHistogramBounds != null && unmskHistogramBounds.length > 0 && histApprxFreq == null){
            histApprxFreq = new Float[histogramBounds.length - 1];
            Float histBinFreq = Float.valueOf(Math.round(restFreq * size / histApprxFreq.length));
            for (int i = 0; i < this.histApprxFreq.length; i++) {
                histApprxFreq[i] = histBinFreq / histApprxNDistinct[i] + 1;
                sumAbsFreq += (histBinFreq + histApprxNDistinct[i]);
            }
        }
    }

    @Override
    public Float getSumAbsoluteFreq() {
        return sumAbsFreq;
    }

}
