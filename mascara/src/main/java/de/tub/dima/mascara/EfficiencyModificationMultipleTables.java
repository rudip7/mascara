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
public class EfficiencyModificationMultipleTables {
    private MascaraMaster mascara;
    private Map<String, String> queries;

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

        mascara = new MascaraMaster(connectionProperties, Arrays.asList("c_p1", "l_p1", "o_p1", "n", "s", "r"));

        queries = new HashMap<>();
        queries.put("join_1", readFile("src/main/resources/queries/efficiency/join_1.sql"));
        queries.put("join_2", readFile("src/main/resources/queries/efficiency/join_2.sql"));
        queries.put("join_3", readFile("src/main/resources/queries/efficiency/join_3.sql"));
        queries.put("join_4", readFile("src/main/resources/queries/efficiency/join_4.sql"));
        queries.put("join_5", readFile("src/main/resources/queries/efficiency/join_5.sql"));
        queries.put("join_6", readFile("src/main/resources/queries/efficiency/join_6.sql"));

    }
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(EfficiencyModificationMultipleTables.class.getSimpleName())
                .resultFormat(ResultFormatType.CSV)  // Set result format to CSV
                .result("src/main/resources/results/efficiency/modification_n_tables.csv")
                .build();

        new Runner(opt).run();
    }

    @Param({"join_6", "join_5", "join_4", "join_3", "join_2", "join_1"}) // Add more configurations as needed
    private String configuration;

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 20, time = 200, timeUnit = TimeUnit.MILLISECONDS)
    public List<CompliantPlan> benchmarkModify() throws Exception {
        String queryString = queries.get(configuration);
        RelRoot logicalPlan = mascara.getLogicalPlan(queryString);
        return mascara.modify(logicalPlan);
    }
}
