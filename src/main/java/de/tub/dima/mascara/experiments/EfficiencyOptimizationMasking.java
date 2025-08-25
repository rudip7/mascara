package de.tub.dima.mascara.experiments;

import de.tub.dima.mascara.modifier.CompliantPlan;
import de.tub.dima.mascara.MascaraMaster;
import org.apache.calcite.rel.RelRoot;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static de.tub.dima.mascara.utils.Utils.readFile;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class EfficiencyOptimizationMasking {
    private MascaraMaster mascara;

    private Map<String, RelRoot> logicalPlans;

    private Map<String, List<CompliantPlan>> compliantPlans;

    @Setup
    public void setup() throws Exception {
        // Initialize Mascara and queryString here
        Properties connectionProperties = new Properties();
        FileInputStream input = new FileInputStream("src/main/resources/config.properties");
        connectionProperties.load(input);

        mascara = new MascaraMaster(connectionProperties, Arrays.asList("c_p1", "l_p1", "o_p1", "n", "s", "r"));

        logicalPlans = new HashMap<>();
        compliantPlans = new HashMap<>();

        String queryString = readFile("src/main/resources/queries/efficiency/mask_1.sql");
        logicalPlans.put("mask_1", mascara.getLogicalPlan(queryString));
        compliantPlans.put("mask_1", mascara.modify(logicalPlans.get("mask_1")));

        queryString = readFile("src/main/resources/queries/efficiency/mask_2.sql");
        logicalPlans.put("mask_2", mascara.getLogicalPlan(queryString));
        compliantPlans.put("mask_2", mascara.modify(logicalPlans.get("mask_2")));

        queryString = readFile("src/main/resources/queries/efficiency/mask_4.sql");
        logicalPlans.put("mask_4", mascara.getLogicalPlan(queryString));
        compliantPlans.put("mask_4", mascara.modify(logicalPlans.get("mask_4")));

        queryString = readFile("src/main/resources/queries/efficiency/mask_8.sql");
        logicalPlans.put("mask_8", mascara.getLogicalPlan(queryString));
        compliantPlans.put("mask_8", mascara.modify(logicalPlans.get("mask_8")));

        queryString = readFile("src/main/resources/queries/efficiency/mask_16.sql");
        logicalPlans.put("mask_16", mascara.getLogicalPlan(queryString));
        compliantPlans.put("mask_16", mascara.modify(logicalPlans.get("mask_16")));
    }
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(EfficiencyOptimizationMasking.class.getSimpleName())
                .resultFormat(ResultFormatType.CSV)  // Set result format to CSV
                .result("src/main/resources/results/efficiency/optimization_masking.csv")
//                .result("src/main/resources/results/efficiency/optimization_masking_10000.csv")
                .build();

        new Runner(opt).run();
    }

    @Param({"mask_16", "mask_8", "mask_4", "mask_2", "mask_1"}) // Add more configurations as needed
    private String configuration;

    @Benchmark
    @Fork(value = 1)
//    @Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
//    @Measurement(iterations = 20, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.NANOSECONDS)
    public List<CompliantPlan> benchmarkEstimate() throws Exception {
        RelRoot logicalPlan = logicalPlans.get(configuration);
        List<CompliantPlan> compliantPlan = compliantPlans.get(configuration);
        return mascara.estimateUtilityScores(logicalPlan, compliantPlan);
    }
}
