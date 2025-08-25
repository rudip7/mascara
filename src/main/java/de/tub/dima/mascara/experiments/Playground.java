package de.tub.dima.mascara.experiments;

import de.tub.dima.mascara.MascaraMaster;
import de.tub.dima.mascara.modifier.CompliantPlan;
import org.apache.calcite.rel.RelRoot;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import static de.tub.dima.mascara.utils.Utils.readFile;

public class Playground {
    public static void main(String[] args) throws Exception {
        // Simple connection implementation for loading schema from sales.json

        Properties connectionProperties = new Properties();
        FileInputStream input = new FileInputStream("src/main/resources/config.properties");
        connectionProperties.load(input);

        MascaraMaster mascara = new MascaraMaster(connectionProperties);

        String queryString = readFile("src/main/resources/queries/test/test.sql");

        RelRoot logicalPlan = mascara.parser.getLogicalPlanDebugging(queryString);
        List<CompliantPlan> modifiedQueries = mascara.modify(logicalPlan);
        List<CompliantPlan> compliantQueries = mascara.estimateUtilityScores(logicalPlan, modifiedQueries);

        System.out.println("Number of compliant plans: " + compliantQueries.size());

        CompliantPlan optimalQuery = compliantQueries.get(0);
        System.out.println("-----------------------------");
        System.out.println("Optimal plan:");
        System.out.println(optimalQuery.getCardinalityQuery());
        System.out.println("\nUtility Score: " +optimalQuery.getUtilityScore());

    }
}
