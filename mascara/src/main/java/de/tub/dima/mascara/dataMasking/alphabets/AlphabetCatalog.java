package de.tub.dima.mascara.dataMasking.alphabets;

import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        DiagnosisAlphabet diagnosisAlphabet = new DiagnosisAlphabet();
        ZipAlphabet zipAlphabet = new ZipAlphabet();
        IntegerAlphabet integerAlphabet = new IntegerAlphabet(0, 250);
        PhoneAlphabet phoneAlphabet = new PhoneAlphabet();

        this.alphabets = new HashMap<>();
        this.alphabets.put("diagnosisAlphabet", diagnosisAlphabet);
        this.alphabets.put("zipAlphabet", zipAlphabet);
        this.alphabets.put("integerAlphabet", integerAlphabet);
        this.alphabets.put("phoneAlphabet", phoneAlphabet);

        this.alphabetMapping = new HashMap<>();
        List<String> patientTable = List.of("public", "Patient");
        this.alphabetMapping.put(List.copyOf(patientTable).add("age"), "integerAlphabet");
        this.alphabetMapping.put(new Pair(patientTable, "height"), "integerAlphabet");
        this.alphabetMapping.put(new Pair(patientTable, "weight"), "integerAlphabet");
        this.alphabetMapping.put(new Pair(patientTable, "diagnosis"), "diagnosisAlphabet");
        this.alphabetMapping.put(new Pair(patientTable, "zip"), "zipAlphabet");
        this.alphabetMapping.put(new Pair(patientTable, "phone"), "phoneAlphabet");

        List<String> maskedLowTable = List.of("public", "Masked_low");
        this.alphabetMapping.put(new Pair(maskedLowTable, "age"), "integerAlphabet");
        this.alphabetMapping.put(new Pair(maskedLowTable, "height"), "integerAlphabet");
        this.alphabetMapping.put(new Pair(maskedLowTable, "weight"), "integerAlphabet");
        this.alphabetMapping.put(new Pair(maskedLowTable, "diagnosis"), "diagnosisAlphabet");
        this.alphabetMapping.put(new Pair(maskedLowTable, "zip"), "zipAlphabet");
        this.alphabetMapping.put(new Pair(maskedLowTable, "phone"), "phoneAlphabet");
    }

    public Alphabet getAlphabet(List<String> tableName, String attname){
        String alphabetName = alphabetMapping.get(new Pair(tableName, attname));
        return alphabetName != null ? alphabets.get(alphabetName) : null;
    }

    public Alphabet getAlphabet(String alphabetName){
        return alphabets.get(alphabetName);
    }
}
