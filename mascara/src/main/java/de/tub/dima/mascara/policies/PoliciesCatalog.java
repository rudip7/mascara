package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.DbConnector;
import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.tub.dima.mascara.utils.Utils.readFile;

public class PoliciesCatalog {

    public List<AccessControlPolicy> policies;
    public MaskingFunctionsCatalog maskingFunctionsCatalog;

    public PoliciesCatalog(DbConnector dbConnector, Parser parser, MaskingFunctionsCatalog maskingFunctionsCatalog) throws Exception {
        this.policies = new ArrayList<>();
        this.maskingFunctionsCatalog = maskingFunctionsCatalog;

        List<String> policyNames = Arrays.asList(
                "c_p1",
                "l_p1",
                "l_p2",
                "o_p1",
                "o_p2"
                );

        for (int i = 0; i < policyNames.size(); i++) {
            String name = policyNames.get(i);
            String policyDefinition = dbConnector.getPolicyDefinition(name);
            if (policyDefinition.charAt(policyDefinition.length() - 1) == ';'){
                policyDefinition = policyDefinition.substring(0, policyDefinition.length() - 1);
            }
            policyDefinition = policyDefinition.replace("::text", "");
//            String policyDefinition = "SELECT lineitem.l_shipdate :: text AS l_shipdate\n" +
//                    "FROM lineitem";
            AccessControlPolicy policy = new AccessControlPolicy(policyDefinition, Arrays.asList(name), parser, maskingFunctionsCatalog);
//            policy.setStatistics();
//            policy.indexStats();
            this.policies.add(policy);
        }
    }
}
