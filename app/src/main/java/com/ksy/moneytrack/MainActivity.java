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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final Calendar calendar = Calendar.getInstance();
    private int currentMonth, currentYear;

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


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TransactionAddActivity.class);
            startActivity(intent);
        });
    }

    // To call the designed option menu from the menu folder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the current month and year
        Calendar calendar = Calendar.getInstance();
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
            tvNoTransaction.setVisibility(View.GONE);

            DataRecyclerViewAdapter adapter = new DataRecyclerViewAdapter(MainActivity.this, transactionList); // Pass the data to the adapter
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
            Intent intent = new Intent(MainActivity.this, TransactionBreakdownActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("month", currentMonth);
            intent.putExtra("year", currentYear);
            startActivity(intent);
        });
    }
}