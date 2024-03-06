package de.tub.dima.mascara;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.modifier.QueryModifier;
import de.tub.dima.mascara.optimizer.QualityEstimator;
import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;
import de.tub.dima.mascara.parser.Parser;
import de.tub.dima.mascara.policies.PoliciesCatalog;
import de.tub.dima.mascara.utils.CompliantQueriesTracker;
import de.tub.dima.mascara.utils.DebuggingTools;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class MascaraMaster {

    public DbConnector dbConnector;
    public Parser parser;
    public PoliciesCatalog policiesCatalog;
    public MaskingFunctionsCatalog maskingFunctionsCatalog;
    public QueryModifier queryModifier;
    public StatisticsManager statsManager;

    public MascaraMaster(Properties connectionProperties) throws Exception {
        this(connectionProperties, null);
    }

    public MascaraMaster(Properties connectionProperties, List<String> policyNames) throws Exception {
        this.maskingFunctionsCatalog = new MaskingFunctionsCatalog();
        this.dbConnector = new DbConnector(connectionProperties, this.maskingFunctionsCatalog);
        this.parser = new Parser(this.dbConnector.calciteConnection, (String) connectionProperties.get("schema"));
        this.statsManager = StatisticsManager.getInstance();
        this.statsManager.setConnector(dbConnector);
        this.policiesCatalog = new PoliciesCatalog(this.dbConnector, this.parser, this.maskingFunctionsCatalog, policyNames);
        this.queryModifier = new QueryModifier(this.parser, this.policiesCatalog);
    }


    public String optimalCompliantQuery(String sql) throws Exception {
        return optimalCompliantQuery(sql, null, null, null);
    }

    public String optimalCompliantQuery(String sql, String outputDir, String reportDir, String detailedReport) throws Exception {
        System.out.println("[Requested Query]:\n");
        System.out.println(sql);

        RelRoot logicalPlan = getLogicalPlan(sql);
        DebuggingTools.printPlan("[Logical plan]:", logicalPlan.rel);
        List<CompliantPlan> compliantPlans = modify(logicalPlan);
        QualityEstimator qualityEstimator = new QualityEstimator(logicalPlan.rel, dbConnector);
//        List<Pair<String, Double>> utilityScore = new ArrayList<>(compliantPlans.size());
        for (int i = 0; i < compliantPlans.size(); i++) {
            CompliantPlan compliantPlan = compliantPlans.get(i);
            double score = qualityEstimator.estimate(compliantPlan, compliantPlan.queryAttributes);
            System.out.println(compliantPlan.getId()+": Total Utility Score: " + score+"\n");

        }
        compliantPlans.sort((o1, o2) -> o1.getUtilityScore().compareTo(o2.getUtilityScore()));


        if (reportDir != null){
            CompliantQueriesTracker tracker = new CompliantQueriesTracker(sql);
            for (CompliantPlan plan : compliantPlans) {
                tracker.addCompliantQuery(plan.getId(), plan.getCompliantQuery());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(reportDir), tracker);
        }
        if(outputDir != null){
            try {
                PrintWriter writer = new PrintWriter(outputDir, "UTF-8");
                writer.println("plan_id,utility_score");
                for (CompliantPlan plan : compliantPlans) {
                    writer.println(plan.getId() + "," + plan.getUtilityScore());
                }
                writer.close();
            } catch (IOException e) {
                System.out.println("An error occurred while writing to the file.");
                e.printStackTrace();
            }
        }

        if(detailedReport != null){
            try {
                PrintWriter writer = new PrintWriter(detailedReport, "UTF-8");
                writer.println("plan_id,utility_score,cardinality_diff,attributes,rel_entropy");
                for (CompliantPlan plan : compliantPlans) {
                    writer.println(plan.getId() + "," + plan.getUtilityScore()+ "," + plan.cardinalityDiff + ",\"" + plan.attributes.toString()+ "\",\"" + plan.relEntropy.toString() + "\"");
                }
                writer.close();
            } catch (IOException e) {
                System.out.println("An error occurred while writing to the file.");
                e.printStackTrace();
            }
        }


        String compliantQuery = planToSql(compliantPlans.get(0).logicalPlan.rel);
        System.out.println("Optimal Compliant Query: " + compliantQuery);
        return compliantQuery;
    }

    public List<CompliantPlan> estimateUtilityScores(RelRoot logicalPlan, List<CompliantPlan> compliantPlans) throws SQLException {
        QualityEstimator qualityEstimator = new QualityEstimator(logicalPlan.rel, dbConnector);
        for (int i = 0; i < compliantPlans.size(); i++) {
            CompliantPlan compliantPlan = compliantPlans.get(i);
            qualityEstimator.estimate(compliantPlan, compliantPlan.queryAttributes);
//            System.out.println(compliantPlan.getId()+": Total Utility Score: " + compliantPlan.getUtilityScore()+"\n");
        }
        compliantPlans.sort((o1, o2) -> o1.getUtilityScore().compareTo(o2.getUtilityScore()));
        return compliantPlans;
    }

    public String getCompliantQuery(RelRoot logicalPlan, List<CompliantPlan> compliantPlans) throws SQLException {
        estimateUtilityScores(logicalPlan, compliantPlans);
        return planToSql(compliantPlans.get(0).logicalPlan.rel);
    }

    public static String planToSql(RelNode plan){
        SqlDialect sqlDialect = new SqlDialect(SqlDialect.EMPTY_CONTEXT);
        SqlNode sqlNode = new RelToSqlConverter(sqlDialect).visitRoot(plan).asStatement();
        return sqlNode.toSqlString(sqlDialect).getSql().replace("$", "S");
    }

    public boolean executeQuery(String sql) throws SQLException {
        return dbConnector.executeQuery(sql);
    }

    public void collectStatistics(){
//        dbConnector.
    }

    public RelRoot getLogicalPlan(String sql) throws Exception {
        RelRoot logicalPlan = this.parser.getLogicalPlan(sql);
        this.parser.reset();
        return logicalPlan;
    }

    public List<CompliantPlan> modify(RelRoot logicalPlan){
        List<CompliantPlan> compliantPlans = this.queryModifier.getCompliantPlans(logicalPlan);
        return compliantPlans;
    }

    public Parser getParser() {
        return parser;
    }

    public MaskingFunctionsCatalog getMaskingFunctionsCatalog() {
        return maskingFunctionsCatalog;
    }
}
