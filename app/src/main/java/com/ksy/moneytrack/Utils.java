package com.ksy.moneytrack;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private static void bringForwardBalance(Context context, String previousYearMonth, String currentYearMonth) {
        SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(context);
        mySQLiteAdapter.openToWrite();

        // Calculate the balance from the previous month
        double totalIncome = mySQLiteAdapter.getTotalByType(previousYearMonth, "Income");
        double totalExpenses = mySQLiteAdapter.getTotalByType(previousYearMonth, "Expenses");
        double balance = totalIncome + totalExpenses;

        // Insert balance if it is greater than 0
        if (balance > 0) {
            mySQLiteAdapter.insertTransaction("Income", "Balance", balance, "", currentYearMonth+"-01");
        }

        mySQLiteAdapter.close();
    }

    public static void checkAndBringForwardBalance(Context context) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        String currentYearMonth = String.format("%d-%02d", currentYear, currentMonth);

        // Retrieve the last processed year month from SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String lastProcessedYearMonth = preferences.getString("lastProcessedYearMonth", "");

        // First-time case: Initialize SharedPreferences and skip processing
        if (lastProcessedYearMonth.isEmpty()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("lastProcessedYearMonth", currentYearMonth);
            editor.apply();
            return;
        }

        // Proceed only if this month hasn't been processed
        if (!currentYearMonth.equals(lastProcessedYearMonth)) {
            // Calculate the previous month
            calendar.add(Calendar.MONTH, -1); // Move calendar to the previous month
            int previousYear = calendar.get(Calendar.YEAR);
            int previousMonth = calendar.get(Calendar.MONTH) + 1;
            String previousYearMonth = String.format("%d-%02d", previousYear, previousMonth);

            bringForwardBalance(context, previousYearMonth, currentYearMonth);

            // Update the last processed year month in SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("lastProcessedYearMonth", currentYearMonth);
            editor.apply();
        }
    }

    public static Map<String, List<Transaction>> groupTransactionsByDate(List<Transaction> transactions) {
        Map<String, List<Transaction>> groupedTransactions = new LinkedHashMap<>();

        for (Transaction transaction : transactions) {
            String date = transaction.getDate();
            if (!groupedTransactions.containsKey(date)) {
                groupedTransactions.put(date, new ArrayList<>());
            }
            groupedTransactions.get(date).add(transaction);
        }

        return groupedTransactions;
    }

    public static void showMonthYearPickerDialog(Context context, MenuItem item, MonthYearPickerCallback callback) {
        final String[] months = context.getResources().getStringArray(R.array.months);

        Calendar calendar = Calendar.getInstance();
        int currentMonth;
        int currentYear;

        String title = item.getTitle().toString();

        try {
            String[] parts = title.split(" "); // MMM yyyy format
            List<String> monthList = Arrays.asList(months);

            currentMonth = monthList.indexOf(parts[0]) + 1;
            currentYear = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            currentMonth = calendar.get(Calendar.MONTH) + 1;
            currentYear = calendar.get(Calendar.YEAR);
        }

        // Inflate the layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_month_year_picker, null);

        final NumberPicker npMonth = view.findViewById(R.id.npMonth);
        final NumberPicker npYear = view.findViewById(R.id.npYear);

        npMonth.setMinValue(0);
        npMonth.setMaxValue(months.length - 1);
        npMonth.setDisplayedValues(months);

        npYear.setMinValue(2000);
        npYear.setMaxValue(2100);

        npMonth.setValue(currentMonth - 1);
        npYear.setValue(currentYear);

        // Create and show the dialog
        new AlertDialog.Builder(context)
                .setTitle("Select Month and Year")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedYear = npYear.getValue();
                    int selectedMonth = npMonth.getValue();

                    // Format and set the title
                    String selectedDate = String.format("%s %d", months[selectedMonth], selectedYear);
                    item.setTitle(selectedDate);

                    // Call the callback with the selected month and year
                    if (callback != null) {
                        callback.onMonthYearSelected(selectedMonth + 1, selectedYear);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // To handle user actions
    public interface MonthYearPickerCallback {
        void onMonthYearSelected(Integer month, Integer year);
    }

}
