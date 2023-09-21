package de.tub.dima.mascara.dataMasking.tpch.alphabets;

import de.tub.dima.mascara.dataMasking.Alphabet;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateAlphabet extends Alphabet {
    @Override
    public long indexOf(String value) {
        LocalDate date = LocalDate.parse(value);
        return date.toEpochDay();
    }

    @Override
    public long binNDistinct(String lowerBound, String upperBound) {
        LocalDate parsedLow = LocalDate.parse(lowerBound);
        LocalDate parsedUpper = LocalDate.parse(upperBound);
        return ChronoUnit.DAYS.between(parsedLow, parsedUpper);
    }
}
