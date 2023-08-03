package de.tub.dima.mascara.parser;

import de.tub.dima.mascara.utils.DebuggingTools;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.*;

public class Parser {
    public final Planner planner;
    public final SchemaPlus schema;
    public final CalciteConnection connection;
    public final FrameworkConfig frameworkConfig;

    public Parser(CalciteConnection connection) {
        this.connection = connection;

        this.frameworkConfig = Frameworks.newConfigBuilder()
                .parserConfig(getParserConfig(connection.config()))
                .defaultSchema(connection.getRootSchema())
                .build();

        this.planner = Frameworks.getPlanner(frameworkConfig);
        this.schema = connection.getRootSchema();
    }

    public static SqlParser.Config  getParserConfig(CalciteConnectionConfig config){
        SqlParser.ConfigBuilder parserConfig = SqlParser.configBuilder();
        parserConfig.setCaseSensitive(config.caseSensitive());
        parserConfig.setUnquotedCasing(config.unquotedCasing());
        parserConfig.setQuotedCasing(config.quotedCasing());
        parserConfig.setConformance(config.conformance());

        return parserConfig.build();
    }

    public SqlNode parse(String sql) throws Exception {
        return planner.parse(sql);
    }

    public SqlNode validate(SqlNode node) throws ValidationException {
        return planner.validate(node);
    }

    public RelRoot rel(SqlNode node) throws RelConversionException {
        return planner.rel(node);
    }

    public RelRoot getLogicalPlan(String sql) throws Exception {
        SqlNode parsedQuery = planner.parse(sql);
        SqlNode validatedQuery = planner.validate(parsedQuery);
        return planner.rel(validatedQuery);
    }

    public RelRoot getLogicalPlanDebugging(String sql) throws Exception {
        System.out.println("\n[Requested Query]:");
        System.out.println(sql);
        SqlNode parsedQuery = planner.parse(sql);
        System.out.println("\n[Parsed query]:");
        System.out.println(parsedQuery.toString());
        SqlNode validatedQuery = planner.validate(parsedQuery);
        System.out.println("\n[Validated tree]:");
        System.out.println(validatedQuery.toString());
        RelRoot relRoot = planner.rel(validatedQuery);
        RelNode logicalPlan = relRoot.project();
        DebuggingTools.printPlan("[Logical plan]:", logicalPlan);
        return relRoot;
    }

    public void reset(){
        planner.close();
        planner.reset();
    }

    public FrameworkConfig getFrameworkConfig(){
        return this.frameworkConfig;
    }
}
