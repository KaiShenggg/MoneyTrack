package com.ksy.moneytrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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

        final String[] months = getResources().getStringArray(R.array.months);
        String currentDate = String.format("%s %d", months[currentMonth-1], currentYear);

        // Set the menu title
        MenuItem menuItem = menu.findItem(R.id.menu_date);
        menuItem.setTitle(currentDate);

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

        // Find out which day of the week the 1st falls on
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Sunday is 1, Monday is 2
        firstDayOfWeek = firstDayOfWeek != 1 ? firstDayOfWeek-1 : 7; // Sunday is the last day in my calendar

        // Add empty days for days of the week before the 1st
        for (int i = 1; i < firstDayOfWeek; i++)
            daySummaries.add(new DaySummary("", 0, 0));

        SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToRead();

        Map<String, Map<Integer, Double>> incomeAndExpenses = mySQLiteAdapter.queueIncomeAndExpenses(currentMonth, currentYear);
        Map<Integer, Double> incomePerDay = incomeAndExpenses.get("Income");
        Map<Integer, Double> expensesPerDay = incomeAndExpenses.get("Expenses");

        for (int i = 1; i <= daysInMonth; i++) {
            double income = incomePerDay.getOrDefault(i, 0.0);
            double expenses = expensesPerDay.getOrDefault(i, 0.0);

            daySummaries.add(new DaySummary(String.valueOf(i), income, expenses));
        }

        CalendarAdapter calendarAdapter = new CalendarAdapter(daySummaries);
        RecyclerView rvCalendar = findViewById(R.id.rvCalendar);
        rvCalendar.setAdapter(calendarAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
