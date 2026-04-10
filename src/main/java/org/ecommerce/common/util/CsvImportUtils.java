package org.ecommerce.common.util;

import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;
import java.util.List;

public final class CsvImportUtils {

    private CsvImportUtils() {
        // Utility class
    }

    public static String getValue(CSVRecord record, String... headers) {
        for (String header : headers) {
            if (record.isMapped(header)) {
                String value = record.get(header);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static BigDecimal parseBigDecimal(CSVRecord record, List<String> validationErrors, String... headers) {
        String value = getValue(record, headers);
        if (isBlank(value)) {
            return new BigDecimal(0);
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            validationErrors.add("Invalid decimal value for " + headers[0] + ": " + value);
            return null;
        }
    }
}

