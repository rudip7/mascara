package de.tub.dima.mascara.utils;

import com.google.common.collect.ImmutableList;
import de.tub.dima.mascara.dataMasking.MaskingFunctionsCatalog;
import de.tub.dima.mascara.optimizer.statistics.TableStatistics;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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
//        rootSchema.setPath(ImmutableList.of(ImmutableList.of("public")));
//        this.calciteConnection.setSchema("public");
//        rootSchema.setPath(ImmutableList.of(ImmutableList.of("public")));

        // Add masking functions to schema
        maskingFunctionsCatalog.addToSchema(rootSchema);

        this.jdbcConnection = DriverManager.getConnection(connectionProperties.getProperty("url"), connectionProperties);
    }

    public boolean executeQuery(String query){
        try (Statement stmt = jdbcConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {}
//            stmt.execute(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public TableStatistics getStatistics(List<String> tableName, long size) throws SQLException {
        TableStatistics tableStatistics = new TableStatistics(tableName, size);
        String query = "SELECT attname, n_distinct, most_common_vals::text::text[] as most_common_vals, most_common_freqs, histogram_bounds::text::text[] as histogram_bounds FROM pg_stats WHERE tablename='"+tableName.get(tableName.size()-1)+"'";
        try (Statement stmt = jdbcConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String attname = rs.getString("attname");
                float n_distinct = rs.getFloat("n_distinct");
                Array mostCommonVals = rs.getArray("most_common_vals");
                String[] most_common_vals = mostCommonVals == null ? null : (String[]) mostCommonVals.getArray();
                Array mostCommonFreqs = rs.getArray("most_common_freqs");
                Float[] most_common_freqs = mostCommonFreqs == null ? null : (Float[]) mostCommonFreqs.getArray();
                Array histogramBounds = rs.getArray("histogram_bounds");
                String[] histogram_bounds = histogramBounds == null ? null : (String[]) histogramBounds.getArray();
                if (attname.endsWith("_stat")){
                    attname = attname.substring(0, attname.length() - "_stat".length());
                    tableStatistics.addAttributeStatistics(attname, n_distinct, most_common_vals, most_common_freqs, histogram_bounds, true);
                } else {
                    tableStatistics.addAttributeStatistics(attname, n_distinct, most_common_vals, most_common_freqs, histogram_bounds);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
        return tableStatistics;
    }

    public List<String> getAttributeNames(List<String> tableName) throws SQLException {
        String query = "SELECT attname FROM pg_attribute WHERE attrelid = '"+tableName.get(tableName.size()-1)+"'::regclass AND attnum > 0";
        try (Statement stmt = jdbcConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            List<String> attributeNames = new ArrayList<>();
            while (rs.next()) {
                attributeNames.add(rs.getString("attname"));
            }
            return attributeNames;
        } catch (SQLException e) {
            throw e;
        }
    }

    public Long getTableSize(List<String> tableName) throws SQLException {
        Long size = -1L;
        String query;
        if (tableName.size() == 2){
            query = "SELECT count(*) FROM "+tableName.get(0)+".\""+tableName.get(1)+"\"";
        } else {
            query = "SELECT count(*) FROM "+tableName.get(tableName.size()-1);
        }
        try (Statement stmt = jdbcConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                size = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw e;
        }
        return size;
    }

    public Long estimateCardinality(String query) throws SQLException {
        Long cardinality = -1L;
        String queryCardinality = "EXPLAIN (FORMAT JSON) "+query;
        try (Statement stmt = jdbcConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery(queryCardinality);
            while (rs.next()) {
                String plan = rs.getString(1);
                cardinality = Long.parseLong(plan.split("\"Plan Rows\": ")[1].split(",")[0]);
            }
        } catch (SQLException e) {
            throw e;
        }
        return cardinality;
    }

    public String getPolicyDefinition(String policyName) throws SQLException {
        String definition = null;
        String query = "SELECT definition FROM pg_matviews WHERE matviewname = '"+policyName+"'";
        try (Statement stmt = jdbcConnection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                definition = rs.getString(1);
            }
        } catch (SQLException e) {
            throw e;
        }
        return definition;
    }
}
