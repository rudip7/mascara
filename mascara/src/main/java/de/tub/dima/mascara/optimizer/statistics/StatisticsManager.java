package de.tub.dima.mascara.optimizer.statistics;

import de.tub.dima.mascara.DbConnector;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsManager {
    public Map<List<String>, TableStatistics> statisticsCatalog;
    public DbConnector connector;

    private static StatisticsManager instance;

    static {
        instance = new StatisticsManager();
    }

    private StatisticsManager() {
        this.statisticsCatalog = new HashMap<>();
    }

    public static StatisticsManager getInstance() {
        return instance;
    }

    public void setConnector(DbConnector connector) {
        this.connector = connector;
    }

    public TableStatistics getStatistics(List<String> tableName) {
        TableStatistics tableStatistics = statisticsCatalog.get(tableName);
        if (tableStatistics != null){
            return tableStatistics;
        } else {
            try {
                TableStatistics statistics = connector.getStatistics(tableName);
                statisticsCatalog.put(tableName, statistics);
                return statistics;
            } catch (SQLException e) {
                return null;
            }
        }
    }

    public TableStatistics getTableStatistics(List<String> tableName){
        return statisticsCatalog.get(tableName);
    }

    public AttributeStatistics getAttributeStatistics(List<String> tableName, String attname){
        TableStatistics tableStatistics = getTableStatistics(tableName);
        if (tableStatistics != null){
            return tableStatistics.getAttributeStatistics(attname);
        } else {
            return null;
        }
    }

    public AttributeStatistics getAttributeStatistics(List<String> tableName, int index){
        TableStatistics tableStatistics = getTableStatistics(tableName);
        if (tableStatistics != null){
            return tableStatistics.getAttributeStatistics(index);
        } else {
            return null;
        }
    }
}
