package de.tub.dima.mascara.dataMasking.inverseFunctions;

import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import de.tub.dima.mascara.dataMasking.alphabets.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.alphabets.ZipAlphabet;

import java.util.ArrayList;
import java.util.List;

public class InverseBlurZip extends InverseMaskingFunction {

    public InverseBlurZip() {
        this.name = "INVERSE_BLUR_ZIP";
        this.alphabet = AlphabetCatalog.getInstance().getAlphabet("zipAlphabet");
    }

    @Override
    public List<String> eval(String blurredZip) {
        assert blurredZip.length() == 6;

        if (blurredZip.matches("\\d+")) {
            return List.of(blurredZip);
        }

        int cut = blurredZip.indexOf('X');
        String baseZip = blurredZip.substring(0, cut);
        assert baseZip.isEmpty() || baseZip.matches("\\d+");
        assert blurredZip.substring(cut).equals("X".repeat(6 - cut));

        List<String> values = new ArrayList<>();
        for (int i = 0; i < Math.pow(10, 6 - cut); i++) {
            String tail = String.format("%0" + (6 - cut) + "d", i);
            String zipCode = baseZip + tail;
            int index = alphabet.indexOf(zipCode);
            if (index >= 0){
                values.add(zipCode);
            }
        }
        return values;
    }
}
