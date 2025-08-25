package de.tub.dima.mascara.experiments;

import de.tub.dima.mascara.MascaraMaster;

import java.io.FileInputStream;
import java.util.Properties;

import static de.tub.dima.mascara.utils.Utils.readFile;

public class ACSExperiment {
    public static void main(String[] args) throws Exception {
        // Simple connection implementation for loading schema from sales.json

        Properties connectionProperties = new Properties();
//        connectionProperties.put("url", "jdbc:postgresql://localhost:5432/mascaradb");
        FileInputStream input = new FileInputStream("src/main/resources/config.properties");
        connectionProperties.load(input);

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
