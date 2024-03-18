package de.tub.dima.mascara;

import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;
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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static de.tub.dima.mascara.utils.Utils.readFile;

public class Playground {
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
        String queryString = readFile("src/main/resources/queries/tpch/aggregate/q10.sql");
////        String queryString = readFile("src/main/resources/queries/tpch/final/q3_c_l_o_final.sql");
//        RelRoot logicalPlan = mascara.getLogicalPlan(queryString);
//        List<CompliantPlan> modifiedQueries = mascara.modify(logicalPlan);
//        List<CompliantPlan> compliantQueries = mascara.estimateUtilityScores(logicalPlan, modifiedQueries);
//
//        CompliantPlan optimalQuery = compliantQueries.get(0);
//        System.out.println("-----------------------------");
//        System.out.println(optimalQuery.getId());
//        System.out.println(optimalQuery.getCardinalityQuery());

        mascara.parser.getLogicalPlanDebugging(queryString);






//        String compliantQueryDynamic = mascara.getCompliantQueryDynamic(logicalPlan, modifiedQueries);
//        System.out.printf(compliantQueryDynamic);
//        mascara.executeQuery(compliantQueryDynamic);


//        String queriesDir = "src/main/resources/queries/tpch/access";
//        String baseResultsDir = "src/main/resources/results/ranking_plans/access";
//        String reportDir = "src/main/resources/compliantQueries/access";

//        String queriesDir = "src/main/resources/queries/tpch/filter";
//        String baseResultsDir = "src/main/resources/results/ranking_plans/filter";
//        String reportDir = "src/main/resources/compliantQueries/filter";

//        String queriesDir = "src/main/resources/queries/tpch/join";
//        String baseResultsDir = "src/main/resources/results/ranking_plans/join";
//        String reportDir = "src/main/resources/compliantQueries/join";
//        String detailsReportDir = "src/main/resources/reports/join";

//        String queriesDir = "src/main/resources/queries/tpch/final";
//        String baseResultsDir = "src/main/resources/results/ranking_plans/final";
//        String reportDir = "src/main/resources/compliantQueries/final";

        // Rankings with larger Statistics
//        String resultsDir = baseResultsDir;
//        String resultsDir = baseResultsDir.replace("ranking_plans", "ranking_plans_1000");
//        String resultsDir = baseResultsDir.replace("ranking_plans", "ranking_plans_10000");
//        String resultsDir = baseResultsDir.replace("ranking_plans", "ranking_plans_base");
//        String resultsDir = baseResultsDir.replace("ranking_plans", "ranking_plans_base_10000");


//        Path dir = Paths.get(queriesDir);
//        try {
//            Files.walk(dir)
//                    .filter(Files::isRegularFile)
//                    .forEach(file -> {
//                        String fileName = file.getFileName().toString();
//                        if (fileName.endsWith(".sql")) {
//                            System.out.println("\n----------------------------------------\n");
//                            System.out.println("Starting processing: "+fileName);
//                            String queryString = null;
//                            try {
//                                queryString = readFile(queriesDir+ "/" + fileName);
//                                mascara.optimalCompliantQuery(queryString,
//                                        resultsDir + "/" + fileName.replace(".sql", ".csv"),
//                                        reportDir + "/" + fileName.replace(".sql", ".json"),
//                                        detailsReportDir + "/" + fileName.replace(".sql", ".csv")
//                                );
//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//
//                    });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}
