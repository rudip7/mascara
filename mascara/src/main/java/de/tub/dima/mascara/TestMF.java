package de.tub.dima.mascara;
import de.tub.dima.mascara.dataMasking.InverseMaskingFunction;
import de.tub.dima.mascara.dataMasking.alphabets.IntegerAlphabet;
import de.tub.dima.mascara.dataMasking.alphabets.ZipAlphabet;
import de.tub.dima.mascara.optimizer.statistics.AttributeStatistics;
import de.tub.dima.mascara.optimizer.statistics.StatisticsManager;

import java.util.*;

public class TestMF {

    public static void main(String[] args) throws Exception {
        Properties connectionProperties = new Properties();
        connectionProperties.put("url", "jdbc:postgresql://localhost:5432/mascaradb");
        connectionProperties.put("driverClassName", "org.postgresql.Driver");
        connectionProperties.put("username", "postgres");
        connectionProperties.put("user", "postgres");
        connectionProperties.put("password", "1902");
        connectionProperties.put("schema", "public");

        MascaraMaster mascara = new MascaraMaster(connectionProperties);
        List<String> tableName = List.of("public", "Masked_low");
        String attname = "zip";
        AttributeStatistics stats = mascara.statsManager.getAttributeStatistics(tableName, attname);
//        InverseMaskingFunction inverseBucketizeAge = mascara.getMaskingFunctionsCatalog().getInverseMaskingFunctionByName("INVERSE_BUCKETIZE_AGE");
        InverseMaskingFunction inverseBlurZip = mascara.getMaskingFunctionsCatalog().getInverseMaskingFunctionByName("INVERSE_BLUR_ZIP");
        stats.unmaskStatistics(inverseBlurZip);

//        stats.inverseDistribution(inverseBlurZip);
//        stats.estimateHistFreq( true);

        System.out.println("HALA MADRID!!!");
    }


}
