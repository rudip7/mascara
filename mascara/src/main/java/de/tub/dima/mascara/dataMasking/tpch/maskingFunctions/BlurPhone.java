package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseBlurPhone;

import java.time.LocalDate;

public class BlurPhone extends Generalization {
    public BlurPhone() {
        this.name = "BLUR_PHONE";
        this.inverseMaskingFunction = new InverseBlurPhone();
    }

    public static String eval(String phone) throws Exception {
        return phone.substring(0, 6)+"-XXX-XXXX";
    }
}
