package de.tub.dima.mascara.modifier;

import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelVisitor;
import org.apache.calcite.rel.core.TableScan;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BaseTablesExtractor extends RelVisitor {
    public ArrayList<RelOptTable> baseTables;
    public BaseTablesExtractor() {
        baseTables = new ArrayList<>();
    }
    @Override
    public void visit(RelNode node, int ordinal, @Nullable RelNode parent) {
        if (node instanceof TableScan) {
            if (!baseTables.contains(node.getTable())){
                baseTables.add(node.getTable());
//                List<String> name = node.getTable().getQualifiedName();
//                System.out.println(name);
            }
        }
        super.visit(node, ordinal, parent);
    }
}
