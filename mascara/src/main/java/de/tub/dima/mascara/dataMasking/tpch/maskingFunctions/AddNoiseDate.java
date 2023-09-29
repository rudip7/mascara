package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import de.tub.dima.mascara.dataMasking.MaskingFunction;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

public class AddNoiseDate extends MaskingFunction {
    public AddNoiseDate() {
        this.name = "ADD_NOISE_DATE";
    }

    public static Date eval(Date date, String level, int noise) throws Exception {
        LocalDate localDate = date.toLocalDate();
        noise = Math.abs(noise);
        LocalDate min;
        LocalDate max;
        if (level.equals("DAYS")) {
            min = localDate.minus(noise, ChronoUnit.DAYS);
            max = localDate.plus(noise, ChronoUnit.DAYS);
        } else if (level.equals("WEEKS")) {
            min = localDate.minus(noise, ChronoUnit.WEEKS);
            max = localDate.plus(noise, ChronoUnit.WEEKS);
        } else if (level.equals("MONTHS")) {
            min = localDate.minus(noise, ChronoUnit.MONTHS);
            max = localDate.plus(noise, ChronoUnit.MONTHS);
        } else if (level.equals("YEARS")) {
            min = localDate.minus(noise, ChronoUnit.YEARS);
            max = localDate.plus(noise, ChronoUnit.YEARS);
        } else {
            throw new Exception("For the MF NOISE_DATE level should be either \"DAYS\", \"WEEKS\", \"MONTH\" or \"YEAR\" and was " + level);
        }

        long startEpochDay = min.toEpochDay();
        long endEpochDay = max.toEpochDay();

        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);

        return Date.valueOf(LocalDate.ofEpochDay(randomEpochDay));
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
