package com.ksy.moneytrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SQLiteAdapter {

    // Constant variables
    private static final String MY_DATABASE_NAME = "MONEY_TRACK";
    public static final int MY_DATABASE_VERSION = 1;

    private static final String DATABASE_TRANSACTION_TABLE = "TRANSCATION";
    private static final String TRANSACTION_ID = "id";
    private static final String TRANSACTION_TYPE = "type";
    private static final String TRANSACTION_CATEGORY = "category";
    private static final String TRANSACTION_AMOUNT = "amount";
    private static final String TRANSACTION_MEMO = "memo";
    private static final String TRANSACTION_DATE = "date";
    private static final String TRANSACTION_CREATED_AT = "created_at";

    // SQL command to create the table with the columns
    private static final String SCRIPT_CREATE_DATABASE_TRANSACTION_TABLE = "create table " + DATABASE_TRANSACTION_TABLE +
            " (" + TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TRANSACTION_TYPE + " text not null, " +
            TRANSACTION_CATEGORY + " text not null, " +
            TRANSACTION_AMOUNT + " decimal(10,2) not null, " +
            TRANSACTION_MEMO + " text, " +
            TRANSACTION_DATE + " text not null, " +
            TRANSACTION_CREATED_AT + " text default (datetime('now','localtime')));";

    // Variables
    private final Context context;
    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    // Constructor
    public SQLiteAdapter(Context c) {
        context = c;
    }

    // Open the database to insert data / to write data
    public SQLiteAdapter openToWrite() throws android.database.SQLException {
        // Create a table with MY_DATABASE_NAME and the version of MY_DATABASE_VERSION
        sqLiteHelper = new SQLiteHelper(context, MY_DATABASE_NAME, null, MY_DATABASE_VERSION);

        // Open to write
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();

        return this;
    }

    // Open the database to read data
    public SQLiteAdapter openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, MY_DATABASE_NAME, null, MY_DATABASE_VERSION);

        // Open to read
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();

        return this;
    }

    public long insertTransaction(String type, String category, double amount, String memo, String date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRANSACTION_TYPE, type);
        contentValues.put(TRANSACTION_CATEGORY, category);
        contentValues.put(TRANSACTION_AMOUNT, type.equals("Income") ? amount : amount * -1);
        contentValues.put(TRANSACTION_MEMO, memo);

        // Convert date to SQL DATE format
        String sqlDate = formatDate(date);
        contentValues.put(TRANSACTION_DATE, sqlDate);

        return sqLiteDatabase.insert(DATABASE_TRANSACTION_TABLE, null, contentValues);
    }

    public boolean updateTransaction(int id, String type, String category, double amount, String memo, String date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRANSACTION_TYPE, type);
        contentValues.put(TRANSACTION_CATEGORY, category);
        contentValues.put(TRANSACTION_AMOUNT, type.equals("Income") ? amount : amount * -1);
        contentValues.put(TRANSACTION_MEMO, memo);

        // Convert date to SQL DATE format
        String sqlDate = formatDate(date);
        contentValues.put(TRANSACTION_DATE, sqlDate);

        String whereClause =  TRANSACTION_ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(id)};

        int rowsUpdated = sqLiteDatabase.update(DATABASE_TRANSACTION_TABLE, contentValues, whereClause, whereArgs);
        return rowsUpdated > 0;
    }

    // Helper method to format date to SQL DATE format (YYYY-MM-DD)
    private String formatDate(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = inputFormat.parse(date);
            return sqlFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date; // Return original date string if parsing fails
        }
    }

    public List<Transaction> queueAllTransaction() {
        String[] columns = new String[] {TRANSACTION_ID, TRANSACTION_TYPE, TRANSACTION_CATEGORY, TRANSACTION_AMOUNT, TRANSACTION_MEMO, TRANSACTION_DATE, TRANSACTION_CREATED_AT};
        String orderBy = TRANSACTION_DATE + " ASC, " + TRANSACTION_CREATED_AT + " ASC";
        Cursor cursor = sqLiteDatabase.query(DATABASE_TRANSACTION_TABLE, columns, null, null, null, null,  orderBy);

        List<Transaction> result = new ArrayList<>();

        int index_id = cursor.getColumnIndex(TRANSACTION_ID);
        int index_type = cursor.getColumnIndex(TRANSACTION_TYPE);
        int index_category = cursor.getColumnIndex(TRANSACTION_CATEGORY);
        int index_amount = cursor.getColumnIndex(TRANSACTION_AMOUNT);
        int index_memo = cursor.getColumnIndex(TRANSACTION_MEMO);
        int index_date = cursor.getColumnIndex(TRANSACTION_DATE);
        int index_created_at = cursor.getColumnIndex(TRANSACTION_CREATED_AT);

        for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
            Transaction transaction = new Transaction(
                    cursor.getInt(index_id),
                    cursor.getString(index_type),
                    cursor.getString(index_category),
                    cursor.getDouble(index_amount),
                    cursor.getString(index_memo),
                    cursor.getString(index_date),
                    cursor.getString(index_created_at)
            );
            result.add(transaction);
        }

        cursor.close();
        return result;
    }

    public List<Transaction> queueAllTransaction(int month, int year, double[] totalAmounts) {
        String datePrefix = String.format("%d-%02d", year, month); // yyyy-mm format

        String[] columns = new String[] {TRANSACTION_ID, TRANSACTION_TYPE, TRANSACTION_CATEGORY, TRANSACTION_AMOUNT, TRANSACTION_MEMO, TRANSACTION_DATE, TRANSACTION_CREATED_AT};
        String selection = TRANSACTION_DATE + " LIKE ?";
        String[] selectionArgs = new String[] { datePrefix + "%" };
        String orderBy = TRANSACTION_DATE + " DESC, " + TRANSACTION_CREATED_AT + " DESC";
        Cursor cursor = sqLiteDatabase.query(DATABASE_TRANSACTION_TABLE, columns, selection, selectionArgs, null, null,  orderBy);

        List<Transaction> result = new ArrayList<>();
        double totalIncome = 0;
        double totalExpenses = 0;

        int index_id = cursor.getColumnIndex(TRANSACTION_ID);
        int index_type = cursor.getColumnIndex(TRANSACTION_TYPE);
        int index_category = cursor.getColumnIndex(TRANSACTION_CATEGORY);
        int index_amount = cursor.getColumnIndex(TRANSACTION_AMOUNT);
        int index_memo = cursor.getColumnIndex(TRANSACTION_MEMO);
        int index_date = cursor.getColumnIndex(TRANSACTION_DATE);
        int index_created_at = cursor.getColumnIndex(TRANSACTION_CREATED_AT);

        for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
            Transaction transaction = new Transaction(
                    cursor.getInt(index_id),
                    cursor.getString(index_type),
                    cursor.getString(index_category),
                    cursor.getDouble(index_amount),
                    cursor.getString(index_memo),
                    cursor.getString(index_date),
                    cursor.getString(index_created_at)
            );
            result.add(transaction);

            if (transaction.getType().equals("Income"))
                totalIncome += transaction.getAmount();
            else if (transaction.getType().equals("Expenses"))
                totalExpenses += transaction.getAmount();
        }

        // Store the total income and expenses in the array passed as an argument
        totalAmounts[0] = totalIncome;
        totalAmounts[1] = totalExpenses;

        cursor.close();
        return result;
    }

    public Map<String, Float> queueTransactionByType(String type, int month, int year) {
        String datePrefix = String.format("%d-%02d", year, month); // yyyy-mm format

        String[] columns = new String[] {TRANSACTION_CATEGORY, "SUM(" + TRANSACTION_AMOUNT + ") as TotalAmount"};
        String selection = TRANSACTION_TYPE + " = ? AND " + TRANSACTION_DATE + " LIKE ?";
        String[] selectionArgs = new String[] {type, datePrefix + "%"};

        // Perform aggregation query, grouping by category
        Cursor cursor = sqLiteDatabase.query(DATABASE_TRANSACTION_TABLE, columns, selection, selectionArgs, TRANSACTION_CATEGORY, null, null);

        Map<String, Float> categoryTotals = new HashMap<>();
        if (cursor != null) {
            int indexCategory = cursor.getColumnIndex(TRANSACTION_CATEGORY);
            int indexTotalAmount = cursor.getColumnIndex("TotalAmount");

            while (cursor.moveToNext()) {
                String category = cursor.getString(indexCategory);
                float amount = cursor.getFloat(indexTotalAmount);
                categoryTotals.put(category, amount);
            }
            cursor.close();
        }

        return categoryTotals;
    }

    public Map<String, Map<Integer, Double>> queueIncomeAndExpenses(int month, int year) {
        String datePrefix = String.format("%d-%02d", year, month); // yyyy-mm format

        String[] columns = new String[] {TRANSACTION_TYPE, TRANSACTION_AMOUNT, TRANSACTION_DATE};
        String selection = TRANSACTION_DATE + " LIKE ?";
        String[] selectionArgs = new String[] { datePrefix + "%" };
        String orderBy = TRANSACTION_DATE;
        Cursor cursor = sqLiteDatabase.query(DATABASE_TRANSACTION_TABLE, columns, selection, selectionArgs, null, null,  orderBy);

        Map<Integer, Double> incomePerDay = new HashMap<>();
        Map<Integer, Double> expensesPerDay = new HashMap<>();

        int index_type = cursor.getColumnIndex(TRANSACTION_TYPE);
        int index_amount = cursor.getColumnIndex(TRANSACTION_AMOUNT);
        int index_date = cursor.getColumnIndex(TRANSACTION_DATE);

        for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
            String type = cursor.getString(index_type);
            double amount = cursor.getDouble(index_amount);
            String date = cursor.getString(index_date);
            String[] split = date.split("-");
            int day = Integer.parseInt(split[2]);

            if (type.equals("Income"))
                incomePerDay.put(day, incomePerDay.getOrDefault(day, 0.0) + amount);
            else if (type.equals("Expenses"))
                expensesPerDay.put(day, expensesPerDay.getOrDefault(day, 0.0) + amount);
        }

        cursor.close();

        Map<String, Map<Integer, Double>> result = new HashMap<>();
        result.put("Income", incomePerDay);
        result.put("Expenses", expensesPerDay);

        return result;
    }

    public double getTotalByType(String yearMonth, String type) {
        double total = 0;
        String sumAmount = "SUM(amount)";

        String[] columns = new String[] {sumAmount};
        String selection = TRANSACTION_TYPE + " = ? AND " + TRANSACTION_DATE + " LIKE ?";
        String[] selectionArgs = new String[] {type, yearMonth + "%"};
        Cursor cursor = sqLiteDatabase.query(DATABASE_TRANSACTION_TABLE, columns, selection, selectionArgs, null, null, null);

        int index_sum_amount = cursor.getColumnIndex(sumAmount);

        if (cursor.moveToFirst()) { // Check if the query returned a result
            total = cursor.getDouble(index_sum_amount);
        }

        cursor.close();
        return total;
    }

    public boolean deleteTransaction(int id) {
        String whereClause =  TRANSACTION_ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(id)};

        int rowsDeleted = sqLiteDatabase.delete(DATABASE_TRANSACTION_TABLE, whereClause, whereArgs);
        return rowsDeleted > 0;
    }

    // Close the database
    public void close() {
        sqLiteHelper.close();
    }


    // Superclass of SQLiteOpenHelper ---> implement both the override methods which creates the database
    public class SQLiteHelper extends SQLiteOpenHelper {

        // Constructor with 4 parameters
        public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // To create the database
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SCRIPT_CREATE_DATABASE_TRANSACTION_TABLE);
        }

        // Version control
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            sqLiteDatabase.execSQL(SCRIPT_CREATE_DATABASE_TRANSACTION_TABLE);
        }
    }
}