package de.tub.dima.mascara.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Utils {

    public static String readFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));
        String queryString = String.join(System.lineSeparator(), lines).replace("\r", "");
        return queryString;
    }
}
