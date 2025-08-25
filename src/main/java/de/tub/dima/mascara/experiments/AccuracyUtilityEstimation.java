package de.tub.dima.mascara.experiments;

import de.tub.dima.mascara.MascaraMaster;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static de.tub.dima.mascara.utils.Utils.readFile;

public class AccuracyUtilityEstimation {
    public static void main(String[] args) throws Exception {

//        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
//        System.setProperty("log4j.configurationFile", "log4j2.xml");

        System.out.println("Starting Mascara Accuracy Utility Estimation Experiment...");

        String baseDir = args.length > 0 ? args[0] : "src/main/resources";

        int statsSize = 100;
        if (args.length > 1 && args[1].matches("\\d+")) {
            statsSize = Integer.parseInt(args[0]);
            System.out.println("Customized statistics size: " + statsSize);
        }

        Properties connectionProperties = new Properties();
        FileInputStream input = new FileInputStream("src/main/resources/config.properties");
        connectionProperties.load(input);

        MascaraMaster mascara = new MascaraMaster(connectionProperties);

        System.out.println("Running simple SELECT * queries...");
        String queriesDir = baseDir + "/queries/tpch/access";
        String baseResultsDir = baseDir + "/results/ranking_plans/access";
        String reportDir = baseDir + "/compliantQueries/access";
        String resultsDir;
        if (statsSize == 100){
            resultsDir = baseResultsDir;
        } else {
            resultsDir = baseResultsDir.replace("ranking_plans", "ranking_plans_" + statsSize);
        }

        processQueries(mascara, queriesDir, resultsDir, reportDir);

        System.out.println("Running filter queries...");
        queriesDir = queriesDir.replace("access", "filter");
        reportDir = reportDir.replace("access", "filter");
        resultsDir = resultsDir.replace("access", "filter");

        processQueries(mascara, queriesDir, resultsDir, reportDir);

        System.out.println("Running join queries...");
        queriesDir = queriesDir.replace("filter", "join");
        reportDir = reportDir.replace("filter", "join");
        resultsDir = resultsDir.replace("filter", "join");

        processQueries(mascara, queriesDir, resultsDir, reportDir);

        System.out.println("Running final complex queries...");
        queriesDir = queriesDir.replace("join", "final");
        reportDir = reportDir.replace("join", "final");
        resultsDir = resultsDir.replace("join", "final");

        processQueries(mascara, queriesDir, resultsDir, reportDir);

        System.out.println("Mascara Accuracy Utility Estimation Experiment Finished.");
    }

    private static void processQueries(MascaraMaster mascara, String queriesDir, String resultsDir, String reportDir) {
        Path dir = Paths.get(queriesDir);
        try {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        if (fileName.endsWith(".sql")) {
                            // System.out.println("\n----------------------------------------\n");
                            // System.out.println("Starting processing: " + fileName);
                            String queryString = null;
                            try {
                                queryString = readFile(queriesDir + "/" + fileName);
                                mascara.optimalCompliantQuery(queryString,
                                        resultsDir + "/" + fileName.replace(".sql", ".csv"),
                                        reportDir + "/" + fileName.replace(".sql", ".json"),
                                        null
                                );
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
