package de.tub.dima.mascara.optimizer.iqMetadata;

import de.tub.dima.mascara.optimizer.statistics.AttributeStatistics;
import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;

import java.util.List;

public class AttributeMetadata {
    public List<String> originalTableName;
    public String originalAttname;
    public AttributeStatistics originalStats;
    public int originalIndex;
    public List<String> compliantTableName;
    public String compliantAttname;
    public int compliantIndex;
    public AttributeStatistics compliantStats;

    public AttributeMetadata(List<String> originalTableName, int originalIndex, List<String> compliantTableName, int compliantIndex) {
        StatisticsManager statsManager = StatisticsManager.getInstance();
        this.originalTableName = originalTableName;
        this.originalIndex = originalIndex;
        this.originalStats = statsManager.getAttributeStatistics(originalTableName, originalIndex);
        this.originalAttname = originalStats.getAttname();

        this.compliantTableName = compliantTableName;
        this.compliantIndex = compliantIndex;
        this.compliantStats = statsManager.getAttributeStatistics(originalTableName, compliantIndex);
        this.compliantAttname = compliantStats.getAttname();
    }

    public AttributeMetadata(List<String> originalTableName, String originalAttname, List<String> compliantTableName, String compliantAttname) {
        StatisticsManager statsManager = StatisticsManager.getInstance();
        this.originalTableName = originalTableName;
        this.originalIndex = -1;
        this.originalStats = statsManager.getAttributeStatistics(originalTableName, originalAttname);
        this.originalAttname = originalAttname;

        this.compliantTableName = compliantTableName;
        this.compliantIndex = -1;
        this.compliantStats = statsManager.getAttributeStatistics(originalTableName, compliantAttname);
        this.compliantAttname = compliantAttname;
    }

    public AttributeMetadata(List<String> originalTableName, int originalIndex) {
        StatisticsManager statsManager = StatisticsManager.getInstance();
        this.originalTableName = originalTableName;
        this.originalIndex = originalIndex;
        this.originalStats = statsManager.getAttributeStatistics(originalTableName, originalIndex);
        this.originalAttname = originalStats.getAttname();

        this.compliantTableName = null;
        this.compliantIndex = -1;
        this.compliantStats = null;
        this.compliantAttname = null;
    }

    public AttributeMetadata(List<String> originalTableName, String originalAttname) {
        StatisticsManager statsManager = StatisticsManager.getInstance();
        this.originalTableName = originalTableName;
        this.originalIndex = -1;
        this.originalStats = statsManager.getAttributeStatistics(originalTableName, originalAttname);
        this.originalAttname = originalAttname;

        this.compliantTableName = null;
        this.compliantIndex = -1;
        this.compliantStats = null;
        this.compliantAttname = null;
    }
}
