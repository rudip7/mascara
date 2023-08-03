package de.tub.dima.mascara.utils;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.externalize.RelWriterImpl;
import org.apache.calcite.sql.SqlExplainLevel;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DebuggingTools {

    public static void printPlan(String header, RelNode relTree) {
        StringWriter sw = new StringWriter();

        sw.append("\n").append(header).append("\n");

        RelWriterImpl relWriter = new RelWriterImpl(new PrintWriter(sw), SqlExplainLevel.ALL_ATTRIBUTES, true);

        relTree.explain(relWriter);

        System.out.println(sw);
    }
}
