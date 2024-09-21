package com.ksy.moneytrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransactionBreakdownActivity extends AppCompatActivity implements ItemListAdapter.OnAmountChangeListener {

    private String type;
    private int currentMonth, currentYear;
    private TextView tvTotalAmountSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_breakdown);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.tx_breakdown);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button in the app bar

        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        currentMonth = intent.getIntExtra("month", 1);
        currentYear = intent.getIntExtra("year", 2000);

        tvTotalAmountSelected = findViewById(R.id.tvTotalAmountSelected);

        showBreakdown();
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
                    showBreakdown();
                }
            });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBreakdown() {
        Map<String, Float> categoryTotals = loadData();

        // Sort categories by value in descending order
        List<Map.Entry<String, Float>> sortedCategories = new ArrayList<>(categoryTotals.entrySet());
        sortedCategories.sort((e1, e2) -> Float.compare(e1.getValue(), e2.getValue()));

        setupPieChart(sortedCategories);
        setupCategoryList(sortedCategories);
    }

    private Map<String, Float> loadData() {
        SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToRead();

        Map<String, Float> categoryTotals = mySQLiteAdapter.queueTransactionByType(type, currentMonth, currentYear);

        mySQLiteAdapter.close();
        return categoryTotals;
    }

    private void setupPieChart(List<Map.Entry<String, Float>> sortedCategories) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int[] colorArray = { getColor(R.color.elm), getColor(R.color.apple), getColor(R.color.coral_red), getColor(R.color.cornflower), getColor(R.color.carrot_orange) };

        PieChart pieChart = findViewById(R.id.pieChart);
        pieChart.setHoleRadius(70);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

        LinearLayout legendContainer = findViewById(R.id.legendContainer);
        legendContainer.removeAllViews(); // Clear any existing views

        if (!sortedCategories.isEmpty()) {
            // Compute total sum
            float totalSum = 0;
            for (Map.Entry<String, Float> entry : sortedCategories) {
                totalSum += Math.abs(entry.getValue());
            }

            float otherSum = 0;
            int index = 0;
            int size = sortedCategories.size();

            // Add top 4 categories to the chart
            for (int i = 0; i < size; i++) {
                Map.Entry<String, Float> entry = sortedCategories.get(i);
                float percentage = (Math.abs(entry.getValue()) / totalSum) * 100;

                if (i < 4 || size == 5) {
                    entries.add(new PieEntry(percentage, entry.getKey()));
                    colors.add(colorArray[index]);
                } else {
                    otherSum += percentage;
                }

                index++;
            }

            // Add Others category if more than 5 categories exist
            if (size > 5) {
                entries.add(new PieEntry(otherSum, "Others"));
                colors.add(colorArray[colorArray.length - 1]);
            }

            PieDataSet dataSet = new PieDataSet(entries, "Categories");
            dataSet.setColors(colors);

            PieData data = new PieData(dataSet);
            data.setDrawValues(false); // Hide values inside the pie chart

            pieChart.setData(data);
            pieChart.setCenterText(type + "\n" + MainActivity.decimalFormat.format(Math.abs(totalSum)));

            pieChart.invalidate(); // Refresh the chart

            // Create category legend
            for (int i = 0; i < entries.size(); i++) {
                PieEntry entry = entries.get(i);

                LinearLayout legendItem = new LinearLayout(this);
                legendItem.setOrientation(LinearLayout.HORIZONTAL);

                // Color of legend
                View colorView = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
                params.setMargins(0, 15, 15, 0);
                colorView.setLayoutParams(params);
                colorView.setBackgroundColor(colors.get(i));

                // Legend
                TextView legend = new TextView(this);
                legend.setText(entry.getLabel() + ": " + String.format("%.2f%%", entry.getValue()));

                legendItem.addView(colorView);
                legendItem.addView(legend);

                legendContainer.addView(legendItem);
            }
        } else {
            entries.add(new PieEntry(100, getString(R.string.tx_no_data_available)));

            PieDataSet dataSet = new PieDataSet(entries, getString(R.string.tx_no_data_available));
            dataSet.setColors(getColor(R.color.light_gray));

            PieData data = new PieData(dataSet);
            data.setDrawValues(false); // Hide values inside the pie chart

            pieChart.setData(data);
            pieChart.setCenterText(type + "\n" + getString(R.string.tx_zero));

            pieChart.invalidate(); // Refresh the chart
        }

        // Set up the touch listener to detect center clicks
        pieChart.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float distance = (float) Math.sqrt(Math.pow(event.getX() - pieChart.getWidth() / 2, 2) +
                        Math.pow(event.getY() - pieChart.getHeight() / 2, 2));
                float radius = pieChart.getRadius() * pieChart.getHoleRadius() / 100;

                if (distance < radius) {
                    type = type.equals("Income") ? "Expenses" : "Income";
                    showBreakdown();
                }
            }
            return true;
        });
    }

    private void setupCategoryList(List<Map.Entry<String, Float>> sortedCategories) {
        TextView tvType = findViewById(R.id.tvType);
        tvType.setText(type.equals("Income") ? R.string.tx_income_list : R.string.tx_expenses_list);

        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        TextView tvNoData = findViewById(R.id.tvNoData);

        if (!sortedCategories.isEmpty()) {
            tvNoData.setVisibility(View.GONE);

            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            ItemListAdapter adapter = new ItemListAdapter(TransactionBreakdownActivity.this, sortedCategories, false, this); // Pass the data to the adapter
            rvCategories.setAdapter(adapter);
        } else {
            rvCategories.setAdapter(null);
            tvNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAmountChanged(double totalAmount) {
        if (totalAmount != 0) {
            tvTotalAmountSelected.setVisibility(View.VISIBLE);
            tvTotalAmountSelected.setText("Total Amount Selected: " + MainActivity.decimalFormat.format(Math.abs(totalAmount)));
        } else
            tvTotalAmountSelected.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
