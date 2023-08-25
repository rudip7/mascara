package de.tub.dima.mascara.dataMasking.alphabets;

import java.util.ArrayList;
import java.util.HashMap;

public class DiagnosisAlphabet extends Alphabet{
    public DiagnosisAlphabet() {
        this.alphabet = new HashMap<>();
        String letters = "ABCDEFGHIJKLMNOPQRSTVYZ";

        int idx = 0;
        for (char letter : letters.toCharArray()) {
            for (double number = 0.0; number < 100.0; number += 0.1) {
                double truncatedNumber = Math.floor(number * 10) / 10;
                String formattedNumber = (truncatedNumber < 10.0) ? "0" + truncatedNumber : String.valueOf(truncatedNumber);
                alphabet.put(letter + formattedNumber, idx);
            }
        }
    }
}
