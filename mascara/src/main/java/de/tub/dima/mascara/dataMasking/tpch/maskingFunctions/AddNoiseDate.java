package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class AddNoiseDate extends MaskingFunction {
    public AddNoiseDate() {
        this.name = "ADD_NOISE_DATE";
    }

    public static LocalDate eval(LocalDate date, int level, int noise) throws Exception {
        noise = Math.abs(noise);
        LocalDate min;
        LocalDate max;
        if (level == 1) {
            min = date.minus(noise, ChronoUnit.DAYS);
            max = date.plus(noise, ChronoUnit.DAYS);
        } else if (level == 2) {
            min = date.minus(noise, ChronoUnit.WEEKS);
            max = date.plus(noise, ChronoUnit.WEEKS);
        } else if (level == 3) {
            min = date.minus(noise, ChronoUnit.MONTHS);
            max = date.plus(noise, ChronoUnit.MONTHS);
        } else if (level == 4) {
            min = date.minus(noise, ChronoUnit.YEARS);
            max = date.plus(noise, ChronoUnit.YEARS);
        } else {
            throw new Exception("For the MF NOISE_DATE level should be either \"DAYS\", \"WEEKS\", \"MONTH\" or \"YEAR\" and was " + level);
        }

        long startEpochDay = min.toEpochDay();
        long endEpochDay = max.toEpochDay();

        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);

        return LocalDate.ofEpochDay(randomEpochDay);
    }

//    public static LocalDate eval(LocalDate date, String level, int noise) throws Exception {
//        noise = Math.abs(noise);
//        LocalDate min;
//        LocalDate max;
//        if (level.equals("DAYS")) {
//            min = date.minus(noise, ChronoUnit.DAYS);
//            max = date.plus(noise, ChronoUnit.DAYS);
//        } else if (level.equals("WEEKS")) {
//            min = date.minus(noise, ChronoUnit.WEEKS);
//            max = date.plus(noise, ChronoUnit.WEEKS);
//        } else if (level.equals("MONTHS")) {
//            min = date.minus(noise, ChronoUnit.MONTHS);
//            max = date.plus(noise, ChronoUnit.MONTHS);
//        } else if (level.equals("YEARS")) {
//            min = date.minus(noise, ChronoUnit.YEARS);
//            max = date.plus(noise, ChronoUnit.YEARS);
//        } else {
//            throw new Exception("For the MF NOISE_DATE level should be either \"DAYS\", \"WEEKS\", \"MONTH\" or \"YEAR\" and was " + level);
//        }
//
//        long startEpochDay = min.toEpochDay();
//        long endEpochDay = max.toEpochDay();
//
//        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
//
//        return LocalDate.ofEpochDay(randomEpochDay);
//    }
}
