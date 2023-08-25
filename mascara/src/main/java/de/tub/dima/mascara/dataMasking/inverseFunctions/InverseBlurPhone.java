package de.tub.dima.mascara.dataMasking.inverseFunctions;

import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;

import java.util.ArrayList;
import java.util.List;

public class InverseBlurPhone extends InverseMaskingFunction {

    public InverseBlurPhone() {
        this.name = "INVERSE_BLUR_PHONE";
    }

    @Override
    public List<String> eval(String blurredPhone) {
        if (blurredPhone.matches("\\d+")) {
            return List.of(blurredPhone);
        }

        int phoneLen = blurredPhone.length();
        int cut = blurredPhone.indexOf('X');
        String basePhone = blurredPhone.substring(0, cut);
        assert basePhone.isEmpty() || basePhone.matches("\\d+");
        assert blurredPhone.substring(cut).equals("X".repeat(phoneLen - cut));

        List<String> values = new ArrayList<>();
        for (int i = 0; i < Math.pow(10, phoneLen - cut); i++) {
            String tail = String.format("%0" + (phoneLen - cut) + "d", i);
            values.add(basePhone + tail);
        }
        return values;
    }
}
