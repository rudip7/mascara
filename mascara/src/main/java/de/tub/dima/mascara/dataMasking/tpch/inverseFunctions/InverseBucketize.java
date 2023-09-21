package de.tub.dima.mascara.dataMasking.tpch.inverseFunctions;

import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InverseBucketize extends InverseMaskingFunction {

    public final float granularity;
    public InverseBucketize(float granularity, String alphabetName) {
        this.name = "INVERSE_BUCKETIZE";
        this.alphabet = AlphabetCatalog.getInstance().getAlphabet(alphabetName);
        this.granularity = granularity;
    }

    public InverseBucketize(float granularity){
        this(granularity, "floatAlphabet");
    }

    public InverseBucketize(){
        this(1.0f, "integerAlphabet");
    }
    @Override
    public List<String> eval(String maskedValue) {
        List<String> values = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+\\.\\d+|\\d+");
        Matcher matcher = pattern.matcher(maskedValue);

        List<Float> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(Float.parseFloat(matcher.group()));
        }

        float low = numbers.get(0);
        float high = numbers.get(1);
        float current = low;
        if (maskedValue.charAt(maskedValue.length() - 1) == ')'){
            while (current < high){
                values.add(String.valueOf(current));
                current += granularity;
            }
        } else if (maskedValue.charAt(maskedValue.length() - 1) == ']') {
            while (current <= high){
                values.add(String.valueOf(current));
                current += granularity;
            }
        }


        return values;
    }
}
