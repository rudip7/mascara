package de.tub.dima.mascara.parser;

import de.tub.dima.mascara.utils.DebuggingTools;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.fun.SqlLibrary;
import org.apache.calcite.sql.fun.SqlLibraryOperatorTableFactory;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserImplFactory;
import org.apache.calcite.sql.parser.babel.SqlBabelParserImpl;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.sql.validate.SqlConformance;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.tools.*;

import java.util.EnumSet;

public class Parser {
    public final Planner planner;
    public final SchemaPlus schema;
    public final CalciteConnection connection;
    public final FrameworkConfig frameworkConfig;

    public Parser(CalciteConnection connection, String defaultSchema) {
        this.connection = connection;
        SqlOperatorTable operatorTable = SqlLibraryOperatorTableFactory.INSTANCE.getOperatorTable(
                SqlLibrary.POSTGRESQL);
        this.frameworkConfig = Frameworks.newConfigBuilder()
                .parserConfig(getParserConfig(connection.config()))
//                .operatorTable(SqlLibraryOperatorTableFactory.INSTANCE.getOperatorTable(
//                        SqlLibrary.POSTGRESQL))
                .sqlValidatorConfig(SqlValidator.Config.DEFAULT.withIdentifierExpansion(true))
                .defaultSchema(connection.getRootSchema().getSubSchema(defaultSchema))
                .build();

        this.planner = Frameworks.getPlanner(frameworkConfig);
        this.schema = connection.getRootSchema();
    }

    public static SqlParser.Config  getParserConfig(CalciteConnectionConfig config){
        SqlParser.ConfigBuilder parserConfig = SqlParser.configBuilder();
        parserConfig.setCaseSensitive(config.caseSensitive());
        parserConfig.setUnquotedCasing(config.unquotedCasing());
        parserConfig.setQuotedCasing(config.quotedCasing());
        parserConfig.setConformance(SqlConformanceEnum.BABEL);


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
