package de.tub.dima.mascara.dataMasking;

import de.tub.dima.mascara.dataMasking.medical.alphabets.DiagnosisAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.IntegerAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.PhoneAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.ZipAlphabet;

import java.util.ArrayList;
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


//    private AlphabetCatalog() {
//        DiagnosisAlphabet diagnosisAlphabet = new DiagnosisAlphabet();
//        ZipAlphabet zipAlphabet = new ZipAlphabet();
//        IntegerAlphabet integerAlphabet = new IntegerAlphabet(0, 250);
//        PhoneAlphabet phoneAlphabet = new PhoneAlphabet();
//
//        this.alphabets = new HashMap<>();
//        this.alphabets.put("diagnosisAlphabet", diagnosisAlphabet);
//        this.alphabets.put("zipAlphabet", zipAlphabet);
//        this.alphabets.put("integerAlphabet", integerAlphabet);
//        this.alphabets.put("phoneAlphabet", phoneAlphabet);
//
//        this.alphabetMapping = new HashMap<>();
//        List<String> patientTable = List.of("public", "Patient");
//        this.alphabetMapping.put(List.of("public", "Patient", "age"), "integerAlphabet");
//        this.alphabetMapping.put(List.of("public", "Patient",  "height"), "integerAlphabet");
//        this.alphabetMapping.put(List.of("public", "Patient",  "weight"), "integerAlphabet");
//        this.alphabetMapping.put(List.of("public", "Patient",  "diagnosis"), "diagnosisAlphabet");
//        this.alphabetMapping.put(List.of("public", "Patient",  "zip"), "zipAlphabet");
//        this.alphabetMapping.put(List.of("public", "Patient",  "phone"), "phoneAlphabet");
//
//        List<String> maskedLowTable = List.of("public", "Masked_low");
//        this.alphabetMapping.put(List.of("public", "Masked_low", "age"), "integerAlphabet");
//        this.alphabetMapping.put(List.of("public", "Masked_low", "height"), "integerAlphabet");
//        this.alphabetMapping.put(List.of("public", "Masked_low", "weight"), "integerAlphabet");
//        this.alphabetMapping.put(List.of("public", "Masked_low", "diagnosis"), "diagnosisAlphabet");
//        this.alphabetMapping.put(List.of("public", "Masked_low", "zip"), "zipAlphabet");
//        this.alphabetMapping.put(List.of("public", "Masked_low", "phone"), "phoneAlphabet");
//    }

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
        this.alphabetMapping.put(List.of("public", "Patient", "age"), "integerAlphabet");
        this.alphabetMapping.put(List.of("public", "Patient",  "height"), "integerAlphabet");
        this.alphabetMapping.put(List.of("public", "Patient",  "weight"), "integerAlphabet");
        this.alphabetMapping.put(List.of("public", "Patient",  "diagnosis"), "diagnosisAlphabet");
        this.alphabetMapping.put(List.of("public", "Patient",  "zip"), "zipAlphabet");
        this.alphabetMapping.put(List.of("public", "Patient",  "phone"), "phoneAlphabet");

        List<String> maskedLowTable = List.of("public", "Masked_low");
        this.alphabetMapping.put(List.of("public", "Masked_low", "age"), "integerAlphabet");
        this.alphabetMapping.put(List.of("public", "Masked_low", "height"), "integerAlphabet");
        this.alphabetMapping.put(List.of("public", "Masked_low", "weight"), "integerAlphabet");
        this.alphabetMapping.put(List.of("public", "Masked_low", "diagnosis"), "diagnosisAlphabet");
        this.alphabetMapping.put(List.of("public", "Masked_low", "zip"), "zipAlphabet");
        this.alphabetMapping.put(List.of("public", "Masked_low", "phone"), "phoneAlphabet");
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
