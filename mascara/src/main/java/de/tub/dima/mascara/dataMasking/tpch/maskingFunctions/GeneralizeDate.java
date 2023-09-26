package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;

public class GeneralizeDate extends MaskingFunction {
    public GeneralizeDate() {
        this.aggregable = true;
        this.name = "GENERALIZE_DATE";
    }

//    public static Date eval(Date date, int level) throws Exception {
//        if (level == 3) {
////            return LocalDate.of(date.getYear(), date.getMonth(), 1);
////            return YearMonth.of(date.getYear(), date.getMonth());
//            return null;
//        } else if (level == 4) {
////            return LocalDate.of(date.getYear(), 1, 1);
////            return Year.of(date.getYear());
//            return null;
//        } else {
//            throw new Exception("For the MF GENERALIZE_DATE level should be either \"MONTH\" or \"YEAR\" and was " + level);
//        }
//    }

    public static Date eval(Date date, String level) throws Exception {
        return null;
//        if (level.equals("MONTH")) {
//            return YearMonth.of(date.getYear(), date.getMonth());
//        } else if (level.equals("YEAR")) {
//            return Year.of(date.getYear());
//        } else {
//            throw new Exception("For the MF GENERALIZE_DATE level should be either \"MONTH\" or \"YEAR\" and was " + level);
//        }
    }
}
