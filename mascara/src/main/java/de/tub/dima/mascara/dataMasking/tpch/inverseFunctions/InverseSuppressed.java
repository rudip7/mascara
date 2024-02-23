package de.tub.dima.mascara.dataMasking.tpch.inverseFunctions;

import de.tub.dima.mascara.dataMasking.Alphabet;
import de.tub.dima.mascara.dataMasking.DiscretizedAlphabet;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import de.tub.dima.mascara.dataMasking.medical.alphabets.DoubleAlphabet;
import de.tub.dima.mascara.dataMasking.medical.alphabets.IntegerAlphabet;
import de.tub.dima.mascara.dataMasking.tpch.alphabets.FloatAlphabet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InverseSuppressed extends InverseMaskingFunction {

    public List<String> alphabetValues = null;

    public InverseSuppressed() {
        this.name = "INVERSE_SUPPRESSED";
    }

    @Override
    public List<String> eval(String maskedValue) {
        if (alphabetValues == null){
            return Arrays.asList(maskedValue);
        } else {
            return alphabetValues;
        }
    }

    @Override
    public void setAlphabet(Alphabet alphabet) {
        super.setAlphabet(alphabet);

        if (this.alphabet != null && this.alphabet.getAlphabet() != null){
            alphabetValues = new ArrayList<>(this.alphabet.getAlphabet().keySet());
        }
    }
}
