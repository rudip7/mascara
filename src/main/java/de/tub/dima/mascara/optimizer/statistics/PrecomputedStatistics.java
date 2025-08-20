package de.tub.dima.mascara.optimizer.statistics;

import java.util.List;

public class PrecomputedStatistics extends AttributeStatistics{

    public PrecomputedStatistics(List<String> tableName, String attname, Double relativeEntropy) {
        super(tableName, attname, 0.0f, null, null, null, 0L);
        this.relativeEntropy = relativeEntropy != null ? relativeEntropy : 0.0;
    }

    public PrecomputedStatistics(List<String> tableName, String attname) {
        this(tableName, attname, 0.0);
    }
}
