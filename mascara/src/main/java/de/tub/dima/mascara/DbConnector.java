package de.tub.dima.mascara;

import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnector {
    public final CalciteConnection connection;
    public DbConnector(
            Properties connectionProperties, MaskingFunctionsCatalog maskingFunctionsCatalog
    ) throws SQLException, ClassNotFoundException {
        Properties configProperties = new Properties();
        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.FALSE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        this.connection = DriverManager.getConnection("jdbc:calcite:", configProperties)
                .unwrap(CalciteConnection.class);

        SchemaPlus rootSchema = connection.getRootSchema();

        // Load the PostgreSQL JDBC driver
        Class.forName((String) connectionProperties.get("driverClassName"));

        final DataSource ds = JdbcSchema.dataSource(
                (String) connectionProperties.get("url"),
                (String) connectionProperties.get("driverClassName"),
                (String) connectionProperties.get("username"),
                (String) connectionProperties.get("password"));

        String schemaName = (String) connectionProperties.get("schema");

        JdbcSchema jdbcSchema = JdbcSchema.create(rootSchema, schemaName, ds, null, schemaName);
        rootSchema.add(schemaName, jdbcSchema);

        // Add masking functions to schema
        maskingFunctionsCatalog.addToSchema(rootSchema);
    }
}
