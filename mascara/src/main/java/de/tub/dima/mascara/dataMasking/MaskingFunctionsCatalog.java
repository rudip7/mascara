package de.tub.dima.mascara.dataMasking;

import de.tub.dima.mascara.dataMasking.medical.maskingFunctions.*;
import de.tub.dima.mascara.dataMasking.tpch.maskingFunctions.*;
import de.tub.dima.mascara.dataMasking.tpch.maskingFunctions.AddRelativeNoise;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;

import java.util.ArrayList;
import java.util.List;

public class MaskingFunctionsCatalog {

    public List<MaskingFunction> maskingFunctions;

    public List<InverseMaskingFunction> inverseMaskingFunctions;

    public MaskingFunctionsCatalog() {
        this.maskingFunctions = new ArrayList<>();
        this.maskingFunctions.add(new AddNoiseDate());
        this.maskingFunctions.add(new GeneralizeDate());
        this.maskingFunctions.add(new Suppress());
        this.maskingFunctions.add(new Bucketize());
        this.maskingFunctions.add(new AddRelativeNoise());


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
//            schema.add();
        }
//        schema.add("GENERALIZE_DATE", new GeneralizeDate2());
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
