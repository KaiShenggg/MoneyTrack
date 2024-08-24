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
import java.util.List;
import java.util.Locale;

public class SQLiteAdapter {

    // Constant variables
    private static final String MY_DATABASE_NAME = "MONEY_TRACK";
    public static final int MY_DATABASE_VERSION = 1;

    private static final String DATABASE_TRANSACTION_TABLE = "TRANSCATION";
    private static final String TRANSACTION_ID = "id";
    private static final String TRANSACTION_KEY_CONTENT = "type";
    private static final String TRANSACTION_KEY_CONTENT_2 = "category";
    private static final String TRANSACTION_KEY_CONTENT_3 = "amount";
    private static final String TRANSACTION_KEY_CONTENT_4 = "memo";
    private static final String TRANSACTION_KEY_CONTENT_5 = "date";
    private static final String TRANSACTION_KEY_CONTENT_6 = "created_at";

    // SQL command to create the table with the columns
    private static final String SCRIPT_CREATE_DATABASE_TRANSACTION_TABLE = "create table " + DATABASE_TRANSACTION_TABLE +
            " (" + TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TRANSACTION_KEY_CONTENT + " text not null, " +
            TRANSACTION_KEY_CONTENT_2 + " text not null, " +
            TRANSACTION_KEY_CONTENT_3 + " decimal(10,2) not null, " +
            TRANSACTION_KEY_CONTENT_4 + " text, " +
            TRANSACTION_KEY_CONTENT_5 + " text not null, " +
            TRANSACTION_KEY_CONTENT_6 + " text default (datetime('now','localtime')));";

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

    public long insertTransaction(String content, String content_2, double content_3, String content_4, String content_5) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRANSACTION_KEY_CONTENT, content);
        contentValues.put(TRANSACTION_KEY_CONTENT_2, content_2);
        contentValues.put(TRANSACTION_KEY_CONTENT_3, content.equals("Income") ? content_3 : content_3 * -1);
        contentValues.put(TRANSACTION_KEY_CONTENT_4, content_4);

        // Convert content_6 (date) to SQL DATE format
        String sqlDate = formatDate(content_5);
        contentValues.put(TRANSACTION_KEY_CONTENT_5, sqlDate);

        return sqLiteDatabase.insert(DATABASE_TRANSACTION_TABLE, null, contentValues);
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

    public List<Transaction> queueAllTransaction(int month, int year, double[] totalAmounts) {
        String datePrefix = String.format("%d-%02d", year, month); // yyyy-mm format

        String[] columns = new String[] {TRANSACTION_ID, TRANSACTION_KEY_CONTENT, TRANSACTION_KEY_CONTENT_2, TRANSACTION_KEY_CONTENT_3, TRANSACTION_KEY_CONTENT_4, TRANSACTION_KEY_CONTENT_5, TRANSACTION_KEY_CONTENT_6};
        String selection = TRANSACTION_KEY_CONTENT_5 + " LIKE ?";
        String[] selectionArgs = new String[] { datePrefix + "%" };
        String orderBy = TRANSACTION_KEY_CONTENT_5 + " DESC, " + TRANSACTION_KEY_CONTENT_6 + " DESC";
        Cursor cursor = sqLiteDatabase.query(DATABASE_TRANSACTION_TABLE, columns, selection, selectionArgs, null, null,  orderBy);

        List<Transaction> result = new ArrayList<>();
        double totalIncome = 0;
        double totalExpenses = 0;

        int index_id = cursor.getColumnIndex(TRANSACTION_ID);
        int index_CONTENT = cursor.getColumnIndex(TRANSACTION_KEY_CONTENT);
        int index_CONTENT_2 = cursor.getColumnIndex(TRANSACTION_KEY_CONTENT_2);
        int index_CONTENT_3 = cursor.getColumnIndex(TRANSACTION_KEY_CONTENT_3);
        int index_CONTENT_4 = cursor.getColumnIndex(TRANSACTION_KEY_CONTENT_4);
        int index_CONTENT_5 = cursor.getColumnIndex(TRANSACTION_KEY_CONTENT_5);
        int index_CONTENT_6 = cursor.getColumnIndex(TRANSACTION_KEY_CONTENT_6);

        for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()) {
            Transaction transaction = new Transaction(
                    cursor.getInt(index_id),
                    cursor.getString(index_CONTENT),
                    cursor.getString(index_CONTENT_2),
                    cursor.getDouble(index_CONTENT_3),
                    cursor.getString(index_CONTENT_4),
                    cursor.getString(index_CONTENT_5),
                    cursor.getString(index_CONTENT_6)
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