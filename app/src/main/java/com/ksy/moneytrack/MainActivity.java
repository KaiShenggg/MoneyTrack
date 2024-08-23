package com.ksy.moneytrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;

    private RecyclerView rvTransaction;

    private TextView tvNoTransaction;

    private DataRecyclerViewAdapter adapter;
    private SQLiteAdapter mySQLiteAdapter;

    public static DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listTransactions();


        LinearLayout incomeSection = findViewById(R.id.incomeSection);
        incomeSection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewTransaction.class);
            startActivity(intent);
        });


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewTransaction.class);
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
            showMonthYearPickerDialog(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMonthYearPickerDialog(MenuItem item) {
        final Calendar calendar = Calendar.getInstance();
        final String[] months = getResources().getStringArray(R.array.months);

        String title = item.getTitle().toString();
        int currentMonth, currentYear;

        try {
            String[] parts = title.split(" "); // MMM yyyy format

            List<String> monthList = Arrays.asList(months);
            currentMonth = monthList.indexOf(parts[0]);

            currentYear = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            currentMonth = calendar.get(Calendar.MONTH) + 1;
            currentYear = calendar.get(Calendar.YEAR);
        }

        // Inflate the layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.layout_month_year_picker, null);

        final NumberPicker npMonth = view.findViewById(R.id.npMonth);
        final NumberPicker npYear = view.findViewById(R.id.npYear);

        npMonth.setMinValue(0);
        npMonth.setMaxValue(months.length - 1);
        npMonth.setDisplayedValues(months);

        npYear.setMinValue(2000);
        npYear.setMaxValue(2100);

        npMonth.setValue(currentMonth);
        npYear.setValue(currentYear);

        // Create and show the dialog
        new AlertDialog.Builder(this)
                .setTitle("Select Month and Year")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedYear = npYear.getValue();
                    int selectedMonth = npMonth.getValue();

                    // Format and set the title
                    String selectedDate = String.format("%s %d", months[selectedMonth], selectedYear);
                    item.setTitle(selectedDate);
                    listTransactions(selectedMonth+1, selectedYear);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void listTransactions() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        listTransactions(currentMonth, currentYear);
    }

    public void listTransactions(int month, int year) {
        mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToRead();

        rvTransaction = findViewById(R.id.rvTransaction);
        rvTransaction.setLayoutManager(new LinearLayoutManager(this));
        rvTransaction.setNestedScrollingEnabled(false);

        // Array to hold total income and expenses
        double[] totalAmounts = new double[2];
        
        // Retrieve data from database
        List<Transaction> transactionList = mySQLiteAdapter.queueAllTransaction(month, year, totalAmounts);

        tvNoTransaction = findViewById(R.id.tvNoTransaction);

        if (transactionList.isEmpty()) {
            rvTransaction.setAdapter(null);
            tvNoTransaction.setVisibility(View.VISIBLE);
        } else {
            tvNoTransaction.setVisibility(View.GONE);

            adapter = new DataRecyclerViewAdapter(MainActivity.this, transactionList); // Pass the data to the adapter
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
}