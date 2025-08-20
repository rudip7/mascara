package de.tub.dima.mascara.modifier;

import de.tub.dima.mascara.parser.Parser;
import de.tub.dima.mascara.policies.AccessControlPolicy;
import de.tub.dima.mascara.policies.PoliciesCatalog;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.tools.RelBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryModifier {
    public final Parser parser;
    public final PolicySelector policySelector;
    public RelBuilder builder;
    public QueryModifier(Parser parser, PoliciesCatalog policiesCatalog) {
        this.parser = parser;
        this.policySelector = new PolicySelector(policiesCatalog, parser.getFrameworkConfig());
        this.builder = RelBuilder.create(parser.getFrameworkConfig());
    }

    public List<CompliantPlan> getCompliantPlans(RelRoot logicalPlan){
        HashMap<RelOptTable, List<AccessControlPolicy>> candidatePolicies = policySelector.selectCandidatePolicies(logicalPlan);


        // TODO maybe RelNode instead of RelRoot
        ArrayList<CompliantPlan> compliantPlans = new ArrayList<>();

        List<Map<RelOptTable, AccessControlPolicy>> policyCombinations = generatePolicyCombinations(candidatePolicies);
        for (Map<RelOptTable, AccessControlPolicy> policies:
             policyCombinations) {
            CompliantPlanner compliantPlanner = new CompliantPlanner(parser.getFrameworkConfig(), policies);
            compliantPlanner.go(logicalPlan.rel);
            RelNode compliantPlan = compliantPlanner.builder.build();
            
            if (compliantPlan != null){
//                DebuggingTools.printPlan("[Compliant plan]:", compliantPlan);
                compliantPlans.add(new CompliantPlan(RelRoot.of(compliantPlan, logicalPlan.kind), new ArrayList<>(policies.values()), compliantPlanner.getAttributeMapping(), RelRoot.of(compliantPlanner.cardinalityPlan, logicalPlan.kind)));
            }
        }
        return compliantPlans;
    }

    private static List<Map<RelOptTable, AccessControlPolicy>> generatePolicyCombinations(Map<RelOptTable, List<AccessControlPolicy>> hashMap) {
        List<Map<RelOptTable, AccessControlPolicy>> policyCombinations = new ArrayList<>();
        int totalKeys = hashMap.size();
        Object[] tables = hashMap.keySet().toArray();
        int[] keyIndices = new int[totalKeys];
        List<List<AccessControlPolicy>> valueLists = new ArrayList<>(hashMap.values());

        while (true) {
            // Generate a combination
            Map<RelOptTable, AccessControlPolicy> currentCombination = new HashMap<>();
            for (int i = 0; i < totalKeys; i++) {
                RelOptTable key = (RelOptTable) tables[i];
                AccessControlPolicy value = valueLists.get(i).get(keyIndices[i]);
                currentCombination.put(key, value);
            }
            policyCombinations.add(currentCombination);

            // Move to the next combination
            int currentKeyIndex = totalKeys - 1;
            while (currentKeyIndex >= 0) {
                keyIndices[currentKeyIndex]++;
                if (keyIndices[currentKeyIndex] < valueLists.get(currentKeyIndex).size()) {
                    // Combination found
                    break;
                } else {
                    // Reset current key index and move to the previous key
                    keyIndices[currentKeyIndex] = 0;
                    currentKeyIndex--;
                }
            }

            if (currentKeyIndex < 0) {
                // No more combinations, exit the loop
                break;
            }
        }

        return policyCombinations;
    }
}
