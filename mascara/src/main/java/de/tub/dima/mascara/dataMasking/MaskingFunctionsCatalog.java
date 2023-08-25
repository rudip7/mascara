package de.tub.dima.mascara.dataMasking;

import de.tub.dima.mascara.dataMasking.inverseFunctions.InverseBlurPhone;
import de.tub.dima.mascara.dataMasking.inverseFunctions.InverseBlurZip;
import de.tub.dima.mascara.dataMasking.inverseFunctions.InverseBucketizeAge;
import de.tub.dima.mascara.dataMasking.inverseFunctions.InverseGeneralizeDiagnosis;
import de.tub.dima.mascara.dataMasking.maskingFunctions.*;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaskingFunctionsCatalog {

    public List<MaskingFunction> maskingFunctions;

    public List<InverseMaskingFunction> inverseMaskingFunctions;

    public MaskingFunctionsCatalog() {
        this.maskingFunctions = new ArrayList<>();
        this.maskingFunctions.add(new AddRelativeNoise());
        BlurPhone blurPhone = new BlurPhone();
        this.maskingFunctions.add(blurPhone);
        BlurZip blurZip = new BlurZip();
        this.maskingFunctions.add(blurZip);
        BucketizeAge bucketizeAge = new BucketizeAge();
        this.maskingFunctions.add(bucketizeAge);
        GeneralizeDiagnosis generalizeDiagnosis = new GeneralizeDiagnosis();
        this.maskingFunctions.add(generalizeDiagnosis);

        this.inverseMaskingFunctions = new ArrayList<>();
        for (MaskingFunction maskingFunction : this.maskingFunctions) {
            if (maskingFunction.getInverseMaskingFunction() != null){
                this.inverseMaskingFunctions.add(maskingFunction.getInverseMaskingFunction());
            }
        }
    }

    public void addToSchema(SchemaPlus schema){
        for (MaskingFunction function : maskingFunctions) {
            schema.add(function.name, ScalarFunctionImpl.create(function.getClass(), "eval"));
        }
    }

    public MaskingFunction getMaskingFunctionByName(String name){
        for (MaskingFunction function : maskingFunctions) {
            if (function.name.equals(name)){
                return function;
            }
        }
        return null;
    }

    public InverseMaskingFunction getInverseMaskingFunctionByName(String name){
        for (InverseMaskingFunction function : inverseMaskingFunctions) {
            if (function.name.equals(name)){
                return function;
            }
        }
        return null;
    }
}
