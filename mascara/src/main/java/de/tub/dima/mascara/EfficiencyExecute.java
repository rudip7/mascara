package de.tub.dima.mascara;

import org.apache.calcite.rel.RelRoot;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static de.tub.dima.mascara.utils.Utils.readFile;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class EfficiencyExecute {
    public MascaraMaster mascara;

    private Map<String, String> queries;


    private Map<String, String> staticQueries;
    private Map<String, String> dynamicQueries;

    @Setup
    public void setup() throws Exception {
        // Initialize Mascara and queryString here
        Properties connectionProperties = new Properties();
//        connectionProperties.put("url", "jdbc:postgresql://localhost:5432/mascaradb");
        connectionProperties.put("url", "jdbc:postgresql://localhost:5432/tpchdb");
        connectionProperties.put("driverClassName", "org.postgresql.Driver");
        connectionProperties.put("username", "postgres");
        connectionProperties.put("user", "postgres");
        connectionProperties.put("password", "1902");
        connectionProperties.put("schema", "public");

        mascara = new MascaraMaster(connectionProperties);

        queries = new HashMap<>();
        staticQueries = new HashMap<>();
        dynamicQueries = new HashMap<>();

        queries.put("q1", readFile("src/main/resources/queries/tpch/aggregate/q1.sql"));
        queries.put("q3", readFile("src/main/resources/queries/tpch/aggregate/q3.sql"));
        queries.put("q5", readFile("src/main/resources/queries/tpch/aggregate/q5.sql"));
        queries.put("q6", readFile("src/main/resources/queries/tpch/aggregate/q6.sql"));
        queries.put("q10", readFile("src/main/resources/queries/tpch/aggregate/q10.sql"));

        String queryString = queries.get("q1");
        RelRoot logicalPlan = mascara.getLogicalPlan(queryString);
        List<CompliantPlan> modified = mascara.modify(logicalPlan);
        List<CompliantPlan> compliantPlans = mascara.estimateUtilityScores(logicalPlan, modified);
        CompliantPlan optimalPlan = compliantPlans.get(0);
        staticQueries.put("q1", MascaraMaster.planToSql(optimalPlan.logicalPlan.rel));
        dynamicQueries.put("q1", mascara.makeDynamic(optimalPlan));


        queryString = queries.get("q3");
        logicalPlan = mascara.getLogicalPlan(queryString);
        modified = mascara.modify(logicalPlan);
        compliantPlans = mascara.estimateUtilityScores(logicalPlan, modified);
        optimalPlan = compliantPlans.get(0);
        staticQueries.put("q3", MascaraMaster.planToSql(optimalPlan.logicalPlan.rel));
        dynamicQueries.put("q3", mascara.makeDynamic(optimalPlan));

        queryString = queries.get("q5");
        logicalPlan = mascara.getLogicalPlan(queryString);
        modified = mascara.modify(logicalPlan);
        compliantPlans = mascara.estimateUtilityScores(logicalPlan, modified);
        optimalPlan = compliantPlans.get(0);
        staticQueries.put("q5", MascaraMaster.planToSql(optimalPlan.logicalPlan.rel));
        dynamicQueries.put("q5", mascara.makeDynamic(optimalPlan));

        queryString = queries.get("q6");
        logicalPlan = mascara.getLogicalPlan(queryString);
        modified = mascara.modify(logicalPlan);
        compliantPlans = mascara.estimateUtilityScores(logicalPlan, modified);
        optimalPlan = compliantPlans.get(0);
        staticQueries.put("q6", MascaraMaster.planToSql(optimalPlan.logicalPlan.rel));
        dynamicQueries.put("q6", mascara.makeDynamic(optimalPlan));

        queryString = queries.get("q10");
        logicalPlan = mascara.getLogicalPlan(queryString);
        modified = mascara.modify(logicalPlan);
        compliantPlans = mascara.estimateUtilityScores(logicalPlan, modified);
        optimalPlan = compliantPlans.get(0);
        staticQueries.put("q10", MascaraMaster.planToSql(optimalPlan.logicalPlan.rel));
        dynamicQueries.put("q10", mascara.makeDynamic(optimalPlan));

    }
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(EfficiencyExecute.class.getSimpleName())
                .resultFormat(ResultFormatType.CSV)  // Set result format to CSV
                .result("src/main/resources/results/efficiency/executing.csv")
//                .result("src/main/resources/results/efficiency/executing_10000.csv")
                .build();

        new Runner(opt).run();
    }

    @Param({"q1", "q3", "q5", "q6", "q10"}) // Add more configurations as needed
    private String configuration;


    @Benchmark
    @Fork(value = 1)
//    @Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
//    @Measurement(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    public boolean benchmarkExecutingStatic() throws Exception {
        String compliantQuery = staticQueries.get(configuration);
        return mascara.executeQuery(compliantQuery);
    }

    @Benchmark
    @Fork(value = 1)
//    @Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
//    @Measurement(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    public boolean benchmarkExecutingDynamic() throws Exception {
        String compliantQuery = dynamicQueries.get(configuration);
        return mascara.executeQuery(compliantQuery);
    }

    @Benchmark
    @Fork(value = 1)
//    @Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
//    @Measurement(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    public boolean benchmarkExecuting() throws Exception {
        String query = queries.get(configuration);
        return mascara.executeQuery(query);
    }
}
