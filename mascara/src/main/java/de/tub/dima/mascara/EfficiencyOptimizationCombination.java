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
public class EfficiencyOptimizationCombination {
    private Map<String, MascaraMaster> mascaraMap;
    private Map<String, RelRoot> logicalPlans;

    private Map<String, List<CompliantPlan>> compliantPlans;
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
        logicalPlans = new HashMap<>();
        compliantPlans = new HashMap<>();

        List<String> c_policyNames = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            c_policyNames.add("c_e_"+i);
        }
        List<String> l_policyNames = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            l_policyNames.add("l_e_"+i);
        }
        List<String> o_policyNames = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            o_policyNames.add("o_e_"+i);
        }

        String queryString = readFile("src/main/resources/queries/efficiency/join_3.sql");

        List<String> policyNames_1 = new ArrayList<>();
        policyNames_1.addAll(c_policyNames.subList(0, 1));
        policyNames_1.addAll(l_policyNames.subList(0, 1));
        policyNames_1.addAll(o_policyNames.subList(0, 1));
        mascaraMap.put("pol_1", new MascaraMaster(connectionProperties, policyNames_1));
        logicalPlans.put("pol_1", mascaraMap.get("pol_1").getLogicalPlan(queryString));
        compliantPlans.put("pol_1", mascaraMap.get("pol_1").modify(logicalPlans.get("pol_1")));

        List<String> policyNames_2 = new ArrayList<>();
        policyNames_2.addAll(c_policyNames.subList(0, 2));
        policyNames_2.addAll(l_policyNames.subList(0, 2));
        policyNames_2.addAll(o_policyNames.subList(0, 2));
        mascaraMap.put("pol_2", new MascaraMaster(connectionProperties, policyNames_2));
        logicalPlans.put("pol_2", mascaraMap.get("pol_2").getLogicalPlan(queryString));
        compliantPlans.put("pol_2", mascaraMap.get("pol_2").modify(logicalPlans.get("pol_2")));

        List<String> policyNames_3 = new ArrayList<>();
        policyNames_3.addAll(c_policyNames.subList(0, 3));
        policyNames_3.addAll(l_policyNames.subList(0, 3));
        policyNames_3.addAll(o_policyNames.subList(0, 3));
        mascaraMap.put("pol_3", new MascaraMaster(connectionProperties, policyNames_3));
        logicalPlans.put("pol_3", mascaraMap.get("pol_3").getLogicalPlan(queryString));
        compliantPlans.put("pol_3", mascaraMap.get("pol_3").modify(logicalPlans.get("pol_3")));

        List<String> policyNames_4 = new ArrayList<>();
        policyNames_4.addAll(c_policyNames.subList(0, 4));
        policyNames_4.addAll(l_policyNames.subList(0, 4));
        policyNames_4.addAll(o_policyNames.subList(0, 4));
        mascaraMap.put("pol_4", new MascaraMaster(connectionProperties, policyNames_4));
        logicalPlans.put("pol_4", mascaraMap.get("pol_4").getLogicalPlan(queryString));
        compliantPlans.put("pol_4", mascaraMap.get("pol_4").modify(logicalPlans.get("pol_4")));

        List<String> policyNames_5 = new ArrayList<>();
        policyNames_5.addAll(c_policyNames.subList(0, 5));
        policyNames_5.addAll(l_policyNames.subList(0, 5));
        policyNames_5.addAll(o_policyNames.subList(0, 5));
        mascaraMap.put("pol_5", new MascaraMaster(connectionProperties, policyNames_5));
        logicalPlans.put("pol_5", mascaraMap.get("pol_5").getLogicalPlan(queryString));
        compliantPlans.put("pol_5", mascaraMap.get("pol_5").modify(logicalPlans.get("pol_5")));

    }
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(EfficiencyOptimizationCombination.class.getSimpleName())
                .resultFormat(ResultFormatType.CSV)  // Set result format to CSV
//                .result("src/main/resources/results/efficiency/optimization_combinations.csv")
                .result("src/main/resources/results/efficiency/optimization_combinations_10000.csv")
                .build();

        new Runner(opt).run();
    }

    @Param({"pol_5", "pol_4", "pol_3", "pol_2", "pol_1"}) // Add more configurations as needed
    private String configuration;

    @Benchmark
    @Fork(value = 1)
//    @Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
//    @Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    public List<CompliantPlan> benchmarkEstimate() throws Exception {
        MascaraMaster mascara = mascaraMap.get(configuration);
        RelRoot logicalPlan = logicalPlans.get(configuration);
        List<CompliantPlan> compliantPlan = compliantPlans.get(configuration);
        return mascara.estimateUtilityScores(logicalPlan, compliantPlan);
    }
}
