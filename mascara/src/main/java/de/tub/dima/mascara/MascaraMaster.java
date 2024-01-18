package de.tub.dima.mascara;

import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.modifier.QueryModifier;
import de.tub.dima.mascara.optimizer.QualityEstimator;
import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;
import de.tub.dima.mascara.parser.Parser;
import de.tub.dima.mascara.policies.PoliciesCatalog;
import de.tub.dima.mascara.utils.DebuggingTools;
import org.apache.calcite.rel.RelRoot;

import java.util.ArrayList;
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
        this.maskingFunctionsCatalog = new MaskingFunctionsCatalog();
        this.dbConnector = new DbConnector(connectionProperties, this.maskingFunctionsCatalog);
        this.parser = new Parser(this.dbConnector.calciteConnection, (String) connectionProperties.get("schema"));
        this.statsManager = StatisticsManager.getInstance();
        this.statsManager.setConnector(dbConnector);
        this.policiesCatalog = new PoliciesCatalog(this.dbConnector, this.parser, this.maskingFunctionsCatalog);
        this.queryModifier = new QueryModifier(this.parser, this.policiesCatalog);
    }

    public String optimalCompliantQuery(String sql) throws Exception {
        RelRoot logicalPlan = getLogicalPlan(sql);
        DebuggingTools.printPlan("[Logical plan]:", logicalPlan.rel);
        List<CompliantPlan> compliantPlans = modify(logicalPlan);
        QualityEstimator qualityEstimator = new QualityEstimator(logicalPlan.rel);
        List<Double> qualityScore = new ArrayList<>(compliantPlans.size());
        for (int i = 0; i < compliantPlans.size(); i++) {
            CompliantPlan compliantPlan = compliantPlans.get(i);
            qualityScore.add(qualityEstimator.estimate(compliantPlan, compliantPlan.queryAttributes));
        }
        System.out.println("Hala Madrid!");

        // TODO
        return "Hala Madrid!";
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
