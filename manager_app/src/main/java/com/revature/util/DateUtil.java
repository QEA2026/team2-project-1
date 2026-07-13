package com.revature.util;

import java.time.LocalDate;

public class DateUtil {

    private DateUtil() {}

    public static LocalDate parseFlexibleDate(String raw) {
        if (raw == null) {
            return null;
        }
        String datePart = raw.contains("T") ? raw.substring(0, raw.indexOf("T")) : raw;
        return LocalDate.parse(datePart);
    }
}