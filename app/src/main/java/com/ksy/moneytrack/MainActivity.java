package com.ksy.moneytrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final Calendar calendar = Calendar.getInstance();
    private int currentMonth, currentYear;

    public static final int REQUEST_CODE = 1;

    public static DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listTransactions();


        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentYear = calendar.get(Calendar.YEAR);


        LinearLayout incomeSection = findViewById(R.id.incomeSection);
        LinearLayout expensesSection = findViewById(R.id.expensesSection);
        LinearLayout balanceSection = findViewById(R.id.balanceSection);

        setOnClickListener(incomeSection, "Income");
        setOnClickListener(expensesSection, "Expenses");
        setOnClickListener(balanceSection, "Balance");


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TransactionAddActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        });
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
                    listTransactions(selectedMonth, selectedYear);
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void listTransactions() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        listTransactions(currentMonth, currentYear);
    }

    private void listTransactions(int month, int year) {
        SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToRead();

        RecyclerView rvTransaction = findViewById(R.id.rvTransaction);
        rvTransaction.setLayoutManager(new LinearLayoutManager(this));
        rvTransaction.setNestedScrollingEnabled(false);

        // Array to hold total income and expenses
        double[] totalAmounts = new double[2];
        
        // Retrieve data from database
        List<Transaction> transactionList = mySQLiteAdapter.queueAllTransaction(month, year, totalAmounts);

        TextView tvNoTransaction = findViewById(R.id.tvNoTransaction);

        if (transactionList.isEmpty()) {
            rvTransaction.setAdapter(null);
            tvNoTransaction.setVisibility(View.VISIBLE);
        } else {
            List<DaySummary> daySummaries = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, month-1);
            calendar.set(Calendar.YEAR, year);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            Map<String, Map<Integer, Double>> incomeAndExpenses = mySQLiteAdapter.queueIncomeAndExpenses(month, year);
            Map<Integer, Double> incomePerDay = incomeAndExpenses.get("Income");
            Map<Integer, Double> expensesPerDay = incomeAndExpenses.get("Expenses");

            for (int i = daysInMonth; i >= 1; i--) {
                double income = incomePerDay.getOrDefault(i, 0.0);
                double expenses = expensesPerDay.getOrDefault(i, 0.0);

                if (income != 0 || expenses != 0)
                    daySummaries.add(new DaySummary(i, income, expenses));
            }

            tvNoTransaction.setVisibility(View.GONE);

            DataRecyclerViewAdapter adapter = new DataRecyclerViewAdapter(MainActivity.this, transactionList, daySummaries); // Pass the data to the adapter
            rvTransaction.setAdapter(adapter);

            adapter.notifyDataSetChanged(); // Notify the adapter of the data changes
        }

        // Update total income, expenses and balance
        TextView tvTotalIncome = findViewById(R.id.tvTotalIncomeRM);
        TextView tvTotalExpenses = findViewById(R.id.tvTotalExpensesRM);
        TextView tvBalance = findViewById(R.id.tvBalanceRM);
        tvTotalIncome.setText(String.format("%.2f", totalAmounts[0]));
        tvTotalExpenses.setText(String.format("%.2f", totalAmounts[1]));
        tvBalance.setText(String.format("%.2f", totalAmounts[0] + totalAmounts[1]));

        mySQLiteAdapter.close();
    }

    private void setOnClickListener(LinearLayout linerLayout, String type) {
        linerLayout.setOnClickListener(v -> {
            Intent intent;
            if (!type.equals("Balance")) {
                intent = new Intent(MainActivity.this, TransactionBreakdownActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("month", currentMonth);
                intent.putExtra("year", currentYear);
            } else {
                intent = new Intent(MainActivity.this, CalendarActivity.class);
                intent.putExtra("month", currentMonth);
                intent.putExtra("year", currentYear);
            }
            startActivity(intent);
        });
    }

    // To update transaction view when returning from other activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
            recreate();
    }
}