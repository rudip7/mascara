package de.tub.dima.mascara.dataMasking.medical.maskingFunctions;

import de.tub.dima.mascara.dataMasking.Generalization;
import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.inverseFunctions.InverseGeneralizeDiagnosis;

public class GeneralizeDiagnosis extends Generalization {
    public GeneralizeDiagnosis() {
        this.name = "GENERALIZE_DIAGNOSIS";
        this.inverseMaskingFunction = new InverseGeneralizeDiagnosis();
    }

    public static String eval(String diagnosis, int level) throws Exception {
        if (level == 1) {
            return diagnosis.substring(0, diagnosis.length() - 1) + "X";
        } else if (level == 2) {
            return diagnosis.substring(0, diagnosis.length() - 4) + "XX.X";
        } else {
            throw new Exception("For the MF generalize_diagnosis level should be either 1 or 2, and was " + level);
        }
    }
}
