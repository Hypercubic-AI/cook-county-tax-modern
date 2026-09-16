package org.cookcounty.tax.application.service;

import org.cookcounty.tax.domain.model.TaxRateDivision;
import org.cookcounty.tax.domain.model.TaxRateEqualizedValue;
import org.cookcounty.tax.domain.port.out.TaxRateInputReferenceDataRepository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Test-only bridge from the reviewed inputs and the production V9 seed to domain records. */
record TaxRateInputSeedFixture(
        List<TaxRateEqualizedValue> equalizedValues, List<TaxRateDivision> divisions)
        implements TaxRateInputReferenceDataRepository {

    private static final String V9_RESOURCE = "db/migration/V9__tax_rate_input_reference_data.sql";
    private static final Pattern INSERT =
            Pattern.compile(
                    "(?is)\\bINSERT\\s+INTO\\s+([a-z_][a-z0-9_]*)\\s*"
                            + "\\(([^)]*)\\)\\s*VALUES\\s*(.*?);");
    private static final int EQUALIZED_RECORD_LENGTH = 115;
    private static final int DIVISION_RECORD_LENGTH = 90;

    TaxRateInputSeedFixture {
        equalizedValues = List.copyOf(equalizedValues);
        divisions = List.copyOf(divisions);
    }

    static TaxRateInputSeedFixture fromV9() throws IOException {
        String sql;
        ClassLoader classLoader = TaxRateInputSeedFixture.class.getClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("TaxRateInputSeedFixture has no class loader");
        }
        try (InputStream stream = classLoader.getResourceAsStream(V9_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("V9 tax-rate seed is not on the test classpath");
            }
            sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<TaxRateEqualizedValue> equalized =
                rows(sql, "tax_rate_equalized_values").stream()
                        .map(
                                row ->
                                        new TaxRateEqualizedValue(
                                                integer(row, "source_order"),
                                                fixed(number(row, "volume_number"), 3),
                                                fixed(number(row, "parcel_number"), 15),
                                                fixed(number(row, "tax_code"), 5),
                                                BigDecimal.valueOf(number(row, "assessed_value")),
                                                BigDecimal.valueOf(number(row, "equalized_value")),
                                                text(row, "tax_type"),
                                                null))
                        .sorted(Comparator.comparingInt(TaxRateEqualizedValue::sourceOrder))
                        .toList();
        List<TaxRateDivision> divisions =
                rows(sql, "tax_rate_divisions").stream()
                        .map(
                                row ->
                                        new TaxRateDivision(
                                                integer(row, "source_order"),
                                                fixed(number(row, "volume_number"), 3),
                                                fixed(number(row, "parcel_number"), 15),
                                                fixed(number(row, "division_number"), 14),
                                                null))
                        .sorted(Comparator.comparingInt(TaxRateDivision::sourceOrder))
                        .toList();
        requireUniqueSourceOrder(
                equalized.stream().map(TaxRateEqualizedValue::sourceOrder).toList(),
                "equalized value");
        requireUniqueSourceOrder(
                divisions.stream().map(TaxRateDivision::sourceOrder).toList(), "division");
        return new TaxRateInputSeedFixture(equalized, divisions);
    }

    static TaxRateInputSeedFixture fromReviewedCobolInputs() throws IOException {
        byte[] equalizedSource =
                ReviewedFixture.bytes("data/cobol-fixtures/equalval.bin");
        byte[] divisionSource =
                ReviewedFixture.bytes("data/cobol-fixtures/divsion.bin");
        requireWholeRecords(equalizedSource, EQUALIZED_RECORD_LENGTH, "EQUALVAL");
        requireWholeRecords(divisionSource, DIVISION_RECORD_LENGTH, "DIVSION");

        List<TaxRateEqualizedValue> equalized = new ArrayList<>();
        for (int offset = 0, sourceOrder = 1;
                offset < equalizedSource.length;
                offset += EQUALIZED_RECORD_LENGTH, sourceOrder++) {
            equalized.add(
                    new TaxRateEqualizedValue(
                            sourceOrder,
                            fixed(packed(equalizedSource, offset, 2), 3),
                            fixed(packed(equalizedSource, offset + 2, 8), 15),
                            fixed(packed(equalizedSource, offset + 10, 3), 5),
                            BigDecimal.valueOf(packed(equalizedSource, offset + 13, 6)),
                            BigDecimal.valueOf(packed(equalizedSource, offset + 22, 6)),
                            displayDigit(equalizedSource[offset + 56]),
                            null));
        }

        List<TaxRateDivision> divisions = new ArrayList<>();
        for (int offset = 0, sourceOrder = 1;
                offset < divisionSource.length;
                offset += DIVISION_RECORD_LENGTH, sourceOrder++) {
            divisions.add(
                    new TaxRateDivision(
                            sourceOrder,
                            fixed(packed(divisionSource, offset + 5, 2), 3),
                            fixed(packed(divisionSource, offset + 7, 8), 15),
                            fixed(packed(divisionSource, offset + 1, 4), 14),
                            null));
        }
        return new TaxRateInputSeedFixture(equalized, divisions);
    }

    @Override
    public List<TaxRateEqualizedValue> findEqualizedValuesInSourceOrder() {
        return equalizedValues;
    }

    @Override
    public List<TaxRateDivision> findDivisionsInSourceOrder() {
        return divisions;
    }

    /// Formats an unsigned packed-decimal business key at its exact source width.
    private static String fixed(long value, int digits) {
        return String.format("%0" + digits + "d", value);
    }

    private static List<Map<String, String>> rows(String sql, String table) {
        List<Map<String, String>> result = new ArrayList<>();
        Matcher statements = INSERT.matcher(sql);
        while (statements.find()) {
            String matchedTable = Objects.requireNonNull(statements.group(1), "matched table");
            if (!matchedTable.equalsIgnoreCase(table)) {
                continue;
            }
            String matchedColumns = Objects.requireNonNull(statements.group(2), "matched columns");
            String matchedValues = Objects.requireNonNull(statements.group(3), "matched values");
            List<String> columns = commaSeparated(matchedColumns);
            for (String tuple : tuples(matchedValues)) {
                List<String> values = commaSeparated(tuple);
                if (columns.size() != values.size()) {
                    throw new IllegalStateException(
                            table + " seed column/value count does not match");
                }
                Map<String, String> row = new HashMap<>();
                for (int index = 0; index < columns.size(); index++) {
                    row.put(
                            columns.get(index).trim().toLowerCase(Locale.ROOT),
                            values.get(index).trim());
                }
                result.add(Map.copyOf(row));
            }
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("V9 has no seed rows for " + table);
        }
        return List.copyOf(result);
    }

    private static List<String> tuples(String valuesClause) {
        List<String> result = new ArrayList<>();
        int start = -1;
        boolean quoted = false;
        for (int index = 0; index < valuesClause.length(); index++) {
            char current = valuesClause.charAt(index);
            if (current == '\''
                    && quoted
                    && index + 1 < valuesClause.length()
                    && valuesClause.charAt(index + 1) == '\'') {
                index++;
                continue;
            }
            if (current == '\'') {
                quoted = !quoted;
            } else if (!quoted && current == '(') {
                if (start >= 0) {
                    throw new IllegalStateException("Nested V9 seed tuple");
                }
                start = index + 1;
            } else if (!quoted && current == ')') {
                if (start < 0) {
                    throw new IllegalStateException("Unopened V9 seed tuple");
                }
                result.add(valuesClause.substring(start, index));
                start = -1;
            }
        }
        if (quoted || start >= 0 || result.isEmpty()) {
            throw new IllegalStateException("Malformed V9 seed tuple list");
        }
        return List.copyOf(result);
    }

    private static List<String> commaSeparated(String source) {
        List<String> result = new ArrayList<>();
        int start = 0;
        boolean quoted = false;
        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            if (current == '\''
                    && quoted
                    && index + 1 < source.length()
                    && source.charAt(index + 1) == '\'') {
                index++;
            } else if (current == '\'') {
                quoted = !quoted;
            } else if (current == ',' && !quoted) {
                result.add(source.substring(start, index).trim());
                start = index + 1;
            }
        }
        if (quoted) {
            throw new IllegalStateException("Unclosed V9 string literal");
        }
        result.add(source.substring(start).trim());
        return List.copyOf(result);
    }

    private static int integer(Map<String, String> row, String column) {
        return Math.toIntExact(number(row, column));
    }

    private static long number(Map<String, String> row, String column) {
        return Long.parseLong(required(row, column));
    }

    private static String text(Map<String, String> row, String column) {
        String literal = required(row, column);
        if (literal.length() < 2
                || literal.charAt(0) != '\''
                || literal.charAt(literal.length() - 1) != '\'') {
            throw new IllegalStateException(column + " is not a V9 string literal");
        }
        return literal.substring(1, literal.length() - 1).replace("''", "'");
    }

    private static String required(Map<String, String> row, String column) {
        String value = row.get(column);
        if (value == null) {
            throw new IllegalStateException("V9 seed is missing column " + column);
        }
        return value;
    }

    private static void requireUniqueSourceOrder(List<Integer> orders, String source) {
        Set<Integer> unique = new HashSet<>(orders);
        if (unique.size() != orders.size()) {
            throw new IllegalStateException("V9 has duplicate " + source + " source_order");
        }
    }

    private static void requireWholeRecords(byte[] source, int recordLength, String name) {
        if (source.length == 0 || source.length % recordLength != 0) {
            throw new IllegalStateException(name + " is not a non-empty fixed-record fixture");
        }
    }

    private static long packed(byte[] source, int offset, int length) {
        long value = 0;
        for (int index = 0; index < length; index++) {
            int raw = Byte.toUnsignedInt(source[offset + index]);
            int high = raw >>> 4;
            if (high > 9) {
                throw new IllegalStateException("Invalid packed-decimal digit");
            }
            value = Math.addExact(Math.multiplyExact(value, 10), high);
            int low = raw & 0x0f;
            if (index < length - 1) {
                if (low > 9) {
                    throw new IllegalStateException("Invalid packed-decimal digit");
                }
                value = Math.addExact(Math.multiplyExact(value, 10), low);
            } else if (low == 0x0d || low == 0x0b) {
                value = -value;
            } else if (low != 0x0c && low != 0x0f) {
                throw new IllegalStateException("Invalid packed-decimal sign");
            }
        }
        return value;
    }

    private static String displayDigit(byte value) {
        int unsigned = Byte.toUnsignedInt(value);
        if (unsigned < '0' || unsigned > '9') {
            throw new IllegalStateException("Invalid display-numeric tax type");
        }
        return Character.toString((char) unsigned);
    }

}
