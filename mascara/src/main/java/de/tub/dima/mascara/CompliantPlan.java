package de.tub.dima.mascara;

import de.tub.dima.mascara.policies.AccessControlPolicy;
import de.tub.dima.mascara.policies.AttributeMappings;
import org.apache.calcite.rel.RelRoot;

import java.util.List;

public class CompliantPlan {
    // TODO: Add new backup plan for the case of aggregate queries
    public RelRoot logicalPlan;
    public List<AccessControlPolicy> policies;
    public AttributeMappings queryAttributes;

    public String id = "";
    public Double utilityScore = Double.MAX_VALUE;

    public String compliantQuery;

    public CompliantPlan(RelRoot logicalPlan, List<AccessControlPolicy> policies, AttributeMappings queryAttributes) {
        this.logicalPlan = logicalPlan;
        this.policies = policies;
        this.queryAttributes = queryAttributes;
        int size = policies.size();
        for (int i = 0; i < size; i++) {
            AccessControlPolicy policy = policies.get(i);
            this.id += policy.policyName.get(1);
            if (i < size - 1) {
                this.id += "-";
            }
        }
        this.compliantQuery = MascaraMaster.planToSql(logicalPlan.rel);
    }

    public String getId() {
        return id;
    }

    public void setUtilityScore(double utilityScore) {
        this.utilityScore = utilityScore;
    }

    public Double getUtilityScore() {
        return utilityScore;
    }

    public void setCompliantQuery(String compliantQuery) {
        this.compliantQuery = compliantQuery;
    }

    public String getCompliantQuery() {
        if (compliantQuery == null) {
            compliantQuery = MascaraMaster.planToSql(logicalPlan.rel);
        }
        return compliantQuery;
    }
}
