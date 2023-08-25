package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;
import de.tub.dima.mascara.parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.tub.dima.mascara.utils.Utils.readFile;

public class PoliciesCatalog {

    public List<AccessControlPolicy> policies;
    public MaskingFunctionsCatalog maskingFunctionsCatalog;

    public PoliciesCatalog(Parser parser, MaskingFunctionsCatalog maskingFunctionsCatalog) throws Exception {
        this.policies = new ArrayList<>();
        this.maskingFunctionsCatalog = maskingFunctionsCatalog;
        // Hard-coded for now
        List<String> policyPaths = Arrays.asList("src/main/resources/policies/masked_low.sql");
        List<List<String>> policyNames = Arrays.asList(Arrays.asList("public", "Masked_low"));
        for (int i = 0; i < policyPaths.size(); i++) {
            String path = policyPaths.get(i);
            List<String> name = policyNames.get(i);
            String policyString = readFile(path);
            AccessControlPolicy policy = new AccessControlPolicy(policyString, name, parser, maskingFunctionsCatalog);
//            policy.setStatistics();
//            policy.indexStats();
            this.policies.add(policy);
        }
    }
}
