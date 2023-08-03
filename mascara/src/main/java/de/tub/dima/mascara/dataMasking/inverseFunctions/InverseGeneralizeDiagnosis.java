package de.tub.dima.mascara.dataMasking.inverseFunctions;

import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import java.util.ArrayList;
import java.util.List;

public class InverseGeneralizeDiagnosis implements InverseMaskingFunction {
    public static List<String> eval(String generalizedDiagnosis) {
        char head = generalizedDiagnosis.charAt(0);
        String possibleHeads = "ABCDEFGHIJKLMNOPQRSTVXYZ";
        assert possibleHeads.indexOf(head) != -1 && generalizedDiagnosis.length() == 5 :
                "Input with wrong formatting. " + generalizedDiagnosis;

        List<String> values = new ArrayList<>();
        if (generalizedDiagnosis.substring(1).equals("XX.X")) {
            for (double i = 0.0; i <= 99.9; i += 0.1) {
                String tail = String.format("%.1f", i);
                if (i < 10.0) {
                    values.add(head + "0" + tail.substring(0, 3));
                } else {
                    values.add(head + tail.substring(0, 4));
                }
            }
        } else if (generalizedDiagnosis.substring(3).equals(".X") && Character.isDigit(generalizedDiagnosis.charAt(1)) &&
                Character.isDigit(generalizedDiagnosis.charAt(2))) {
            for (int i = 0; i < 10; i++) {
                values.add(generalizedDiagnosis.substring(0, 4) + i);
            }
        } else {
            throw new IllegalArgumentException("Input with wrong formatting.");
        }

        return values;
    }
}
