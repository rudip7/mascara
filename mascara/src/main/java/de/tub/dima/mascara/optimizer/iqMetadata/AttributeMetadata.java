package de.tub.dima.mascara.optimizer.iqMetadata;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.optimizer.statistics.AttributeStatistics;
import de.tub.dima.mascara.optimizer.statistics.MaskedAttributeStatistics;
import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;

import java.util.List;

public class AttributeMetadata {
    public List<String> tableName;
    public String attname;
    public int index;
    public AttributeStatistics stats = null;
    public boolean groupingAttribute = false;
    public boolean aggregate = false;
    public MaskingFunction maskingFunction = null;

    public AttributeMetadata(List<String> tableName, int index, String attname) {
        this.tableName = tableName;
        this.index = index;
        this.attname = attname;
        this.maskingFunction = null;
        triggerEstimateHistFreq();
    }

    public AttributeMetadata(List<String> tableName, int index) {
        this.tableName = tableName;
        this.index = index;
        this.attname = null;
        this.maskingFunction = null;
        triggerEstimateHistFreq();
    }

    public AttributeMetadata(List<String> tableName, int index, String attname, MaskingFunction maskingFunction) {
        this.tableName = tableName;
        this.index = index;
        this.attname = attname;
        this.maskingFunction = maskingFunction;
        triggerEstimateHistFreq();
    }

    public AttributeMetadata(List<String> tableName, int index, MaskingFunction maskingFunction) {
        this.tableName = tableName;
        this.index = index;
        this.attname = null;
        this.maskingFunction = maskingFunction;
        triggerEstimateHistFreq();
    }


    public MaskingFunction getMaskingFunction() {
        return maskingFunction;
    }

    public void setMaskingFunction(MaskingFunction maskingFunction) {
        this.maskingFunction = maskingFunction;
    }

    public void triggerEstimateHistFreq(){
        getStats();
        if(stats != null){
            if (this.maskingFunction != null && this.maskingFunction instanceof Generalization && ((Generalization) this.maskingFunction).getInverseMaskingFunction() != null){
                if (!(stats instanceof MaskedAttributeStatistics)){
                    System.out.println("This was not expected");
//                    StatisticsManager statsManager = StatisticsManager.getInstance();
//                    MaskedAttributeStatistics maskedStatistics = new MaskedAttributeStatistics(stats);
//                    this.stats = this.index >= 0 ? statsManager.setAttributeStatistics(tableName, index, maskedStatistics) : statsManager.setAttributeStatistics(tableName, attname, maskedStatistics);
                }
                ((MaskedAttributeStatistics) stats).unmaskStatistics(((Generalization) this.maskingFunction).getInverseMaskingFunction());
            } else {
                stats.estimateHistFreq(false);
            }
        }
    }

    public List<String> getTableName() {
        return tableName;
    }

    public String getAttname() {
        if (attname == null && getStats() != null){
            this.attname = stats.getAttname();
        }
        return attname;
    }

    public int getIndex() {
        return index;
    }

    public AttributeStatistics getStats() {
        if (stats == null){
            StatisticsManager statsManager = StatisticsManager.getInstance();
            this.stats = this.index >= 0 ? statsManager.getAttributeStatistics(tableName, index) : statsManager.getAttributeStatistics(tableName, attname);
        }
        return stats;
    }

    public boolean isGroupingAttribute() {
        return groupingAttribute;
    }

    public boolean isAggregate() {
        return aggregate;
    }

    public void setGrouping() {
        this.groupingAttribute = true;
    }
}
