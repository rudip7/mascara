package de.tub.dima.mascara.dataMasking.tpch.inverseFunctions;

import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.alphabets.DoubleAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.IntegerAlphabet;
import de.tub.dima.mascara.dataMasking.tpch.alphabets.FloatAlphabet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InverseBucketizeLow extends InverseMaskingFunction {

    public double granularity;
    public double bSize;
    public InverseBucketizeLow(double bSize, double granularity) {
        this.name = "INVERSE_BUCKETIZE_LOW";
        this.granularity = granularity;
        this.bSize = bSize;
    }

    public InverseBucketizeLow(double bSize){
        this(bSize, 1.0);
    }
    @Override
    public List<String> eval(String maskedValue) {
        List<String> values = new ArrayList<>();
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
}
