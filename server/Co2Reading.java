package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable data class representing a CO2 reading taken at a point in time.
 *
 * @param timestamp the timestamp when the reading was taken
 * @param userId    identifier of the user who submitted the reading
 * @param postcode  postcode associated with the reading
 * @param co2Ppm    CO2 concentration in parts per million
 */
public record Co2Reading(LocalDateTime timestamp, String userId, String postcode, double co2Ppm) {

    private static final DateTimeFormatter CSV_TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convert this reading to a single CSV line matching the repository header.
     *
     * @return CSV-formatted representation of this reading
     */
    public String toCsvLine() {
        return timestamp.format(CSV_TIMESTAMP_FORMAT) + ',' +
                escape(userId) + ',' +
                escape(postcode) + ',' +
                co2Ppm;
    }

    /**
     * Escape a string for safe inclusion in CSV. Quoting and double-quoting
     * are applied when the value contains special characters. Returns an
     * empty string for null values.
     *
     * @param value the input string (may be null)
     * @return escaped CSV-safe string
     */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        if (needsQuoting) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
}
