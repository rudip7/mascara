package de.tub.dima.mascara.dataMasking;

import de.tub.dima.mascara.dataMasking.acs.Bucketize1;
import de.tub.dima.mascara.dataMasking.acs.BucketizeLow1;
import de.tub.dima.mascara.dataMasking.acs.NoiseCow;
import de.tub.dima.mascara.dataMasking.acs.NoiseMar;
import de.tub.dima.mascara.dataMasking.medical.maskingFunctions.*;
import de.tub.dima.mascara.dataMasking.tpch.maskingFunctions.*;
import de.tub.dima.mascara.dataMasking.tpch.maskingFunctions.AddRelativeNoise;
import de.tub.dima.mascara.dataMasking.tpch.maskingFunctions.BlurPhone;
import de.tub.dima.mascara.dataMasking.tpch.transformationFuntions.GetRangeMidpoint;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;

import java.util.ArrayList;
import java.util.List;

public class MaskingFunctionsCatalog {

    public List<MaskingFunction> maskingFunctions;

    public List<TransformationFunction> transformationFunctions;

    public List<InverseMaskingFunction> inverseMaskingFunctions;

    public MaskingFunctionsCatalog() {
        this.maskingFunctions = new ArrayList<>();
        this.maskingFunctions.add(new AddNoiseDate());
        this.maskingFunctions.add(new GeneralizeDate());
        this.maskingFunctions.add(new Suppress());
        this.maskingFunctions.add(new Bucketize());
        this.maskingFunctions.add(new BucketizeLow());
        this.maskingFunctions.add(new AddRelativeNoise());
        this.maskingFunctions.add(new AddAbsoluteNoise());
        this.maskingFunctions.add(new Round());
        this.maskingFunctions.add(new BlurPhone());
        this.maskingFunctions.add(new AddLaplaceNoise());

        this.maskingFunctions.add(new NoiseCow());
        this.maskingFunctions.add(new NoiseMar());
        this.maskingFunctions.add(new Bucketize1());
        this.maskingFunctions.add(new BucketizeLow1());


        this.inverseMaskingFunctions = new ArrayList<>();
        for (MaskingFunction maskingFunction : this.maskingFunctions) {
            if (maskingFunction instanceof Generalization && ((Generalization) maskingFunction).getInverseMaskingFunction() != null){
                this.inverseMaskingFunctions.add(((Generalization) maskingFunction).getInverseMaskingFunction());
            }
        }

        this.transformationFunctions = new ArrayList<>();
        this.transformationFunctions.add(new GetRangeMidpoint());
    }

    public void addToSchema(SchemaPlus schema){
        for (MaskingFunction function : maskingFunctions) {
            schema.add(function.name, ScalarFunctionImpl.create(function.getClass(), "eval"));
        }
        for (TransformationFunction function : transformationFunctions) {
            schema.add(function.name, ScalarFunctionImpl.create(function.getClass(), "eval"));
        }

    }

    public MaskingFunction getMaskingFunctionByName(String name){
        for (MaskingFunction function : maskingFunctions) {
            if (function.name.equalsIgnoreCase(name)){
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
