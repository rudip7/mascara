package de.tub.dima.mascara.dataMasking.tpch.inverseFunctions;

import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class InverseGeneralizeDate extends InverseMaskingFunction {
    public InverseGeneralizeDate() {
        this.name = "INVERSE_GENERALIZE_DATE";
        this.alphabet = AlphabetCatalog.getInstance().getAlphabet("dateAlphabet");
    }

    @Override
    public List<String> eval(String maskedValue) {
        List<String> possibleDates = new ArrayList<>();
        LocalDate startDate = null;
        LocalDate endDate= null;
        LocalDate currentDate;
        if (maskedValue.length() == 7) {
            YearMonth yearMonth = YearMonth.parse(maskedValue);

            startDate = yearMonth.atDay(1);
            endDate = yearMonth.atEndOfMonth();
        } else if (maskedValue.length() == 4) {
            startDate = LocalDate.of(Integer.parseInt(maskedValue), 1,1);
            endDate = LocalDate.of(Integer.parseInt(maskedValue), 12,31);
        }
        if (startDate != null && endDate != null){
            currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                possibleDates.add(currentDate.toString());
                currentDate = currentDate.plus(1, ChronoUnit.DAYS);
            }
        }
        return possibleDates;
    }
}
