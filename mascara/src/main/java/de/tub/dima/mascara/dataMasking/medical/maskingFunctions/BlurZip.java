package de.tub.dima.mascara.dataMasking.medical.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.inverseFunctions.InverseBlurZip;

public class BlurZip extends Generalization {

    public BlurZip() {
        this.name = "BLUR_ZIP";
        this.inverseMaskingFunction = new InverseBlurZip();
    }

    public static String eval(String zipCode, int nFields) {
        assert (nFields <= 6 && nFields > 0);
        return zipCode.substring(0, zipCode.length() - nFields) + "X".repeat(nFields);
    }
}
