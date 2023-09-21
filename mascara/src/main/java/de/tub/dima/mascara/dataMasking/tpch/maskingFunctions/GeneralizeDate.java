package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;

public class GeneralizeDate extends MaskingFunction {
    public GeneralizeDate() {
        this.aggregable = true;
        this.name = "GENERALIZE_DATE";
    }

    public static Temporal eval(LocalDate date, String level) throws Exception {
        if (level.equals("MONTH")) {
            return YearMonth.of(date.getYear(), date.getMonth());
        } else if (level.equals("YEAR")) {
            return Year.of(date.getYear());
        } else {
            throw new Exception("For the MF GENERALIZE_DATE level should be either \"MONTH\" or \"YEAR\" and was " + level);
        }
    }
}
