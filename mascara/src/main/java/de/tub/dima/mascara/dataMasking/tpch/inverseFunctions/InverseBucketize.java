package de.tub.dima.mascara.dataMasking.tpch.inverseFunctions;

import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.AlphabetCatalog;
import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.alphabets.DoubleAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.IntegerAlphabet;
import de.tub.dima.mascara.dataMasking.tpch.alphabets.FloatAlphabet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InverseBucketize extends InverseMaskingFunction {


    public double granularity;



    public double bSize = -1.0;
    public InverseBucketize(double granularity) {
        this.name = "INVERSE_BUCKETIZE";
        this.granularity = granularity;
    }

    public InverseBucketize(){
        this(1.0);
    }

    public InverseBucketize(double bSize, double granularity){
        this(granularity);
        this.bSize = bSize;
    }
    @Override
    public List<String> eval(String maskedValue) {
        List<String> values = new ArrayList<>();

        try {
            double low = Double.parseDouble(maskedValue);
            double high = low + bSize;

            double current = low;
            while (current < high){
                if (discretize){
                    values.add(((DiscretizedAlphabet) this.alphabet).getDiscretizedValue(String.valueOf(current)));
                } else {
                    values.add(String.valueOf(current));
                }
                current += granularity;
            }
            return values;
        } catch (NumberFormatException e) {
            Pattern pattern = Pattern.compile("\\d+\\.\\d+|\\d+");
            Matcher matcher = pattern.matcher(maskedValue);

            List<Double> numbers = new ArrayList<>();
            while (matcher.find()) {
                String stringValue = matcher.group();
                if (discretize){
                    stringValue = ((DiscretizedAlphabet) this.alphabet).getDiscretizedValue(stringValue);
                }
                numbers.add(Double.parseDouble(stringValue));
            }

            double low = numbers.get(0);
            double high = numbers.get(1);
            double current = low;
            if (maskedValue.charAt(maskedValue.length() - 1) == ')'){
                while (current < high){
                    if (discretize){
                        values.add(((DiscretizedAlphabet) this.alphabet).getDiscretizedValue(String.valueOf(current)));
                    } else {
                        values.add(String.valueOf(current));
                    }
                    current += granularity;
                }
            } else if (maskedValue.charAt(maskedValue.length() - 1) == ']') {
                while (current <= high){
                    if (discretize){
                        values.add(((DiscretizedAlphabet) this.alphabet).getDiscretizedValue(String.valueOf(current)));
                    } else {
                        values.add(String.valueOf(current));
                    }
                    current += granularity;
                }
            }

            return values;
        }
    }

    @Override
    public void setAlphabet(Alphabet alphabet) {
        super.setAlphabet(alphabet);
        if (this.alphabet != null && this.alphabet instanceof DiscretizedAlphabet && ((DiscretizedAlphabet) this.alphabet).shouldDiscretize()){
            this.discretize = true;
            if (this.alphabet instanceof FloatAlphabet){
                this.granularity = ((FloatAlphabet) this.alphabet).getAccuracy();
            } else if (this.alphabet instanceof DoubleAlphabet){
                this.granularity = ((DoubleAlphabet) this.alphabet).getAccuracy();
            } else if (this.alphabet instanceof IntegerAlphabet){
                this.granularity = 1.0;
            }
        }
    }

    public double getGranularity() {
        return granularity;
    }

    public void setbSize(double bSize) {
        this.bSize = bSize;
    }

}
