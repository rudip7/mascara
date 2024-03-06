package de.tub.dima.mascara.optimizer;

import de.tub.dima.mascara.CompliantPlan;
import de.tub.dima.mascara.DbConnector;
import de.tub.dima.mascara.MascaraMaster;
import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.tpch.alphabets.PhoneAlphabet;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseBlurPhone;
import de.tub.dima.mascara.dataMasking.tpch.maskingFunctions.BlurPhone;
import de.tub.dima.mascara.optimizer.statistics.*;
import de.tub.dima.mascara.policies.AttributeMapping;
import de.tub.dima.mascara.policies.AttributeMappings;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlDialect;

import java.sql.SQLException;
import java.util.*;

public class QualityEstimator {

    public RelNode originalPlan;
    public DbConnector dbConnector;

    public double originalCardinality;

    public QualityEstimator(RelNode originalPlan, DbConnector dbConnector) throws SQLException {
        this.dbConnector = dbConnector;
        this.originalPlan = originalPlan;

//        String query = MascaraMaster.planToSql(originalPlan);
        String cardinalityQuery = convertToCardinalityQuery(originalPlan);
        originalCardinality = dbConnector.estimateCardinality(cardinalityQuery);

    }

    public String convertToCardinalityQuery(RelNode originalPlan){
        RelNode cardinalityPlan = originalPlan;

        while (cardinalityPlan.getInputs().size() > 0){
            if (cardinalityPlan instanceof Project){
                break;
            } else {
                cardinalityPlan = cardinalityPlan.getInput(0);
            }
        }
        return MascaraMaster.planToSql(cardinalityPlan);
    }

    public double estimate(CompliantPlan plan, AttributeMappings queryMappings) throws SQLException {
        double relativeEntropy = 0.0;
        for (AttributeMapping mapping : queryMappings.getRelevantMappings()){
            if (mapping.isMasked()){
                double attributeRelativeEntropy = 0.0;
                if (mapping.getCompliantStats().getRelativeEntropy() >= 0.0){
                    attributeRelativeEntropy = mapping.getCompliantStats().getRelativeEntropy();
                } else
                if (mapping.getCompliantStats() instanceof SuppressedAttributeStatistics && ((SuppressedAttributeStatistics) mapping.getCompliantStats()).isStillSuppressed()){
                    attributeRelativeEntropy = estimateRelativeEntropySuppressed(mapping.getOriginalStats(), mapping.getCompliantStats());
                } else if (mapping.getOriginalStats() instanceof DiscretizedStatistics){
                    attributeRelativeEntropy = estimateRelativeEntropyDiscretized(mapping.getOriginalStats(), mapping.getCompliantStats());
                } else {
                    attributeRelativeEntropy = estimateRelativeEntropy(mapping.getOriginalStats(), mapping.getCompliantStats());
                }
                relativeEntropy += attributeRelativeEntropy;
                plan.addAttributeEntropy(mapping.getOriginalStats().attname, attributeRelativeEntropy);
//                System.out.println(attributeRelativeEntropy+" for attribute: "+mapping.getOriginalStats().attname+" and tables: "+mapping.getOriginalStats().tableName.get(1)+", "+mapping.getCompliantStats().tableName.get(1));
            } else {
                plan.addAttributeEntropy(mapping.getOriginalStats().attname, 0.0);
            }
        }

        Long compliantCardinality = dbConnector.estimateCardinality(plan.getCardinalityQuery());
        plan.setCardinalityDiff(Math.abs(originalCardinality - compliantCardinality)/ originalCardinality);

        double penaltyFactor = (Math.abs(originalCardinality - compliantCardinality) / originalCardinality) + 1.0;
//        System.out.println("Penalty factor: "+penaltyFactor);
        double utilityScore = relativeEntropy * penaltyFactor;
        plan.setUtilityScore(utilityScore);
        return utilityScore;
    }

    private double estimateRelativeEntropyBlurPhone(AttributeStatistics original, AttributeStatistics masked) {
        // TODO: Hardcoded for now, this should be later removed
        double relativeEntropy = 0.0;
        PhoneAlphabet phoneAlphabet = (PhoneAlphabet) AlphabetCatalog.getInstance().getAlphabet("phoneAlphabet");

        double originalAbsFreq = ((DiscretizedStatistics) original).getSumAbsoluteFreq();
        List<String> sortedKeys = new ArrayList<>(masked.getDist().keySet());
        Collections.sort(sortedKeys);

        for (String x : sortedKeys) {
            String xMin = InverseBlurPhone.min(x);
            String xMax = InverseBlurPhone.max(x);
            long indexMin = phoneAlphabet.indexOf(xMin);
            long indexMax = phoneAlphabet.indexOf(xMax);

            Float qX = masked.getFreq(x) / 10000000;

            int minBucketIdx = original.getBucketIdx(xMin);
            int maxBucketIdx = original.getBucketIdx(xMax);
            // Value not in Histogram
            if (minBucketIdx == -1){
                minBucketIdx = 0;
            }
            if (maxBucketIdx == -1){
                maxBucketIdx = original.getHistogramBounds().length - 2;
            }

            for (int i = minBucketIdx; i < maxBucketIdx; i++) {
                Long oglLowIdxBound = original.getHistogramIdx(i);
                Long oglHighIdxBound = original.getHistogramIdx(i + 1);
                double pX = original.getHistApprxFreq(i) / originalAbsFreq;
                if (indexMin > oglLowIdxBound && indexMax < oglHighIdxBound){
                    // Masked values within bucket
                    relativeEntropy += (indexMax - indexMin) * pX * log(pX / qX);
                } else if (indexMin > oglLowIdxBound){
                    // Masked values partially within bucket (low)
                    relativeEntropy += (oglHighIdxBound - indexMin) * pX * log(pX / qX);
                } else if (indexMax < oglHighIdxBound){
                    // Masked values partially within bucket (high)
                    relativeEntropy += (indexMax - oglLowIdxBound) * pX * log(pX / qX);
                } else {
                    // Bucket should be completely included
                    relativeEntropy += (oglHighIdxBound - oglLowIdxBound) * pX * log(pX / qX);
                }
            }
        }

        return relativeEntropy;
    }

//    private void convertMCVtoHist(DiscretizedAttributeStatistics masked) {
//        ArrayList<String> sortedKeys = new ArrayList<>(masked.getDist().keySet());
//        Collections.sort(sortedKeys);
//
//        masked.histogramBounds = new String[sortedKeys.size() + 1];
//        float[] histApprxFreq = new float[sortedKeys.size()];
//        for (int i = 0; i < sortedKeys.size(); i++) {
//            masked.histogramBounds[i] = sortedKeys.get(i);
//            histApprxFreq[i] = masked.getFreq(sortedKeys.get(i));
//        }
//        masked.histogramBounds[sortedKeys.size()] = InverseBlurPhone.max(sortedKeys.get(sortedKeys.size()-1));
//
//        masked.mostCommonVals = null;
//        masked.dist = null;
//        masked.restFreq = 1.0f;
//
//        masked.discretizeStatistics();
//
//    }

    public double estimateRelativeEntropySuppressed(AttributeStatistics original, AttributeStatistics masked) {
        double relativeEntropy = 0.0;
        double qX = 1.0 / (original.getSize() * original.getnDistinct());

        if (original.getnDistinct() == original.getSize()){
            double pX = 1.0 / original.getSize();

            return original.getnDistinct() * pX * log(pX / qX);
        }

        int mcv = original.getDist().keySet().size();
        for (String x : original.getDist().keySet()) {
            Float pX = original.getFreq(x) / original.getSize();
            relativeEntropy += pX * log(pX / qX);
        }
        if (original.getnDistinct() > mcv){
            long restDistinct = original.getnDistinct() - mcv;
            Float pX = original.getRestFreq() / restDistinct;
            relativeEntropy += restDistinct * pX * log(pX / qX);
        }

        masked.setRelativeEntropy(relativeEntropy);

        return relativeEntropy;
    }

    public double estimateRelativeEntropyNDistinct(AttributeStatistics original, AttributeStatistics masked){
        double pX = 1.0 / original.getnDistinct();
        double qX = 1.0 / masked.getnDistinct();

        return original.getnDistinct() * pX * log(pX / qX);
    }

        /**
         * Estimate relative entropy for discretized continuous domains.
         * @param original
         * @param masked
         * @return
         */
    public double estimateRelativeEntropyDiscretized(AttributeStatistics original, AttributeStatistics masked) {
        if (original.getnDistinct() > original.getSize() * 0.99){
            return estimateRelativeEntropyNDistinct(original, masked);
        }

        // Create a union of the most common values
        Set<String> values = new HashSet<>();
        values.addAll(original.getDist().keySet());
        values.addAll(masked.getDist().keySet());

        long notFoundInOriginal = 0;
        long notFoundInMasked = 0;

        // Count missing values in MCV that are not contained in the equi-depths histograms
        for (String x : values) {
            Float pX = original.getFreq(x);
            if (pX == null) pX = 0.0f;

            Float qX = masked.getFreq(x);
            if (qX == null) qX = 0.0f;

            if (pX > 0.0 && qX == 0.0 && !masked.isValueInHistogram(x) && !original.isValueInHistogram(x)) {
                notFoundInMasked++;
            }

            if (pX == 0.0 && qX > 0.0 && !masked.isValueInHistogram(x) && !original.isValueInHistogram(x)) {
                notFoundInOriginal++;
            }
        }

        // Check when histograms stop to overlap and add difference to either notFoundInOriginal or notFoundInMasked
        if (original.hasHistogram() && masked.hasHistogram()) {
            Long originalHistLow = original.getHistogramIdx(0);
            Long maskedHistLow = masked.getHistogramIdx(0);
            Long lowDiff = originalHistLow - maskedHistLow;
            if (lowDiff < 0) {
                // original lower bound is smaller than masked lower bound:
                // add difference to notFoundInMasked
                notFoundInMasked += Math.abs(lowDiff);
            } else if (lowDiff > 0) {
                // masked lower bound is smaller than original lower bound:
                // add difference to notFoundInOriginal
                notFoundInOriginal += lowDiff;
            }

            Long originalHistHigh = original.getHistogramIdx(-1);
            Long maskedHistHigh = masked.getHistogramIdx(-1);
            Long highDiff = originalHistHigh - maskedHistHigh;
            if (highDiff > 0) {
                // original higher bound is bigger than masked higher bound:
                // add difference to notFoundInMasked
                notFoundInMasked += highDiff;
            } else if (highDiff < 0) {
                // masked higher bound is bigger than original higher bound:
                // add difference to notFoundInOriginal
                notFoundInOriginal += Math.abs(highDiff);
            }
        }

        double originalAbsFreq = ((DiscretizedStatistics) original).getSumAbsoluteFreq() + notFoundInOriginal;
        double maskedAbsFreq = ((DiscretizedStatistics) masked).getSumAbsoluteFreq() + notFoundInMasked;

        double relativeEntropy = 0.0;

        int[] originalFoundInHist = original.hasHistogram() ? new int[original.getHistogramBounds().length - 1] : null;
        int[] maskedFoundInHist = masked.hasHistogram() ? new int[masked.getHistogramBounds().length - 1] : null;

        double sumPX = 0.0;
        for (String x : values) {
            Double pX = original.getFreq(x) / originalAbsFreq;
            Double qX = masked.getFreq(x) / maskedAbsFreq;

            if (pX > 0.0 && qX == 0.0) {
                // Case MCV is present in original but not in masked
                int bucketIdx = masked.getBucketIdx(x);
                if (bucketIdx >= 0) {
                    // Value is within a bucket in masked histogram
                    qX = masked.getHistApprxFreq(bucketIdx) / maskedAbsFreq;
                    maskedFoundInHist[bucketIdx]++;
                } else {
                    // Value can not be found in the masked histogram
                    qX = 1.0 / maskedAbsFreq;
                }
            } else if (pX == 0.0 && qX > 0.0) {
                // Case MCV is present in masked but not in original
                int bucketIdx = original.getBucketIdx(x);
                if (bucketIdx >= 0) {
                    // Value is within a bucket in original histogram
                    pX = original.getHistApprxFreq(bucketIdx) / originalAbsFreq;
                    originalFoundInHist[bucketIdx]++;
                } else {
                    // Value can not be found in the masked histogram
                    pX = 1.0 / originalAbsFreq;
                }
            }

            if (pX > 0.0 && qX > 0.0) {
                sumPX += pX;
                relativeEntropy += pX * log(pX / qX);
            } else {
                System.out.println("This should not happen");
            }
        }

        // Compare elements in histograms
        // Case 1: Equi-depth histograms available for both attributes.
        if (original.getRestFreq() > 0.1) {
            if (original.hasHistogram() && masked.hasHistogram()) {
                int mskBin = 0;
                int oglBin = 0;
                Long oglLowIdxBound = original.getHistogramIdx(oglBin);
                Long oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                double pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                double minPX = 1.0 / originalAbsFreq;

                Long mskLowIdxBound = masked.getHistogramIdx(mskBin);
                Long mskHighIdxBound = masked.getHistogramIdx(mskBin + 1);
                double qX = masked.getHistApprxFreq(mskBin) / maskedAbsFreq;
                double minQX = 1.0 / maskedAbsFreq;

                while (oglBin < original.getHistogramIdx().length - 1 && mskBin < masked.getHistogramIdx().length - 1) {
                    if (oglLowIdxBound > mskLowIdxBound) {
                        // masked low bound is smaller than the original low bound (cases 1 and 2)
                        if (oglBin == 0 && mskHighIdxBound <= oglLowIdxBound) {
                            // masked bin completely out of original histogram
                            relativeEntropy += (mskHighIdxBound - mskLowIdxBound) * (minPX * log(minPX / qX));
                            mskBin++;
                            if (mskBin < masked.getHistogramIdx().length - 2) {
                                mskLowIdxBound = masked.getHistogramIdx(mskBin);
                                mskHighIdxBound = masked.getHistogramIdx(mskBin + 1);
                                qX = masked.getHistApprxFreq(mskBin) / maskedAbsFreq;
                            }
                            continue;
                        }
                        if (oglBin == 0) {
                            // special case for the first bin
                            relativeEntropy += (oglLowIdxBound - mskLowIdxBound) * (minPX * log(minPX / qX));
                        }
                        if (oglHighIdxBound >= mskHighIdxBound) {
                            // Case 1:
                            // masked low bound is smaller than the original low bound
                            // masked high bound is also smaller than the original high bound
                            //     |--------|
                            // |--------|
                            relativeEntropy += (mskHighIdxBound - oglLowIdxBound) * (pX * log(pX / qX));
                            if (oglHighIdxBound.equals(mskHighIdxBound)) {
                                // Original bin has also been completed
                                oglBin++;
                                if (oglBin < original.getHistogramIdx().length - 2) {
                                    oglLowIdxBound = original.getHistogramIdx(oglBin);
                                    oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                                    pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                                }
                            }
                            // Masked bin has been completed
                            mskBin++;
                            if (mskBin < masked.getHistogramIdx().length - 2) {
                                mskLowIdxBound = masked.getHistogramIdx(mskBin);
                                mskHighIdxBound = masked.getHistogramIdx(mskBin + 1);
                                qX = masked.getHistApprxFreq(mskBin) / maskedAbsFreq;
                            }
                        } else {
                            // Case 2:
                            // masked low bound is smaller than the original low bound
                            // masked high bound is bigger than the original high bound
                            //     |--------|
                            // |--------------|
                            if (oglBin == 0) {
                                // special case for the last bin
                                relativeEntropy += (oglLowIdxBound - mskLowIdxBound) * (minPX * log(minPX / qX));
                            }
                            relativeEntropy += (oglHighIdxBound - oglLowIdxBound) * (pX * log(pX / qX));
                            // Original bin has been completed
                            oglBin++;
                            if (oglBin < original.getHistogramIdx().length - 2) {
                                oglLowIdxBound = original.getHistogramIdx(oglBin);
                                oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                                pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                            }
                        }
                    } else {
                        // masked low bound is bigger than the original low bound (cases 3 and 4)
                        if (mskBin == 0 && oglHighIdxBound <= mskLowIdxBound) {
                            // original bin completely out of masked histogram
                            relativeEntropy += (oglHighIdxBound - oglLowIdxBound) * (pX * log(pX / minQX));
                            oglBin++;
                            if (oglBin < original.getHistogramIdx().length - 2) {
                                oglLowIdxBound = original.getHistogramIdx(oglBin);
                                oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                                pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                            }
                            continue;
                        }
                        if (mskBin == 0) {
                            // special case for the first bin
                            relativeEntropy += (mskLowIdxBound - oglLowIdxBound) * (pX * log(pX / minQX));
                        }
                        if (oglHighIdxBound >= mskHighIdxBound) {
                            // Case 3:
                            // masked low bound is bigger than the original low bound
                            // masked high bound is smaller than the original high bound
                            //     |--------|
                            //       |----|
                            relativeEntropy += (mskHighIdxBound - mskLowIdxBound) * (pX * log(pX / qX));

                            if (oglHighIdxBound.equals(mskHighIdxBound)) {
                                // Original bin has also been completed
                                oglBin++;
                                if (oglBin < original.getHistogramIdx().length - 2) {
                                    oglLowIdxBound = original.getHistogramIdx(oglBin);
                                    oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                                    pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                                }
                            }

                            // Masked bin has been completed
                            mskBin++;
                            if (mskBin < masked.getHistogramIdx().length - 2) {
                                mskLowIdxBound = masked.getHistogramIdx(mskBin);
                                mskHighIdxBound = masked.getHistogramIdx(mskBin + 1);
                                qX = masked.getHistApprxFreq(mskBin) / maskedAbsFreq;
                            }
                        } else {
                            // Case 4:
                            // masked low bound is bigger than the original low bound
                            // masked high bound is bigger than the original high bound
                            //     |--------|
                            //       |---------|
                            relativeEntropy += (oglHighIdxBound - mskLowIdxBound) * (pX * log(pX / qX));
                            // Original bin has been completed
                            oglBin++;
                            if (oglBin < original.getHistogramIdx().length - 2) {
                                oglLowIdxBound = original.getHistogramIdx(oglBin);
                                oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                                pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                            }
                        }
                    }
                }
                if (mskBin == masked.getHistogramIdx().length - 1 && oglBin < original.getHistogramIdx().length - 1) {
                    // Masked histogram was completely consumed
                    // compute rest of current bin
                    relativeEntropy += (oglHighIdxBound - mskHighIdxBound) * (pX * log(pX / qX));

                    oglBin++;
                    if (oglBin < original.getHistogramIdx().length - 2) {
                        oglLowIdxBound = original.getHistogramIdx(oglBin);
                        oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                        pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                    }

                    // Compute relative entropy for the last bins
                    while (oglBin < original.getHistogramIdx().length - 2) {
                        relativeEntropy += (oglHighIdxBound - oglLowIdxBound) * (pX * log(pX / minQX));
                        oglBin++;
                        if (oglBin < original.getHistogramIdx().length - 2) {
                            oglLowIdxBound = original.getHistogramIdx(oglBin);
                            oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                            pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                        }

                    }
                } else if (mskBin < masked.getHistogramIdx().length - 1 && oglBin == original.getHistogramIdx().length - 1) {
                    // Original histogram was completely consumed
                    // compute rest of current bin
                    relativeEntropy += (mskHighIdxBound - oglHighIdxBound) * (pX * log(pX / qX));

                    mskBin++;
                    if (mskBin < masked.getHistogramIdx().length - 2) {
                        mskLowIdxBound = masked.getHistogramIdx(mskBin);
                        mskHighIdxBound = masked.getHistogramIdx(mskBin + 1);
                        qX = masked.getHistApprxFreq(mskBin) / maskedAbsFreq;
                    }

                    // Compute relative entropy for the last bins
                    while (mskBin < masked.getHistogramIdx().length - 2) {
                        relativeEntropy += (mskHighIdxBound - mskLowIdxBound) * (minPX * log(minPX / minQX));
                        mskBin++;
                        if (mskBin < masked.getHistogramIdx().length - 2) {
                            mskLowIdxBound = masked.getHistogramIdx(mskBin);
                            mskHighIdxBound = masked.getHistogramIdx(mskBin + 1);
                            qX = masked.getHistApprxFreq(mskBin) / maskedAbsFreq;
                        }
                    }
                }

            } else if (original.hasHistogram() && !masked.hasHistogram()) {
                // Case 2: Equi-depth histogram available for the original attribute.
                int oglBin = 0;
//                Long oglLowIdxBound = original.getHistogramIdx(oglBin);
//                Long oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                double pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;

                double qX;
                if (masked.getRestFreq() > 0.0) {
                    qX = Math.round(((DiscretizedStatistics) masked).getSumAbsoluteFreq() * masked.getRestFreq()) / maskedAbsFreq;
                } else {
                    qX = 1.0 / maskedAbsFreq;
                }


                while (oglBin < original.getHistogramIdx().length - 1) {
                    long remainingOgl = original.getHistApprxNDistinct(oglBin) - originalFoundInHist[oglBin];
                    if (remainingOgl > 0){
                        relativeEntropy += remainingOgl * (pX * log(pX / qX));
                    }
                    oglBin++;
                    if (oglBin < original.getHistogramIdx().length - 2) {
//                        oglLowIdxBound = original.getHistogramIdx(oglBin);
//                        oglHighIdxBound = original.getHistogramIdx(oglBin + 1);
                        pX = original.getHistApprxFreq(oglBin) / originalAbsFreq;
                    }
                }

            } else if (!original.hasHistogram() && masked.hasHistogram()) {
                // Case 3: Equi-depth histogram available for the masked attribute.

                int mskBin = 0;
//                Long mskLowIdxBound = masked.getHistogramIdx(mskBin);
//                Long mskHighIdxBound = masked.getHistogramIdx(mskBin + 1);
                double qX = masked.getHistApprxFreq(mskBin) / maskedAbsFreq;

                double pX = Math.round(((DiscretizedStatistics) original).getSumAbsoluteFreq() * original.getRestFreq()) / originalAbsFreq;
                while (mskBin < masked.getHistogramIdx().length - 1) {
                    long remainingMsk = masked.getHistApprxNDistinct(mskBin) - maskedFoundInHist[mskBin];
                    if (remainingMsk > 0){
                        relativeEntropy += remainingMsk * (pX * log(pX / qX));
                    }
                    mskBin++;
                    if (mskBin < masked.getHistogramIdx().length - 2) {
//                        mskLowIdxBound = original.getHistogramIdx(mskBin);
//                        mskHighIdxBound = original.getHistogramIdx(mskBin + 1);
                        qX = masked.getHistApprxFreq(mskBin) / maskedAbsFreq;
                    }
                }

            }
        }
        relativeEntropy = relativeEntropy < 0 ? relativeEntropy * -1 : relativeEntropy;
        masked.setRelativeEntropy(relativeEntropy);
        return relativeEntropy;
    }

    /**
     * Base case when estimating the relative entropy between the original and
     * the masked attribute.
     * @param original
     * @param masked
     * @return
     */
    public double estimateRelativeEntropy(AttributeStatistics original, AttributeStatistics masked){
        // Create a union of the most common values
        Set<String> values = new HashSet<>();
        values.addAll(original.getDist().keySet());
        values.addAll(masked.getDist().keySet());

        // Phase 1: Compute relative entropy using only the most common values, which have exact frequencies.
        // TODO: rename variables (maybe)
        int count = 0;
        double cumulativeFreq = 0.0;
        double relativeEntropy = 0.0;

        List<String> toRecompute = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();

        for (String x : values) {
            Float pX = original.getFreq(x);

            Float qX = masked.getFreq(x);

            if (pX > 0.0 && qX == 0.0){
                toRecompute.add(x);
            }

            if (pX == 0.0 && qX > 0.0){
                count++;
                cumulativeFreq += qX;
                toAdd.add(x);
            }

            if (pX > 0.0 && qX > 0.0){
                relativeEntropy += pX * log(pX / qX);
            }
        }

        // Phase 2: Estimate the relative entropy for missing values.
        // Case 1: Equi-depth histograms available for both attributes.
        if (original.getHistApprxFreq() != null && masked.getHistApprxFreq() != null) {
            // add values from the masked distribution that were missing in the original into the equi-depth histogram
            for (String x : toAdd) {
                int binIdx = masked.getBucketIdx(x);
                float freqSum = (masked.getHistApprxFreq(binIdx) * masked.getHistApprxNDistinct(binIdx))
                        + masked.getFreq(x);
                masked.incrementHistApprxNDistinct(binIdx);
                masked.updateHistApprxFreq(freqSum / masked.getHistApprxNDistinct(binIdx), binIdx);
            }

            // recompute entropy for missing values with exact frequency in the original stats and no entry in
            // the masked stats
            for (String x : toRecompute) {
                float pX = original.getDist().get(x);
                int binIdx = masked.getBucketIdx(x);
                float approxQX = masked.getHistApprxFreq(binIdx);
                relativeEntropy += pX * log(pX / approxQX);
            }
            if (original.getRestFreq() > 0.1) {
                // compute entropy of elements with approximate frequencies
                for (int oglBinIdx = 0; oglBinIdx < original.getHistogramBounds().length - 1; oglBinIdx++) {
                    String lowBinBound = original.getHistogramBound(oglBinIdx);
                    String highBinBound = original.getHistogramBound(oglBinIdx + 1);
                    float approxPX = original.getHistApprxFreq(oglBinIdx);

                    int mskBinIdxLow = masked.getBucketIdx(lowBinBound);
                    int mskBinIdxHigh = masked.getBucketIdx(highBinBound);

                    // check if only one bin is required
                    if (mskBinIdxLow == mskBinIdxHigh) {
                        float approxQX = masked.getHistApprxFreq(mskBinIdxLow);
                        relativeEntropy +=
                                original.getHistApprxNDistinct(oglBinIdx) * (approxPX * log(approxPX / approxQX));
                    } else {
                        long[] possibleNDistinct = new long[mskBinIdxHigh + 1 - mskBinIdxLow];
                        // compute number of possible values in first and last bin
                        possibleNDistinct[0] =
                                original.getAlphabet().indexOf(masked.getHistogramBound(mskBinIdxLow + 1)) -
                                        original.getAlphabet().indexOf(lowBinBound);
                        possibleNDistinct[possibleNDistinct.length - 1] = original.getAlphabet().indexOf(highBinBound) -
                                original.getAlphabet().indexOf(masked.getHistogramBound(mskBinIdxHigh));
                        // compute number of possible values in middle bins
                        for (int i = 1, idx = mskBinIdxLow + 1; i < possibleNDistinct.length - 1; i++, idx++) {
                            possibleNDistinct[i] =
                                    original.getAlphabet().indexOf(masked.getHistogramBound(idx + 1)) -
                                            original.getAlphabet().indexOf(masked.getHistogramBound(idx));
                        }
                        // compute relative portion of distinct values in each bin
                        double totalPossibleNDistinct = 0.0;
                        for (int i = 0; i < possibleNDistinct.length; i++) {
                            totalPossibleNDistinct += possibleNDistinct[i];
                        }
                        double[] relPossibleNDistinct = new double[possibleNDistinct.length];
                        for (int i = 0; i < possibleNDistinct.length; i++) {
                            relPossibleNDistinct[i] = possibleNDistinct[i] / totalPossibleNDistinct;
                        }

                        for (int i = 0, idx = mskBinIdxLow; i < possibleNDistinct.length; i++, idx++) {
                            double approxQX = masked.getHistApprxFreq(idx);
                            double apprxNDistinctBin = relPossibleNDistinct[i] * original.getHistApprxNDistinct(oglBinIdx);
                            relativeEntropy += apprxNDistinctBin * (approxPX * log(approxPX / approxQX));
                        }
                    }
                }
            }
        } else {
            double maskedRestFreq = masked.getRestFreq() + cumulativeFreq;
            double maskedNAppxVals = masked.getnDistinct() - masked.getDist().size() + count;
            double maskedAppxFreq = maskedRestFreq / maskedNAppxVals;

            // Recompute entropy for missing values with exact frequency in the original stats and no entry in the masked stats
            for (String x : toRecompute) {
                double pX = original.getDist().get(x);
                relativeEntropy += pX * log(pX / maskedAppxFreq);
            }

            if (original.getRestFreq() > 0.1){
                // Case 2: Equi-depth histogram available only for the original attribute.
                if (original.getHistApprxFreq() != null && masked.getHistApprxFreq() == null) {
                    // Compute entropy of elements with approximate frequencies
                    for (int oglBinIdx = 0; oglBinIdx < original.getHistApprxFreq().length; oglBinIdx++) {
                        double approxPX = original.getHistApprxFreq(oglBinIdx);
                        relativeEntropy += original.getHistApprxNDistinct()[oglBinIdx] * (approxPX * log(approxPX / maskedAppxFreq));
                    }

                    // Case 3: No Equi-depth histograms available.
                } else if (original.getHistApprxFreq() == null && masked.getHistApprxFreq() == null){
                    // Compute entropy of elements with approximate frequencies
                    double originalRestFreq = original.getRestFreq();
                    double originalNAppxVals = original.getnDistinct() - original.getDist().size();
                    double originalAppxFreq = originalRestFreq / originalNAppxVals;

                    if (originalAppxFreq > 0.0001 && maskedAppxFreq > 0.0001) {
                        relativeEntropy += originalNAppxVals * (originalAppxFreq * log(originalAppxFreq / maskedAppxFreq));
                    }
                }
            }
        }


        relativeEntropy = relativeEntropy < 0 ? relativeEntropy * -1 : relativeEntropy;
        masked.setRelativeEntropy(relativeEntropy);
        return relativeEntropy;
    }
    
    private double log(double value){
        return Math.log(value) / Math.log(2);
    }
}
