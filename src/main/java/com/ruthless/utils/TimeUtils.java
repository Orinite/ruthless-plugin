package com.ruthless.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private TimeUtils() {}

    public static final String convertTimeToDate(long seconds) {
        Instant instant = Instant.ofEpochSecond(seconds);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        return formatter.format(instant);
    }
}
