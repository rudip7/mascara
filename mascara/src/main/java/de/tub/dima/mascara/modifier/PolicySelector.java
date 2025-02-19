package de.tub.dima.mascara.modifier;

import de.tub.dima.mascara.policies.AccessControlPolicy;
import de.tub.dima.mascara.policies.PoliciesCatalog;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.tools.FrameworkConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PolicySelector {
    public PoliciesCatalog policiesCatalog;
    public FrameworkConfig frameworkConfig;



    public PolicySelector(PoliciesCatalog policiesCatalog, FrameworkConfig frameworkConfig) {
        this.policiesCatalog = policiesCatalog;
        this.frameworkConfig = frameworkConfig;
    }

    public HashMap<RelOptTable, List<AccessControlPolicy>> selectCandidatePolicies(RelRoot logicalPlan){
        HashMap<RelOptTable, List<AccessControlPolicy>> candidatePolicies = new HashMap<>();
        List<RelOptTable> baseTables = getBaseTables(logicalPlan);

        for (AccessControlPolicy policy :
            this.policiesCatalog.policies) {
            for (RelOptTable baseTable :
                 baseTables) {
                if (policy.applyToTable(baseTable)){
                    if (!candidatePolicies.containsKey(baseTable)){
                        candidatePolicies.put(baseTable, new ArrayList<>());
                    }
                    candidatePolicies.get(baseTable).add(policy);
                }
            }
        }
        return candidatePolicies;
    }

    public List<RelOptTable> getBaseTables(RelRoot logicalPlan){
        BaseTablesExtractor baseTablesExtractor = new BaseTablesExtractor(frameworkConfig);
        baseTablesExtractor.go(logicalPlan.rel);
        return baseTablesExtractor.baseTables;
    }
}
