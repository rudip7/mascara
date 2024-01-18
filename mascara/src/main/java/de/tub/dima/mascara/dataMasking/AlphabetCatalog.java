package de.tub.dima.mascara.dataMasking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tub.dima.mascara.dataMasking.medical.alphabets.DiagnosisAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.IntegerAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.PhoneAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.ZipAlphabet;
import de.tub.dima.mascara.dataMasking.tpch.alphabets.DateAlphabet;
import de.tub.dima.mascara.dataMasking.tpch.alphabets.FloatAlphabet;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AlphabetCatalog {

    public Map<String, Alphabet> alphabets;

    public Map<List<String>, String> alphabetMapping;

    private static AlphabetCatalog instance;

    static {
        instance = new AlphabetCatalog();
    }

    public static AlphabetCatalog getInstance() {
        return instance;
    }


    private AlphabetCatalog() {
        DateAlphabet dateAlphabet = new DateAlphabet();
        IntegerAlphabet integerAlphabet = new IntegerAlphabet();
        IntegerAlphabet discretizedIntegerAlphabet = new IntegerAlphabet(true);
        FloatAlphabet floatAlphabet = new FloatAlphabet();

        this.alphabets = new HashMap<>();
        this.alphabets.put("dateAlphabet", dateAlphabet);
        this.alphabets.put("integerAlphabet", integerAlphabet);
        this.alphabets.put("discretizedIntegerAlphabet", discretizedIntegerAlphabet);
        this.alphabets.put("floatAlphabet", floatAlphabet);

        fromJSON("src/main/resources/alphabets/tpch.json");
    }

    public void fromJSON(String jsonFilePath){
        try {
            File jsonFile = new File(jsonFilePath);
            if (jsonFile.exists()) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonFile);
                this.alphabetMapping = new HashMap<>();
                parseJSON(rootNode, new ArrayList<>(), this.alphabetMapping);
            } else {
                System.err.println("JSON file not found at the specified path.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseJSON(JsonNode node, List<String> currentPath, Map<List<String>, String> resultMap) {
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            currentPath.add(key);

            if (value.isObject()) {
                parseJSON(value, currentPath, resultMap);
            } else if (value.isTextual()) {
                resultMap.put(new ArrayList<>(currentPath), value.asText());
            }

            currentPath.remove(currentPath.size() - 1);
        }
    }

    public void copyAlphabet(List<String> tableName, String attname, List<String> newTableName, String newAttname){
        List<String> copy = new ArrayList<>(tableName);
        copy.add(attname);
        String alphabetName = alphabetMapping.get(copy);
        if (alphabetName != null){
            List<String> newAtt = new ArrayList<>(newTableName);
            newAtt.add(newAttname);
            alphabetMapping.put(newAtt, alphabetName);
        }
    }


    public Alphabet getAlphabet(List<String> tableName, String attname){
        List<String> copy = new ArrayList<>(tableName);
        copy.add(attname);
        String alphabetName = alphabetMapping.get(copy);
        return alphabetName != null ? alphabets.get(alphabetName) : null;
    }

    public Alphabet getAlphabet(String alphabetName){
        return alphabets.get(alphabetName);
    }
}
