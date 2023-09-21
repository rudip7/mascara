package de.tub.dima.mascara.dataMasking.medical.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.inverseFunctions.InverseBlurPhone;

public class BlurPhone extends MaskingFunction {

    public BlurPhone() {
        this.name = "BLUR_PHONE";
        this.inverseMaskingFunction = new InverseBlurPhone();
    }

    public static String eval(String phoneStr, int nFields) {
        if (nFields >= phoneStr.length()) {
            return "X".repeat(phoneStr.length());
        } else {
            return phoneStr.substring(0, phoneStr.length() - nFields) + "X".repeat(nFields);
        }
    }

}
