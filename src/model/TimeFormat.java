package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

// all times are shown as dd/MM/yyyy HH:mm
// times always come from the simulated clock, never LocalDateTime.now()
public class TimeFormat {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // e.g. "16/11/2026 10:00", or "" if there's no time yet
    public static String format(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(FORMAT);
    }

    // same, but "-" when there's no time yet (for the tables in the windows)
    public static String formatOrDash(LocalDateTime time) {
        return time == null ? "-" : format(time);
    }

    // reads "16/11/2026 10:00", empty text gives null
    public static LocalDateTime parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("bad date \"" + text + "\"");
        }
    }

    public static int minutesBetween(LocalDateTime from, LocalDateTime to) {
        return (int) Duration.between(from, to).toMinutes();
    }
}
