package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;

import java.time.LocalDate;

public class BlurDate extends Generalization {
    public BlurDate() {
        this.name = "BLUR_DATE";
    }

    public static String eval(LocalDate date, int nFields) throws Exception {
        if (nFields <= 0) {
            throw new IllegalArgumentException("Invalid input");
        }
        StringBuilder maskedDate = new StringBuilder(date.toString());

        for (int i = maskedDate.length() - 1, count = 0; i >= 0 && count < nFields; i--) {
            if (maskedDate.charAt(i) != '-') {
                maskedDate.setCharAt(i, 'X');
                count++;
            }
        }
        return maskedDate.toString();
    }
}
