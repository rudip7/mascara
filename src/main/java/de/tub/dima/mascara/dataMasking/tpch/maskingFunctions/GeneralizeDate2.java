package de.tub.dima.mascara.dataMasking.tpch.maskingFunctions;

import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.*;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GeneralizeDate2 extends SqlFunction {
    public GeneralizeDate2() {
        super("GENERALIZE_DATE", SqlKind.OTHER_FUNCTION, ReturnTypes.DATE, InferTypes.FIRST_KNOWN, OperandTypes.STRING, SqlFunctionCategory.USER_DEFINED_FUNCTION);
    }
}
