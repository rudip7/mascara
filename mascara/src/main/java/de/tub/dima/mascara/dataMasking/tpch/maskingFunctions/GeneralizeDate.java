package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;
import de.tub.dima.mascara.dataMasking.tpch.inverseFunctions.InverseGeneralizeDate;
import org.apache.calcite.util.NlsString;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;
import java.util.List;

public class GeneralizeDate extends MaskingFunction {
    public GeneralizeDate() {
        this.aggregable = true;
        this.name = "GENERALIZE_DATE";
        this.parametrizedInverse = true;
        this.inverseMaskingFunction = new InverseGeneralizeDate();
    }

    @Override
    public void setInverseMaskingFunction(List<Object> parameters) {
        if (parameters.size() == 1){
            String level;
            Object param = parameters.get(0);
            if (param instanceof NlsString){
                level = ((NlsString) parameters.get(0)).getValue();
            } else {
                level = (String) parameters.get(0);
            }
            this.inverseMaskingFunction = new InverseGeneralizeDate(level);
        }
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
        if (level.equals("MONTH")) {
            return Date.valueOf(LocalDate.of(date.getYear(), date.getMonth(), 1));
        } else if (level.equals("YEAR")) {
            return Date.valueOf(LocalDate.of(date.getYear(), 1, 1));
        } else {
            throw new Exception("For the MF GENERALIZE_DATE level should be either \"MONTH\" or \"YEAR\" and was " + level);
        }
    }

    @Override
    public GeneralizeDate clone(){
        GeneralizeDate cloned = new GeneralizeDate();
        cloned.inverseMaskingFunction = this.inverseMaskingFunction;
        return cloned;
    }
}
