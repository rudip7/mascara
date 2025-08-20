package de.tub.dima.mascara.dataMasking.tpch.inverseFunctions;

import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class InverseGeneralizeDate extends InverseMaskingFunction {
    private String level;

    public InverseGeneralizeDate() {
        this.name = "INVERSE_GENERALIZE_DATE";
        this.alphabet = AlphabetCatalog.getInstance().getAlphabet("dateAlphabet");
        this.level = "MONTH";
    }

    public InverseGeneralizeDate(String level) {
        this();
        this.level = level;
    }
    @Override
    public List<String> eval(String maskedValue) {
        List<String> possibleDates = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(maskedValue);
        LocalDate endDate= null;
        LocalDate currentDate;
        if (level.equals("MONTH")) {
            endDate = startDate.plusDays(startDate.lengthOfMonth());
        } else if (level.equals("YEAR")) {
            endDate = LocalDate.of(startDate.getYear(), 12,31);
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
