package de.tub.dima.mascara.optimizer.statistics;

import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;
import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.AlphabetCatalog;

import java.util.*;

public class AttributeStatistics implements Cloneable{
    public List<String> tableName;
    public String attname;
    public long nDistinct;
    public String[] mostCommonVals;
    public Float[] mostCommonFreqs;

    public float restFreq;
    public String[] histogramBounds;
    public Map<String, Float> dist;
    public long size;

    public Alphabet alphabet;

    public boolean discretized = false;

    /**
     * Mascara will compute these statistics if an inverse masking function and an alphabet for this column exists
     */
    public Long[] histApprxNDistinct = null;
    public Float[] histApprxFreq = null;
    public long[] histIdx;



    public AttributeStatistics(List<String> tableName, String attname, float nDistinct, String[] mostCommonVals, Float[] mostCommonFreqs, String[] histogramBounds, long size) {
        this.tableName = tableName;
        this.attname = attname;
        this.alphabet = AlphabetCatalog.getInstance().getAlphabet(tableName, attname);

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
        this.restFreq = 1.0f - sumArray(this.mostCommonFreqs);
    }

    AttributeStatistics(AttributeStatistics stats) {
        this.tableName = stats.tableName;
        this.attname = stats.attname;
        this.alphabet = AlphabetCatalog.getInstance().getAlphabet(tableName, attname);

        this.nDistinct = stats.nDistinct;
        this.mostCommonVals = stats.mostCommonVals;
        this.mostCommonFreqs = stats.mostCommonFreqs;
        this.histogramBounds = stats.histogramBounds;
        this.size = stats.size;
        this.dist = stats.dist;

        this.restFreq = stats.restFreq;
    }

    public int getBucketIdx(String val) {
        if (computeHistIdx()){
            long idx = alphabet.indexOf(val);
            if (histIdx[0] <= idx){
                for (int i = 1; i < histIdx.length; i++) {
                    if (histIdx[i] >= idx) {
                        return i - 1;
                    }
                }
            }
        }
        return -1;
    }

    protected boolean computeHistIdx(){
        if (alphabet == null || histogramBounds == null || histogramBounds.length < 1){
            return false;
        }
        if (histIdx == null){
            histIdx = new long[histogramBounds.length];
            for (int i = 0; i < histogramBounds.length; i++) {
                histIdx[i] = alphabet.indexOf(histogramBounds[i]);
            }
        }
        return true;
    }

    public boolean isValueInHistogram(String value) {
        if (computeHistIdx()){
            long idx = alphabet.indexOf(value);
            return idx >= histIdx[0] && idx <= histIdx[histIdx.length -1];
        }
        return false;
    }



    public void estimateHistFreq(boolean masked) {
        if (histogramBounds != null && histApprxFreq == null){
            if (this.alphabet == null){
                this.alphabet = AlphabetCatalog.getInstance().getAlphabet(tableName, attname);
            }
            if (alphabet != null){
                String[] histogramBounds = this.histogramBounds;
                String[] mostCommonVals = this.mostCommonVals;
                long remainingNDistinct = this.mostCommonVals != null ? nDistinct - this.mostCommonVals.length : nDistinct;
                float histBinFreq = restFreq / (this.histogramBounds.length - 1);

                computeApproxFreq(histogramBounds, mostCommonVals, remainingNDistinct, histBinFreq);

            }
        }
    }

    void computeApproxFreq(String[] histogramBounds, String[] mostCommonVals, long remainingNDistinct, float histBinFreq) {
        long[] histNDistinct = new long[histogramBounds.length-1];
        for (int i = 0; i < histogramBounds.length-1; i++) {
            histNDistinct[i] = alphabet.binNDistinct(histogramBounds[i], histogramBounds[i+1]);
        }

        long totalNDistinctHist = alphabet.binNDistinct(histogramBounds[0], histogramBounds[histogramBounds.length-1]);

        if (mostCommonVals != null){
            for (String val : mostCommonVals) {
                int idx = getBucketIdx(val);

                if (idx >= 0) {
                    histNDistinct[idx] -= 1;
                }
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

    private float sumArray(Float[] array) {
        if (array == null){
            return 0.0f;
        }
        float sum = 0;
        for (Float value : array) {
            sum += value != null ? value : 0;
        }
        return sum;
    }

    @Deprecated
    public void adaptFreqSelectivity(float selectivity) {
        assert selectivity >= 0.0 && selectivity <= 1.0 : "The selectivity needs to be a value between 0.0 and 1.0";
//        if (unmskDist == null) {
//            for (Map.Entry<String, Float> entry : dist.entrySet()) {
//                dist.put(entry.getKey(), entry.getValue() * selectivity);
//            }
//            dist.put("NaN", 1.0f - selectivity);
//        } else {
//            for (Map.Entry<String, Float> entry : unmskDist.entrySet()) {
//                unmskDist.put(entry.getKey(), entry.getValue() * selectivity);
//            }
//            unmskDist.put("NaN", 1.0f - selectivity);
//        }
//
//        if (histApprxFreq != null) {
//            for (int i = 0; i < histApprxFreq.length; i++) {
//                histApprxFreq[i] *= selectivity;
//                // histApprxNDistinct[i] = histApprxFreq[i] * selectivity; // You might want to uncomment this line if needed
//            }
//        }
    }


    public String getAttname() {
        return attname;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public AttributeStatistics partialDeepCopy() throws CloneNotSupportedException {
        AttributeStatistics cloned = (AttributeStatistics) super.clone();
        if (this.histApprxNDistinct != null) {
            cloned.histApprxNDistinct = Arrays.copyOf(this.histApprxNDistinct, this.histApprxNDistinct.length);
        }
        if (this.histApprxFreq != null) {
            cloned.histApprxFreq = Arrays.copyOf(this.histApprxFreq, this.histApprxFreq.length);
        }
        return cloned;
    }

    public Float getFreq(String value){
        Float freq = dist.get(value);
        return freq != null ? freq : 0.0f;
    }

    public List<String> getTableName() {
        return tableName;
    }

    public long getnDistinct() {
        return nDistinct;
    }

    public String[] getMostCommonVals() {
        return mostCommonVals;
    }

    public Float[] getMostCommonFreqs() {
        return mostCommonFreqs;
    }

    public float getRestFreq() {
        return restFreq;
    }

    public String[] getHistogramBounds() {
        return histogramBounds;
    }

    public String getHistogramBound(int index) {
        return index < histogramBounds.length && index >= 0 ? histogramBounds[index] : null;
    }

    public boolean hasHistogram(){
        return histogramBounds != null && histogramBounds.length > 0;
    }

    public long[] getHistogramIdx() {
        return histIdx;
    }

    public Long getHistogramIdx(int index) {
        if (index == -1){
            return histIdx != null ? histIdx[histIdx.length - 1] : null;
        }
        return histIdx != null && index < histIdx.length && index >= 0 ? histIdx[index] : null;
    }

//    public List<Integer> getRelevantBinsByIdx(Long lowBinBound, Long highBinBound){
//        List<Integer> relevantBins = new ArrayList<>();
//        for (int i = 0; i < histIdx.length; i++) {
//
//            if (histIdx[i])
//        }
//    }

    public Map<String, Float> getDist() {
        return dist;
    }

    public long getSize() {
        return size;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public Long[] getHistApprxNDistinct() {
        return histApprxNDistinct;
    }

    public Long getHistApprxNDistinct(int index) {
        return index < histApprxNDistinct.length && index >= 0 ? histApprxNDistinct[index] : 1;
    }

    public void updateHistApprxNDistinct(Long value, int index){
        if (index < histApprxNDistinct.length && index >= 0){
            histApprxNDistinct[index] = value;
        }
    }

    public void incrementHistApprxNDistinct(int index){
        if (index < histApprxNDistinct.length && index >= 0){
            histApprxNDistinct[index]++;
        }
    }

    public Float[] getHistApprxFreq() {
        return histApprxFreq;
    }

    public Float getHistApprxFreq(int index) {
        return index < histApprxFreq.length && index >= 0 ? histApprxFreq[index] : 0.0f;
    }

    public void updateHistApprxFreq(Float value, int index){
        if (index < histApprxFreq.length && index >= 0){
            histApprxFreq[index] = value;
        }
    }
}
