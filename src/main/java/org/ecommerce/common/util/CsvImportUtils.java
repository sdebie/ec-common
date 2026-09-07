package org.ecommerce.common.util;

import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class CsvImportUtils {

    private static final Pattern IMAGE_FILE = Pattern.compile("(?i).+\\.(jpg|jpeg|png|webp|gif|avif)$");

    private CsvImportUtils() {
        // Utility class
    }

    public static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    public static String normalizeSlug(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    public static String normalizeCategorySlugs(String value) {
        List<String> normalized = splitCategorySlugs(value);
        return normalized.isEmpty() ? null : String.join(",", normalized);
    }

    public static List<String> splitCategorySlugs(String categorySlugs) {
        if (isBlank(categorySlugs)) {
            return List.of();
        }
        return Arrays.stream(categorySlugs.split(","))
                .map(CsvImportUtils::normalizeSlug)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public static List<String> splitImageNames(String imagesValue) {
        if (isBlank(imagesValue)) {
            return List.of();
        }

        String[] naive = imagesValue.split(",", -1);
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < naive.length; i++) {
            if (!current.isEmpty()) {
                current.append(',');
            }
            current.append(naive[i]);

            boolean last = i == naive.length - 1;
            boolean complete = isCompleteImagePath(current.toString());
            boolean nextComplete = !last && isCompleteImagePath(naive[i + 1]);
            if (complete && (last || nextComplete)) {
                String normalized = normalizeStorageRelativePath(current.toString());
                if (normalized != null) {
                    result.add(normalized);
                }
                current.setLength(0);
            }
        }

        if (!current.isEmpty()) {
            String normalized = normalizeStorageRelativePath(current.toString());
            if (normalized != null) {
                result.add(normalized);
            }
        }

        return List.copyOf(result);
    }

    private static boolean isCompleteImagePath(String value) {
        String normalized = normalizeStorageRelativePath(value);
        return normalized != null && IMAGE_FILE.matcher(normalized).matches();
    }

    /**
     * Normalizes a CSV image value to the storage-relative form used by
     * ProductImageEntity (for example {@code 04/product.jpg}). WordPress
     * exports commonly prefix that value with a slash; storing it without the
     * prefix keeps filesystem validation and URL construction consistent.
     *
     * Path traversal is deliberately not silently removed here. Callers that
     * resolve the path against storage must reject it explicitly.
     */
    public static String normalizeStorageRelativePath(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }

        normalized = normalized.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }

        return normalized.isBlank() ? null : normalized;
    }

    public static String normalizeImagePaths(String imagesValue) {
        List<String> imagePaths = splitImageNames(imagesValue);
        return imagePaths.isEmpty() ? null : String.join(",", imagePaths);
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
