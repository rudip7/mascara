package de.tub.dima.mascara.optimizer.statistics;

import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableStatistics {
    public List<String> tableName;
    public Map<String, AttributeStatistics> attributeStatistics;
    public Map<Integer, String> attributeIndices;

    public long size;

    public TableStatistics(List<String> tableName, long size) {
        this.tableName = tableName;
        this.attributeStatistics = new HashMap<>();
        this.attributeIndices = new HashMap<>();
        this.size = size;
    }

    public void addAttributeStatistics(String attname, float n_distinct, String[] most_common_vals, Float[] most_common_freqs, String[] histogram_bounds){
        addAttributeStatistics(attname, n_distinct, most_common_vals, most_common_freqs, histogram_bounds,false);
    }

    public void addAttributeStatistics(String attname, float n_distinct, String[] most_common_vals, Float[] most_common_freqs, String[] histogram_bounds, boolean force){
        if (force || this.attributeStatistics.get(attname) == null){
            Alphabet alphabet = AlphabetCatalog.getInstance().getAlphabet(tableName, attname);
            if (alphabet != null && alphabet instanceof DiscretizedAlphabet && ((DiscretizedAlphabet) alphabet).shouldDiscretize()){
                this.attributeStatistics.put(attname, new DiscretizedAttributeStatistics(tableName, attname, n_distinct, most_common_vals, most_common_freqs, histogram_bounds, this.size));
            } else {
                this.attributeStatistics.put(attname, new AttributeStatistics(tableName, attname, n_distinct, most_common_vals, most_common_freqs, histogram_bounds, this.size));
            }
        }
    }

    public void indexAttribute(int index, String attname){
        attributeIndices.put(index, attname);
    }

    public AttributeStatistics getAttributeStatistics(String attname){
        return attributeStatistics.get(attname);
    }

    public AttributeStatistics getAttributeStatistics(int index){
        return attributeStatistics.get(attributeIndices.get(index));
    }

    public void setAttributeStatistics(String attname, AttributeStatistics newStats){
        attributeStatistics.put(attname, newStats);
    }

    public void setAttributeStatistics(int index, AttributeStatistics newStats){
        if (attributeIndices.get(index) != null){
            attributeStatistics.put(attributeIndices.get(index), newStats);
        }
    }

    public void setSize(long size) {
        this.size = size;
        for (AttributeStatistics stat : attributeStatistics.values()) {
            stat.setSize(size);
        }
    }
}
