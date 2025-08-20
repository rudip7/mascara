package de.tub.dima.mascara.examples;

import de.tub.dima.mascara.MascaraMaster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static de.tub.dima.mascara.utils.Utils.readFile;

public class ACSExperiment {
    public static void main(String[] args) throws Exception {
        // Simple connection implementation for loading schema from sales.json

        Properties connectionProperties = new Properties();
//        connectionProperties.put("url", "jdbc:postgresql://localhost:5432/mascaradb");
        connectionProperties.put("url", "jdbc:postgresql://localhost:5432/tpchdb");
        connectionProperties.put("driverClassName", "org.postgresql.Driver");
        connectionProperties.put("username", "postgres");
        connectionProperties.put("user", "postgres");
        connectionProperties.put("password", "1902");
        connectionProperties.put("schema", "public");

        MascaraMaster mascara = new MascaraMaster(connectionProperties);

//         Debug
        String queryString = readFile("src/main/resources/queries/acs_income.sql");
        String output = "src/main/resources/results/acs/acs_income.csv";
        String report = "src/main/resources/compliantQueries/acs/acs_income.json";
        String detailed = "src/main/resources/reports/acs/acs_income.csv";
        mascara.optimalCompliantQuery(queryString, output, report, detailed);


//        String queriesDir = "src/main/resources/queries/tpch/access";
//        String baseResultsDir = "src/main/resources/results/ranking_plans/access";
//        String reportDir = "src/main/resources/compliantQueries/access";

//

    }
}
