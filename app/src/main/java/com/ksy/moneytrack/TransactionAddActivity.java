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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransactionAddActivity extends AppCompatActivity {

    private String selectedType = "Expenses";
    private EditText editAmount;

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

        Spinner spinner = customView.findViewById(R.id.spinner_nav);

        // Create an adapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.new_transaction, R.layout.item_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        // Handle spinner selection events
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = parent.getItemAtPosition(position).toString();
                listCategories(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        listCategories(selectedType);


        // To hide the keyboard after user enters the amount
        editAmount = findViewById(R.id.editAmount);

        editAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Check if the "Done" or "Enter" key was pressed
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // Hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });


        CardView cvPickDate = findViewById(R.id.cvDate);
        tvSelectedDate = findViewById(R.id.txSelectedDate);

        // Use the current date as the default date in the date picker
        final Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
        String dateString = sdf.format(calendar.getTime());
        tvSelectedDate.setText(dateString);

        // To add a listener for pick date
        cvPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = selectedYear;
                int month = selectedMonth;
                int day = selectedDay;

                // Create a variable for date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(TransactionAddActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        String dateString = sdf.format(calendar.getTime());
                        tvSelectedDate.setText(dateString);

                        selectedYear = year;
                        selectedMonth = monthOfYear;
                        selectedDay = dayOfMonth;
                    }
                },
                        // Pass year, month and day for selected date in date picker
                        year, month, day);
                datePickerDialog.show();
            }
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
            EditText editMemo = findViewById(R.id.editMemo);
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
            if (amount == 0) {
                Toast.makeText(this, "Please fill in a valid amount!", Toast.LENGTH_SHORT).show();
                return false;
            }

            SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
            mySQLiteAdapter.openToWrite();

            long insertedRowId = mySQLiteAdapter.insertTransaction(selectedType, category, amount, memo, date);

            if (insertedRowId != -1) {
                Toast.makeText(this, selectedType + " added", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TransactionAddActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
            }

            mySQLiteAdapter.close();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void listCategories(String type) {
        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4); // 4 items per row
        rvCategories.setLayoutManager(gridLayoutManager);
        rvCategories.setNestedScrollingEnabled(false);

        categoryAdapter = new CategoryAdapter(this, Categories.getCategoriesByType(type));
        rvCategories.setAdapter(categoryAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}