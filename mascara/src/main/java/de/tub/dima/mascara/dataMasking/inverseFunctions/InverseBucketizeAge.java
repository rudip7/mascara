package de.tub.dima.mascara.dataMasking.inverseFunctions;

import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import de.tub.dima.mascara.dataMasking.alphabets.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.alphabets.IntegerAlphabet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InverseBucketizeAge extends InverseMaskingFunction {
    public InverseBucketizeAge() {
        this.name = "INVERSE_BUCKETIZE_AGE";
        this.alphabet = AlphabetCatalog.getInstance().getAlphabet("integerAlphabet");
    }

    @Override
    public List<String> eval(String bucketizedAge) {
        List<String> values = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+\\.\\d+|\\d+");
        Matcher matcher = pattern.matcher(bucketizedAge);

        List<Integer> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add((int) Double.parseDouble(matcher.group()));
        }

        int low = numbers.get(0);
        int high = numbers.get(1);
        for (int j = low; j <= high; j++) {
            values.add(String.valueOf(j));
        }

        return values;
    }
}
