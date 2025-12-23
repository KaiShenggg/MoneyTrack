package com.ksy.moneytrack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    /**
     * Format UI date to SQL date: MMM dd, yyyy -> yyyy-MM-dd
     */
    public static String formatUiDateToSql(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = inputFormat.parse(date);
            return sqlFormat.format(parsedDate);
        } catch (ParseException e) {
            return date; // Return original date string if parsing fails
        }
    }

    /**
     * Format CSV date to SQL date: dd/MM/yyyy -> yyyy-MM-dd
     */
    public static String formatCsvDateToSql(String date) {
        return formatDateTime(date, "dd/MM/yyyy", "yyyy-MM-dd");
    }

    /**
     * Format CSV datetime to SQL datetime: dd/MM/yyyy HH:mm -> yyyy-MM-dd HH:mm:ss
     */
    public static String formatCsvDateTimeToSql(String dateTimeStr) {
        return formatDateTime(dateTimeStr, "dd/MM/yyyy HH:mm", "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Common date/datetime normalization method
     */
    private static String formatDateTime(String date, String inputPattern, String outputPattern) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        String trimmed = date.trim();

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.getDefault());

            inputFormat.setLenient(false); // Strict mode, eg: 30/02/2025 will fail
            Date parsedDate = inputFormat.parse(trimmed);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            return null;
        }
    }
}
