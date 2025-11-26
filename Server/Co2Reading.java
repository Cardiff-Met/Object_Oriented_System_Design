package Server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Co2Reading(LocalDateTime timestamp, String userId, String postcode, double co2Ppm) {

    private static final DateTimeFormatter CSV_TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String toCsvLine() {
        return timestamp.format(CSV_TIMESTAMP_FORMAT) + ',' +
                escape(userId) + ',' +
                escape(postcode) + ',' +
                co2Ppm;
    }

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
