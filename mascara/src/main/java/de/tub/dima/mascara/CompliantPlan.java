package de.tub.dima.mascara;

import de.tub.dima.mascara.policies.AccessControlPolicy;
import de.tub.dima.mascara.policies.AttributeMappings;
import org.apache.calcite.rel.RelRoot;

import java.util.List;

public class CompliantPlan {
    public RelRoot logicalPlan;
    public List<AccessControlPolicy> policies;
    public AttributeMappings queryAttributes;

    public CompliantPlan(RelRoot logicalPlan, List<AccessControlPolicy> policies, AttributeMappings queryAttributes) {
        this.logicalPlan = logicalPlan;
        this.policies = policies;
        this.queryAttributes = queryAttributes;
    }
}
