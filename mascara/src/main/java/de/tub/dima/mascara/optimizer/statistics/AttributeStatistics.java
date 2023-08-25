package de.tub.dima.mascara.optimizer.statistics;

import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import de.tub.dima.mascara.dataMasking.alphabets.Alphabet;
import de.tub.dima.mascara.dataMasking.alphabets.AlphabetCatalog;

import java.util.*;

public class AttributeStatistics {
    public List<String> tableName;
    public String attname;
    public long nDistinct;
    public String[] mostCommonVals;
    public Float[] mostCommonFreqs;
    public final float restFreq;
    public String[] histogramBounds;
    public Map<String, Float> dist;
    public long size;

    /**
     * These attributes are only relevant for masked attributes.
     */

    public InverseMaskingFunction inverseMF;
    public Alphabet alphabet;
    public int generalizationDegree;
    public Map<String, Float> unmskDist;
    public long unmskNDistinct;
    public String[] unmskMostCommonVals;
    public Float[] unmskMostCommonFreqs;
    public String[] unmskHistogramBounds;

    /**
     * Mascara will compute these statistics if an inverse masking function and an alphabet for this column exists
     */
    public Long[] histApprxNDistinct;
    public Float[] histApprxFreq;



    public AttributeStatistics(List<String> tableName, String attname, float nDistinct, String[] mostCommonVals, Float[] mostCommonFreqs, String[] histogramBounds, long size) {
        this.tableName = tableName;
        this.attname = attname;
        this.nDistinct = (long) (nDistinct >= 0 ? nDistinct : -1* nDistinct *size);
        this.mostCommonVals = mostCommonVals;
        this.mostCommonFreqs = mostCommonFreqs;
        this.histogramBounds = histogramBounds;
        this.size = size;
        this.dist = new HashMap<>();
        if (mostCommonVals != null) {
            for (int i = 0; i < mostCommonVals.length; i++) {
                this.dist.put(mostCommonVals[i], mostCommonFreqs[i]);
            }
        }
        this.restFreq = 1 - sumArray(this.mostCommonFreqs);
        this.alphabet = AlphabetCatalog.getInstance().getAlphabet(tableName, attname);
        this.generalizationDegree = -1;
    }

    public void unmaskStatistics(InverseMaskingFunction inverseMF){
        if (this.inverseMF != null){
            this.inverseMF = inverseMF;
            if (alphabet == null){
                alphabet = inverseMF.getAlphabet();
            }

            inverseDistribution(inverseMF);
            inverseHistogramBounds(inverseMF);
            estimateHistFreq(true);
        }
    }

    private void inverseDistribution(InverseMaskingFunction inverseMF){
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

    private void inverseHistogramBounds(InverseMaskingFunction inverseMF) {
        List<String> unmskBounds = new ArrayList<>();

        for (String bound : histogramBounds) {
            List<String> unmskVals = inverseMF.eval(bound);
            unmskVals.sort(null);
            if (unmskBounds.size() == 0){
                unmskBounds.add(unmskVals.get(0));
            } else {
                unmskBounds.add(unmskVals.get(unmskVals.size() - 1));
            }
        }

        unmskHistogramBounds = unmskBounds.toArray(new String[0]);
    }

    public int getBucketIdx(String val) {
        for (int i = 1; i < histogramBounds.length; i++) {
            if (val.compareTo(histogramBounds[i]) < 0) {
                return i - 1;
            }
        }
        return -1;
    }

    public int getUnmskBucketIdx(String val) {
        for (int i = 1; i < unmskHistogramBounds.length; i++) {
            if (val.compareTo(unmskHistogramBounds[i]) < 0) {
                return i - 1;
            }
        }
        return -1;
    }

    public void estimateHistFreq(boolean masked) {
        if (alphabet != null && histApprxFreq == null){
            String[] histogramBounds;
            String[] mostCommonVals;
            long remainingNDistinct;
            float histBinFreq;

            if (!masked) {
                histogramBounds = this.histogramBounds;
                mostCommonVals = this.mostCommonVals;
                remainingNDistinct = nDistinct - this.mostCommonVals.length;
                histBinFreq = restFreq / (this.histogramBounds.length - 1);
            } else {
                histogramBounds = this.unmskHistogramBounds;
                mostCommonVals = this.unmskMostCommonVals;
                remainingNDistinct = unmskNDistinct - this.unmskMostCommonVals.length;
                histBinFreq = restFreq / (this.unmskHistogramBounds.length - 1);
            }


            int[] histNDistinct = new int[histogramBounds.length-1];
            for (int i = 0; i < histogramBounds.length; i++) {
                histNDistinct[i] = alphabet.binNDistinct(histogramBounds[i], histogramBounds[i+1]);
            }

            int totalNDistinctHist = alphabet.binNDistinct(histogramBounds[0], histogramBounds[histogramBounds.length-1]);

            for (String val : mostCommonVals) {
                int idx;
                if (!masked) {
                    idx = getBucketIdx(val);
                } else {
                    idx = getUnmskBucketIdx(val);
                }
                if (idx >= 0) {
                    histNDistinct[idx] -= 1;
                }
            }

            float[] histRelNDistinct = new float[histNDistinct.length];
            for (int i = 0; i < histNDistinct.length; i++) {
                histRelNDistinct[i] = (float) histNDistinct[i] / totalNDistinctHist;
            }

            histApprxNDistinct = new Long[histRelNDistinct.length];
            for (int i = 0; i < histRelNDistinct.length; i++) {
                histApprxNDistinct[i] = (long) (histRelNDistinct[i] * remainingNDistinct);
            }

            histApprxFreq = new Float[histApprxNDistinct.length];
            for (int i = 0; i < histApprxNDistinct.length; i++) {
                histApprxFreq[i] = histBinFreq / histApprxNDistinct[i];
            }
        }
    }

    private float sumArray(Float[] array) {
        float sum = 0;
        for (Float value : array) {
            sum += value != null ? value : 0;
        }
        return sum;
    }

    public void adaptFreqSelectivity(float selectivity) {
        assert selectivity >= 0.0 && selectivity <= 1.0 : "The selectivity needs to be a value between 0.0 and 1.0";

        if (unmskDist == null) {
            for (Map.Entry<String, Float> entry : dist.entrySet()) {
                dist.put(entry.getKey(), entry.getValue() * selectivity);
            }
            dist.put("NaN", 1.0f - selectivity);
        } else {
            for (Map.Entry<String, Float> entry : unmskDist.entrySet()) {
                unmskDist.put(entry.getKey(), entry.getValue() * selectivity);
            }
            unmskDist.put("NaN", 1.0f - selectivity);
        }

        if (histApprxFreq != null) {
            for (int i = 0; i < histApprxFreq.length; i++) {
                histApprxFreq[i] *= selectivity;
                // histApprxNDistinct[i] = histApprxFreq[i] * selectivity; // You might want to uncomment this line if needed
            }
        }
    }


    public String getAttname() {
        return attname;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
