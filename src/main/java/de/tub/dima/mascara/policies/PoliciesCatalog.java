package de.tub.dima.mascara.policies;

import de.tub.dima.mascara.utils.DbConnector;
import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PoliciesCatalog {

    public List<AccessControlPolicy> policies;
    public MaskingFunctionsCatalog maskingFunctionsCatalog;

    public PoliciesCatalog(DbConnector dbConnector, Parser parser, MaskingFunctionsCatalog maskingFunctionsCatalog) throws Exception {
        this(dbConnector, parser, maskingFunctionsCatalog, null);

    }
    public PoliciesCatalog(DbConnector dbConnector, Parser parser, MaskingFunctionsCatalog maskingFunctionsCatalog, List<String> policyNames) throws Exception {
        this.policies = new ArrayList<>();
        this.maskingFunctionsCatalog = maskingFunctionsCatalog;

        String schemaName = "public";
        if (policyNames == null){
            policyNames = Arrays.asList(
                    "c_p1",
                    "c_p2",
                    "l_p1",
                    "l_p2",
                    "l_p3",
                    "o_p1",
                    "o_p2",
                    "n",
                    "s",
                    "r"
            );
//        List<String> policyNames = Arrays.asList(
//                "a_p1",
//                "a_p2",
//                "a_p3",
//                "a_p4",
//                "a_p5"
//        );
        }

        for (int i = 0; i < policyNames.size(); i++) {
            String name = policyNames.get(i);
            String policyDefinition = dbConnector.getPolicyDefinition(name);
            if (policyDefinition.charAt(policyDefinition.length() - 1) == ';'){
                policyDefinition = policyDefinition.substring(0, policyDefinition.length() - 1);
            }
            policyDefinition = policyDefinition.replace("::text", "");
//            String policyDefinition = "SELECT lineitem.l_shipdate :: text AS l_shipdate\n" +
//                    "FROM lineitem";
            AccessControlPolicy policy = new AccessControlPolicy(policyDefinition, Arrays.asList(schemaName, name), parser, maskingFunctionsCatalog);
//            policy.setStatistics();
//            policy.indexStats();
            this.policies.add(policy);
        }
    }
}
