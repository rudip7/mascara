package de.tub.dima.mascara;

import de.tub.dima.mascara.policies.AccessControlPolicy;
import de.tub.dima.mascara.policies.AttributeMappings;
import org.apache.calcite.rel.RelRoot;

import java.util.ArrayList;
import java.util.List;

public class CompliantPlan {
    // TODO: Add new backup plan for the case of aggregate queries
    public RelRoot logicalPlan;
    public List<AccessControlPolicy> policies;
    public AttributeMappings queryAttributes;

    public String id = "";
    public Double utilityScore = Double.MAX_VALUE;

    public Double cardinalityDiff = 0.0;
    public List<String> attributes = new ArrayList<>();
    public List<Double> relEntropy = new ArrayList<>();
    public String compliantQuery;

    public String cardinalityQuery;

    public CompliantPlan(RelRoot logicalPlan, List<AccessControlPolicy> policies, AttributeMappings queryAttributes, RelRoot cardinalityPlan) {
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
        this.cardinalityQuery = MascaraMaster.planToSql(cardinalityPlan.rel);
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

    public void addAttributeEntropy(String attribute, Double entropy) {
        attributes.add(attribute);
        relEntropy.add(entropy);
    }

    public void setCardinalityDiff(Double cardinalityDiff) {
        this.cardinalityDiff = cardinalityDiff;
    }

    public String getCompliantQuery() {
        if (compliantQuery == null) {
            compliantQuery = MascaraMaster.planToSql(logicalPlan.rel);
        }
        return compliantQuery;
    }

    public String getCardinalityQuery() {
        return cardinalityQuery;
    }

    public List<AccessControlPolicy> getPolicies() {
        return policies;
    }
}
