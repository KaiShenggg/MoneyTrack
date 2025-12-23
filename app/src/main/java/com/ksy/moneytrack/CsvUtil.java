package com.ksy.moneytrack;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {
    /**
     * Export transactions to CSV file
     */
    public static File export(Context context, List<Transaction> transactions) throws IOException {

        // CSV header
        StringBuilder csv = new StringBuilder();
        csv.append("Date,Type,Category,Amount,Memo,Created At\n");

        for (Transaction t : transactions) {
            csv.append(escape(t.getDate())).append(",");
            csv.append(escape(t.getType())).append(",");
            csv.append(escape(t.getCategory())).append(",");
            csv.append(t.getAmount()).append(",");
            csv.append(escape(t.getMemo())).append(",");
            csv.append(escape(t.getCreatedAt())).append("\n");
        }

        // Create export directory
        File cacheDir = new File(context.getCacheDir(), "exports");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // File name
        File file = new File(cacheDir, "transactions.csv");

        FileWriter writer = new FileWriter(file);
        writer.write(csv.toString());
        writer.flush();
        writer.close();

        return file;
    }

    /**
     * Escape CSV value (handle comma, quote, newline)
     */
    private static String escape(String value) {
        if (value == null) return "";

        boolean needQuote = value.contains(",")
                || value.contains("\"")
                || value.contains("\n");

        if (needQuote) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    public static List<Transaction> parseCSV(Context context, Uri uri) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip blank row
                if (line.trim().isEmpty()) {
                    continue;
                }

                Transaction transaction = parseCsvLine(line);
                transactions.add(transaction);
            }
        }

        return transactions;
    }

    private static Transaction parseCsvLine(String line) {
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        if (parts.length < 4) {
            throw new IllegalArgumentException("Insufficient number of columns");
        }

        // Remove quote
        // Mandatory columns
        String dateStr = parts[0].replace("\"", "").trim();
        String category = parts[2].replace("\"", "").trim();
        String amountStr = parts[3].replace("\"", "").trim();

        // Optional columns
        String memo = parts.length > 4 ? parts[4].replace("\"", "").trim() : null;
        String createdAtStr = parts.length > 5 ? parts[5].replace("\"", "").trim() : null;

        // Amount validation
        double amount = Double.parseDouble(amountStr);
        if (amount == 0) {
            throw new IllegalArgumentException("Amount cannot be 0");
        } else if (amount > 1_000_000) { // Max limit
            throw new IllegalArgumentException("Amount is too large");
        }

        // Type and category validation
        String type = amount > 0 ? "Income" : "Expenses";
        category = Categories.getCategoryTitle(type, category);

        // Date validation
        String normalizedDate = DateUtil.formatCsvDateToSql(dateStr);
        if (normalizedDate == null) {
            throw new IllegalArgumentException("Support only dd/MM/yyyy date format");
        }

        String normalizedCreatedAt = DateUtil.formatCsvDateTimeToSql(createdAtStr);

        return new Transaction(0, type, category, amount, memo, normalizedDate, normalizedCreatedAt);
    }
}
