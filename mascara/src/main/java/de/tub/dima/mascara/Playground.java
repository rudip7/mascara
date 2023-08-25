package de.tub.dima.mascara;

import de.tub.dima.mascara.parser.Parser;
import de.tub.dima.mascara.utils.DebuggingTools;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.externalize.RelWriterImpl;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static de.tub.dima.mascara.utils.Utils.readFile;

public class Playground {
    public static void main(String[] args) throws Exception {
        // Simple connection implementation for loading schema from sales.json

        Properties connectionProperties = new Properties();
        connectionProperties.put("url", "jdbc:postgresql://localhost:5432/mascaradb");
        connectionProperties.put("driverClassName", "org.postgresql.Driver");
        connectionProperties.put("username", "postgres");
        connectionProperties.put("user", "postgres");
        connectionProperties.put("password", "1902");
        connectionProperties.put("schema", "public");

        MascaraMaster mascara = new MascaraMaster(connectionProperties);

        Parser parser = mascara.parser;

//        String queryString = readFile("src/main/resources/queries/select_star.sql");
        String queryString = readFile("src/main/resources/queries/filter.sql");
//        String queryString = readFile("src/main/resources/queries/aggregate.sql");
//        String queryString = readFile("src/main/resources/queries/join.sql");

        System.out.println("[Requested Query]:\n");
        System.out.println(queryString);

        mascara.optimalCompliantQuery(queryString);
    }
}
