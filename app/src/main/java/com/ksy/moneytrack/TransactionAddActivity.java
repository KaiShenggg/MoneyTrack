package com.ksy.moneytrack;

import android.app.DatePickerDialog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAddActivity extends AppCompatActivity {

    private String action; // add or edit
    private String selectedType = "Expenses";
    private int id;
    private String categoryTitle;

    private Spinner spinnerType;
    private EditText editAmount;
    private EditText editMemo;
    private TextView tvSelectedDate;

    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;

    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_add);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button in the app bar
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Disable the default title

        // Inflate the custom spinner view
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.action_bar_spinner, null);

        // Set the custom view to the ActionBar
        getSupportActionBar().setCustomView(customView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);


        spinnerType = customView.findViewById(R.id.spinner_nav);
        editAmount = findViewById(R.id.editAmount);
        editMemo = findViewById(R.id.editMemo);
        tvSelectedDate = findViewById(R.id.txSelectedDate);

        setUpSpinner();


        Intent intent = getIntent();
        action = intent.getStringExtra("action");

        if (action.equals("add")) {
            setUpDatePicker(null);
        } else {
            id = intent.getIntExtra("id", 0);
            selectedType = intent.getStringExtra("type");
            categoryTitle = intent.getStringExtra("categoryTitle");
            String amount = intent.getStringExtra("amount");
            String memo = intent.getStringExtra("memo");
            String date = intent.getStringExtra("date");

            List<String> transactionList = Arrays.asList(getResources().getStringArray(R.array.types));
            spinnerType.setSelection(transactionList.indexOf(selectedType));

            editAmount.setText(amount);
            editMemo.setText(memo);

            setUpDatePicker(date);
        }

        listCategories(selectedType, categoryTitle);


        // To hide the keyboard after user enters the amount
        editAmount.setOnEditorActionListener((v, actionId, event) -> {
            // Check if the "Done" or "Enter" key was pressed
            if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_transaction, menu);
        return true;
    }

    // Handle the tick click event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_check) {
            String category = categoryAdapter.getSelectedCategoryTitle();
            String strAmount = editAmount.getText().toString().trim();
            String memo = editMemo.getText().toString().trim();
            String date = tvSelectedDate.getText().toString();

            if (category == null) {
                Toast.makeText(this, "Please select a category!", Toast.LENGTH_SHORT).show();
                return false;
            } else if (strAmount.isEmpty()) {
                Toast.makeText(this, "Please fill in the amount!", Toast.LENGTH_SHORT).show();
                return false;
            } else if (strAmount.equals(".")) {
                Toast.makeText(this, "Please fill in a valid amount!", Toast.LENGTH_SHORT).show();
                return false;
            }

            double amount = Double.parseDouble(strAmount);
            if (amount <= 0) {
                Toast.makeText(this, "Please fill in a valid amount!", Toast.LENGTH_SHORT).show();
                return false;
            } else if (amount > 1_000_000) { // Max limit
                Toast.makeText(this, "Amount is too large", Toast.LENGTH_SHORT).show();
                return false;
            }

            SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
            mySQLiteAdapter.openToWrite();

            if (action.equals("add")) {
                long insertedRowId = mySQLiteAdapter.insertTransaction(selectedType, category, amount, memo, date);

                if (insertedRowId != -1) {
                    Toast.makeText(this, selectedType + " added", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
                }
            } else {
                boolean rowsUpdated = mySQLiteAdapter.updateTransaction(id, selectedType, category, amount, memo, date);

                if (rowsUpdated) {
                    Toast.makeText(this, selectedType + " updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show();
                }
            }

            mySQLiteAdapter.close();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpSpinner() {
        // Create an adapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.types, R.layout.item_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerType.setAdapter(adapter);

        // Handle spinner selection events
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newSelectedType = (String) parent.getItemAtPosition(position);
                if (!selectedType.equals(newSelectedType)) {
                    selectedType = newSelectedType;
                    listCategories(selectedType, categoryTitle);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void listCategories(String type, String categoryTitle) {
        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4); // 4 items per row
        rvCategories.setLayoutManager(gridLayoutManager);
        rvCategories.setNestedScrollingEnabled(false);

        List<Category> categories = Categories.getCategoriesByType(type);

        categoryAdapter = new CategoryAdapter(this, categories, categoryTitle);
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setUpDatePicker(String date) {
        CardView cvPickDate = findViewById(R.id.cvDate);

        // Use the current date as the default date in the date picker
        final Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

        if (date != null) {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date parsedDate = parser.parse(date);
                calendar.setTime(parsedDate);
            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            }
        }

        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        String dateString = sdf.format(calendar.getTime());
        tvSelectedDate.setText(dateString);

        // To add a listener for pick date
        cvPickDate.setOnClickListener(v -> {
            int year = selectedYear;
            int month = selectedMonth;
            int day = selectedDay;

            // Create a variable for date picker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(TransactionAddActivity.this, R.style.DialogTheme, (view, year1, monthOfYear, dayOfMonth) -> {
                calendar.set(year1, monthOfYear, dayOfMonth);
                String dateString1 = sdf.format(calendar.getTime());
                tvSelectedDate.setText(dateString1);

                selectedYear = year1;
                selectedMonth = monthOfYear;
                selectedDay = dayOfMonth;
            },
                    // Pass year, month and day for selected date in date picker
                    year, month, day);
            datePickerDialog.show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}