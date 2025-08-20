package de.tub.dima.mascara.dataMasking.tpch.alphabets;

import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneAlphabet extends DiscretizedAlphabet {
    public PhoneAlphabet() {
        this.discretize = true;
    }

    @Override
    public long indexOf(String value) {
        String pattern = "^\\d{2}-\\d{3}-\\d{3}-\\d{4}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(value);

        if (m.matches()) {
            String number = value.replace("-", "");
            return Long.parseLong(number);
        } else{
            return -1;
        }
    }

    @Override
    public String getDiscretizedValue(String value) {
        if (isDiscretizeble(value))
            return value;
        return null;
    }

    @Override
    public boolean isDiscretizeble(String sampleValue) {
        String pattern = "^\\d{2}-\\d{3}-\\d{3}-\\d{4}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(sampleValue);
        return m.matches();
    }

//    @Override
//    public long binNDistinct(String lowerBound, String upperBound) {
//        long low = indexOf(lowerBound);
//        long up = indexOf(upperBound);
//        return low <= up ? (up - low) : -1;
//    }
}
