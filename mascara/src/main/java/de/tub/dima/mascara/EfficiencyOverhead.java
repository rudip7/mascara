package de.tub.dima.mascara;

import org.apache.calcite.rel.RelRoot;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static de.tub.dima.mascara.utils.Utils.readFile;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class EfficiencyOverhead {
    private Map<String, MascaraMaster> mascaraMap;

    private Map<String, String> queries;

    private Map<String, RelRoot> logicalPlans;

    private Map<String, List<CompliantPlan>> compliantPlans;

    private Map<String, String> compliantqueries;

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

        mascaraMap = new HashMap<>();
        mascaraMap.put("q1", new MascaraMaster(connectionProperties));
        mascaraMap.put("q3", new MascaraMaster(connectionProperties));
        mascaraMap.put("q5", new MascaraMaster(connectionProperties));
        mascaraMap.put("q6", new MascaraMaster(connectionProperties));
        mascaraMap.put("q10", new MascaraMaster(connectionProperties));

        queries = new HashMap<>();
        compliantqueries = new HashMap<>();

        queries.put("q1", readFile("src/main/resources/queries/tpch/aggregate/q1.sql"));
        queries.put("q3", readFile("src/main/resources/queries/tpch/aggregate/q3.sql"));
        queries.put("q5", readFile("src/main/resources/queries/tpch/aggregate/q5.sql"));
        queries.put("q6", readFile("src/main/resources/queries/tpch/aggregate/q6.sql"));
        queries.put("q10", readFile("src/main/resources/queries/tpch/aggregate/q10.sql"));

        logicalPlans = new HashMap<>();
        compliantPlans = new HashMap<>();

        String queryString = queries.get("q1");
        MascaraMaster mascara = mascaraMap.get("q1");
        logicalPlans.put("q1", mascara.getLogicalPlan(queryString));
        compliantPlans.put("q1", mascara.modify(logicalPlans.get("q1")));
        compliantqueries.put("q1", mascara.getCompliantQuery(logicalPlans.get("q1"), compliantPlans.get("q1")));

        queryString = queries.get("q3");
        mascara = mascaraMap.get("q3");
        logicalPlans.put("q3", mascara.getLogicalPlan(queryString));
        compliantPlans.put("q3", mascara.modify(logicalPlans.get("q3")));
        compliantqueries.put("q3", mascara.getCompliantQuery(logicalPlans.get("q3"), compliantPlans.get("q3")));

        queryString = queries.get("q5");
        mascara = mascaraMap.get("q5");
        logicalPlans.put("q5", mascara.getLogicalPlan(queryString));
        compliantPlans.put("q5", mascara.modify(logicalPlans.get("q5")));
        compliantqueries.put("q5", mascara.getCompliantQuery(logicalPlans.get("q5"), compliantPlans.get("q5")));

        queryString = queries.get("q6");
        mascara = mascaraMap.get("q6");
        logicalPlans.put("q6", mascara.getLogicalPlan(queryString));
        compliantPlans.put("q6", mascara.modify(logicalPlans.get("q6")));
        compliantqueries.put("q6", mascara.getCompliantQuery(logicalPlans.get("q6"), compliantPlans.get("q6")));

        queryString = queries.get("q10");
        mascara = mascaraMap.get("q10");
        logicalPlans.put("q10", mascara.getLogicalPlan(queryString));
        compliantPlans.put("q10", mascara.modify(logicalPlans.get("q10")));
        compliantqueries.put("q10", mascara.getCompliantQuery(logicalPlans.get("q10"), compliantPlans.get("q10")));
    }
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(EfficiencyOverhead.class.getSimpleName())
                .resultFormat(ResultFormatType.CSV)  // Set result format to CSV
                .result("src/main/resources/results/efficiency/overhead.csv")
                .build();

        new Runner(opt).run();
    }

    @Param({"q1", "q3", "q5", "q6", "q10"}) // Add more configurations as needed
    private String configuration;

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 20, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public List<CompliantPlan> benchmarkModify() throws Exception {
        MascaraMaster mascara = mascaraMap.get(configuration);
        String queryString = queries.get(configuration);
        RelRoot logicalPlan = mascara.getLogicalPlan(queryString);
        return mascara.modify(logicalPlan);
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 20, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public List<CompliantPlan> benchmarkEstimate() throws Exception {
        MascaraMaster mascara = mascaraMap.get(configuration);
        RelRoot logicalPlan = logicalPlans.get(configuration);
        List<CompliantPlan> compliantPlan = compliantPlans.get(configuration);
        return mascara.estimateUtilityScores(logicalPlan, compliantPlan);
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 20, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public boolean benchmarkExecutingCompliant() throws Exception {
        MascaraMaster mascara = mascaraMap.get(configuration);
        String compliantQuery = compliantqueries.get(configuration);
        return mascara.executeQuery(compliantQuery);
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 20, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public boolean benchmarkExecuting() throws Exception {
        MascaraMaster mascara = mascaraMap.get(configuration);
        String query = queries.get(configuration);
        return mascara.executeQuery(query);
    }
}
