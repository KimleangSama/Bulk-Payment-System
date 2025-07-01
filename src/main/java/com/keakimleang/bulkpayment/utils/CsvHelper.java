package com.keakimleang.bulkpayment.utils;

import com.keakimleang.bulkpayment.payloads.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.jooq.tools.csv.*;

public final class CsvHelper {

    private CsvHelper() {
    }

    public static List<String> readRow(final Path csvFile,
                                       final int rowIndex) {
        return readRow(csvFile, ',', rowIndex);
    }

    public static List<String> readRow(final Path csvFile,
                                       final char separator,
                                       final int rowIndex) {
        try (final var reader = Files.newBufferedReader(csvFile)) {
            return readRow(reader, separator, rowIndex);
        } catch (final IOException e) {
            throw new BulkPaymentServiceException("Failed to read csv file.");
        }
    }

    public static List<String> readRow(final BufferedReader csvFile,
                                       final int rowIndex) {
        return readRow(csvFile, ',', rowIndex);
    }

    public static List<String> readRow(final BufferedReader csvFile,
                                       final char separator,
                                       final int rowIndex) {
        final var csvParser = new CSVParser(separator);
        var idx = -1;
        String line;
        String readRow = "";
        try {
            while ((line = csvFile.readLine()) != null) {
                idx += 1;
                if (rowIndex == idx) {
                    readRow = line;
                    break;
                }
            }
            return Arrays.asList(csvParser.parseLine(readRow));
        } catch (final IOException e) {
            throw new BulkPaymentServiceException("Failed to read csv file.");
        }
    }
}
