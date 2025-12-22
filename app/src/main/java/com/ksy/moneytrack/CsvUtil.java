package com.ksy.moneytrack;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
}
