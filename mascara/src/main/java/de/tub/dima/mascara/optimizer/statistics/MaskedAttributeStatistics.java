package de.tub.dima.mascara.optimizer.statistics;

import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaskedAttributeStatistics extends AttributeStatistics{

    public InverseMaskingFunction inverseMF;
    public int generalizationDegree = -1;
    public Map<String, Float> unmskDist = null;
    public long unmskNDistinct = -1;
    public String[] unmskMostCommonVals = null;
    public Float[] unmskMostCommonFreqs = null;
    public String[] unmskHistogramBounds = null;


    public MaskedAttributeStatistics(List<String> tableName, String attname, float nDistinct, String[] mostCommonVals, Float[] mostCommonFreqs, String[] histogramBounds, long size) {
        super(tableName, attname, nDistinct, mostCommonVals, mostCommonFreqs, histogramBounds, size);
    }

    public MaskedAttributeStatistics(AttributeStatistics stats) {
        super(stats);
    }

    public void unmaskStatistics(InverseMaskingFunction inverseMF){
        if (this.inverseMF == null){
            this.inverseMF = inverseMF;
            if (this.alphabet == null){
                this.alphabet = AlphabetCatalog.getInstance().getAlphabet(tableName, attname);
            }
            this.inverseMF.setAlphabet(this.alphabet);

            inverseHistogramBounds(inverseMF);
            inverseDistribution(inverseMF);
            estimateHistFreq(true);
        }
    }

    protected void inverseDistribution(InverseMaskingFunction inverseMF){
        List<String> newVals = new ArrayList<>();
        List<Float> newFreqs = new ArrayList<>();

        for (String mskVal : dist.keySet()) {
            List<String> unmskVals = inverseMF.eval(mskVal);
            newVals.addAll(unmskVals);
            float newFreq = dist.get(mskVal) / unmskVals.size();
            for (String unmskVal : unmskVals) {
                newFreqs.add(newFreq);
            }
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

    protected void inverseHistogramBounds(InverseMaskingFunction inverseMF) {
        if (histogramBounds == null){
            return;
        }
        List<String> unmskBounds = new ArrayList<>();

        for (String bound : histogramBounds) {
            List<String> unmskVals = inverseMF.eval(bound);
            // Maybe change to compare index in the alphabet
            unmskVals.sort(null);
            if (unmskBounds.size() == 0){
                unmskBounds.add(unmskVals.get(0));
            } else {
                unmskBounds.add(unmskVals.get(unmskVals.size() - 1));
            }
        }

        unmskHistogramBounds = unmskBounds.toArray(new String[0]);
    }

    @Override
    protected boolean computeHistIdx(){
        if (alphabet == null || unmskHistogramBounds == null || unmskHistogramBounds.length < 1){
            return false;
        }
        if (histIdx == null){
            histIdx = new long[unmskHistogramBounds.length];
            for (int i = 0; i < unmskHistogramBounds.length; i++) {
                histIdx[i] = alphabet.indexOf(unmskHistogramBounds[i]);
            }
        }
        return true;
    }

    public void estimateHistFreq(boolean masked) {
        if (unmskHistogramBounds != null && histApprxFreq == null){
            if (this.alphabet == null){
                this.alphabet = AlphabetCatalog.getInstance().getAlphabet(tableName, attname);
            }
            if (alphabet != null){
                String[] histogramBounds = this.unmskHistogramBounds;
                String[] mostCommonVals = this.unmskMostCommonVals;
                long remainingNDistinct = this.unmskMostCommonVals != null ? unmskNDistinct - this.unmskMostCommonVals.length : unmskNDistinct;
                float histBinFreq = restFreq / (this.unmskHistogramBounds.length - 1);

                computeApproxFreq(histogramBounds, mostCommonVals, remainingNDistinct, histBinFreq);
            }
        }
    }





    @Override
    public Float getFreq(String value){
        if (unmskDist != null){
            Float freq = unmskDist.get(value);
            return freq != null ? freq : 0.0f;
        } else if (dist != null) {
            Float freq = dist.get(value);
            return freq != null ? freq : 0.0f;
        } else {
            return 0.0f;
        }
    }

    @Override
    public long getnDistinct() {
        return unmskNDistinct;
    }

    @Override
    public String[] getHistogramBounds() {
        return unmskHistogramBounds;
    }

    @Override
    public String getHistogramBound(int index) {
        return index < unmskHistogramBounds.length && index >= 0 ? unmskHistogramBounds[index] : null;
    }

    @Override
    public Map<String, Float> getDist() {
        if (unmskDist != null){
            return unmskDist;
        } else {
            return dist;
        }
    }

    public InverseMaskingFunction getInverseMF() {
        return inverseMF;
    }

    public int getGeneralizationDegree() {
        return generalizationDegree;
    }
    @Override
    public String[] getMostCommonVals() {
        return unmskMostCommonVals;
    }
    @Override
    public Float[] getMostCommonFreqs() {
        return unmskMostCommonFreqs;
    }
}
