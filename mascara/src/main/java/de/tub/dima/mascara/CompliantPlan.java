package de.tub.dima.mascara;

import de.tub.dima.mascara.policies.AccessControlPolicy;
import org.apache.calcite.rel.RelRoot;

import java.util.List;

public class CompliantPlan {
    public RelRoot logicalPlan;
    public List<AccessControlPolicy> policies;

    public CompliantPlan(RelRoot logicalPlan, List<AccessControlPolicy> policies) {
        this.logicalPlan = logicalPlan;
        this.policies = policies;
    }
}
