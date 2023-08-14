package de.tub.dima.mascara;

import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.optimizer.statistics.TableStatistics;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class DbConnector {
    public final CalciteConnection calciteConnection;
    public final Connection jdbcConnection;
    public DbConnector(
            Properties connectionProperties, MaskingFunctionsCatalog maskingFunctionsCatalog
    ) throws SQLException, ClassNotFoundException {
        Properties configProperties = new Properties();
        configProperties.put(CalciteConnectionProperty.CASE_SENSITIVE.camelName(), Boolean.FALSE.toString());
        configProperties.put(CalciteConnectionProperty.UNQUOTED_CASING.camelName(), Casing.UNCHANGED.toString());
        configProperties.put(CalciteConnectionProperty.QUOTED_CASING.camelName(), Casing.UNCHANGED.toString());

        this.calciteConnection = DriverManager.getConnection("jdbc:calcite:", configProperties)
                .unwrap(CalciteConnection.class);

        SchemaPlus rootSchema = calciteConnection.getRootSchema();

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

        this.jdbcConnection = DriverManager.getConnection(connectionProperties.getProperty("url"), connectionProperties);
    }

    public TableStatistics getStatistics(List<String> tableName) throws SQLException {
        TableStatistics tableStatistics = new TableStatistics(tableName);
        String query = "SELECT attname, n_distinct, most_common_vals::text::text[] as most_common_vals, most_common_freqs, histogram_bounds::text::text[] as histogram_bounds FROM pg_stats WHERE tablename='"+tableName.get(1)+"'";
        try (Statement stmt = jdbcConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String attname = rs.getString("attname");
                float n_distinct = rs.getFloat("n_distinct");
                String[] most_common_vals = (String[]) rs.getArray("most_common_vals").getArray();
                float[] most_common_freqs = (float[]) rs.getArray("most_common_freqs").getArray();
                String[] histogram_bounds = (String[]) rs.getArray("histogram_bounds").getArray();
                tableStatistics.addAttributeStatistics(attname, n_distinct, most_common_vals, most_common_freqs, histogram_bounds);
            }
        } catch (SQLException e) {
            throw e;
        }
        return tableStatistics;
    }
}
