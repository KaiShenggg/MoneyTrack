package com.ksy.moneytrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CalendarActivity extends AppCompatActivity {

    private int currentMonth, currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Calendar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button in the app bar

        Intent intent = getIntent();
        currentMonth = intent.getIntExtra("month", 1);
        currentYear = intent.getIntExtra("year", 2000);

        setupCalendar();
    }

    // To call the designed option menu from the menu folder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the current month and year
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonth-1);
        calendar.set(Calendar.YEAR, currentYear);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
        String dateString = sdf.format(calendar.getTime());

        // Set the menu title
        MenuItem menuItem = menu.findItem(R.id.menu_date);
        menuItem.setTitle(dateString);

        return true;
    }

    // To define the listener for menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_date) {
            Utils.showMonthYearPickerDialog(this, item, (selectedMonth, selectedYear) -> {
                if (selectedMonth != null && selectedYear != null) {
                    currentMonth = selectedMonth;
                    currentYear = selectedYear;
                    setupCalendar();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupCalendar() {
        List<DaySummary> daySummaries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonth-1);
        calendar.set(Calendar.YEAR, currentYear);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToRead();

        Map<String, Map<Integer, Double>> incomeAndExpenses = mySQLiteAdapter.queueIncomeAndExpenses(currentMonth, currentYear);
        Map<Integer, Double> incomePerDay = incomeAndExpenses.get("Income");
        Map<Integer, Double> expensesPerDay = incomeAndExpenses.get("Expenses");

        for (int i = 1; i <= daysInMonth; i++) {
            double income = incomePerDay.getOrDefault(i, 0.0);
            double expenses = expensesPerDay.getOrDefault(i, 0.0);

            daySummaries.add(new DaySummary(i, income, expenses));
        }

        CalendarAdapter calendarAdapter = new CalendarAdapter(this, daySummaries);
        RecyclerView calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
