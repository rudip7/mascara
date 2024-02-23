package de.tub.dima.mascara.optimizer.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tub.dima.mascara.DbConnector;
import net.minidev.json.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class StatisticsManager {
    public Map<List<String>, TableStatistics> statisticsCatalog;
    public Map<List<String>, Long> tableSizes;
    public DbConnector connector;

    public Map<List<String>, Map<String, Double>> relativeEntropies;

    private static StatisticsManager instance;

    static {
        instance = new StatisticsManager();
    }

    private StatisticsManager() {
        this.statisticsCatalog = new HashMap<>();
        this.tableSizes = new HashMap<>();

//        fromJSON("src/main/resources/relativeEntropies/tpch.json");
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
                Long tableSize = getTableSize(tableName);
                if (relativeEntropies != null){
                    Map<String, Double> precomputedStats = relativeEntropies.get(tableName);
                    if (precomputedStats != null){
                        tableStatistics = new TableStatistics(tableName, tableSize);
                        List<String> attributeNames = connector.getAttributeNames(tableName);
                        for (String attname : attributeNames){
                            tableStatistics.addPrecomputedStatistics(attname, precomputedStats.get(attname));
                        }
                        statisticsCatalog.put(tableName, tableStatistics);
                        return tableStatistics;
                    }
                }
                TableStatistics statistics = connector.getStatistics(tableName, tableSize);
                statisticsCatalog.put(tableName, statistics);
                return statistics;
            } catch (SQLException e) {
                return null;
            }
        }
    }

    public void fromJSON(String jsonFilePath){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Load the JSON file from the resources folder
            InputStream inputStream = JSONParser.class.getClassLoader().getResourceAsStream("relativeEntropies/tpch.json");
            JsonNode rootNode = objectMapper.readTree(inputStream);

            relativeEntropies = new HashMap<>();
            parseJSON(rootNode, new ArrayList<>(), relativeEntropies);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseJSON(JsonNode node, List<String> currentPath, Map<List<String>, Map<String, Double>> resultMap) {
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            currentPath.add(key);

            if (value.isObject()) {
                parseJSON(value, new ArrayList<>(currentPath), resultMap);
            } else if (value.isTextual()) {
                List<String> tableName = new ArrayList<>(currentPath.subList(0, 2));
                Map<String, Double> attributes = resultMap.computeIfAbsent(tableName, k -> new HashMap<>());
                attributes.put(currentPath.get(2), Double.valueOf(value.asText()));
            }

            currentPath.remove(currentPath.size() - 1);
        }
    }



    public Long getTableSize(List<String> tableName) {
        Long size = tableSizes.get(tableName);
        if (size != null){
            return size;
        } else {
            try {
                size = connector.getTableSize(tableName);
                tableSizes.put(tableName, size);
                return size;
            } catch (SQLException e) {
                return -1L;
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

    public AttributeStatistics setAttributeStatistics(List<String> tableName, String attname, AttributeStatistics newStatistics){
        TableStatistics tableStatistics = getTableStatistics(tableName);
        if (tableStatistics != null){
            tableStatistics.setAttributeStatistics(attname, newStatistics);
            return newStatistics;
        }
        return null;
    }

    public AttributeStatistics setAttributeStatistics(List<String> tableName, int index, AttributeStatistics newStatistics){
        TableStatistics tableStatistics = getTableStatistics(tableName);
        if (tableStatistics != null){
            tableStatistics.setAttributeStatistics(index, newStatistics);
            return newStatistics;
        }
        return null;
    }
}
