package de.tub.dima.mascara.optimizer.statistics;

import java.util.List;

public class AttributeStatistics {
    public List<String> tableName;
    public String attname;
    public float n_distinct;
    public String[] most_common_vals;
    public float[] most_common_freqs;
    public String[] histogram_bounds;

    public AttributeStatistics(List<String> tableName, String attname, float n_distinct, String[] most_common_vals, float[] most_common_freqs, String[] histogram_bounds) {
        this.tableName = tableName;
        this.attname = attname;
        this.n_distinct = n_distinct;
        this.most_common_vals = most_common_vals;
        this.most_common_freqs = most_common_freqs;
        this.histogram_bounds = histogram_bounds;
    }

    public String getAttname() {
        return attname;
    }
}
