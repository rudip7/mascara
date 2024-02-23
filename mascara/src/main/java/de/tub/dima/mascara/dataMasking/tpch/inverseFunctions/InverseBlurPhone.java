package de.tub.dima.mascara.dataMasking.tpch.inverseFunctions;

import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import de.tub.dima.mascara.dataMasking.tpch.alphabets.PhoneAlphabet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InverseBlurPhone extends InverseMaskingFunction {

    public InverseBlurPhone() {
        this.name = "INVERSE_BLUR_PHONE";
        this.alphabet = new PhoneAlphabet();
    }


    @Override
    public List<String> eval(String maskedValue) {
//        return Arrays.asList(min(maskedValue), max(maskedValue));

        String phonePrefix = maskedValue.substring(0, 7);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 10000000; i+= 1000) {
            String rest = String.format("%07d", i);
            values.add(phonePrefix + rest.substring(0,3)+"-"+rest.substring(3));
        }
        return values;
    }

    public static String min(String maskedValue) {
        return maskedValue.substring(0, 7)+"000-0000";
    }

    public static String max(String maskedValue) {
        return maskedValue.substring(0, 7)+"999-9999";
    }

}
