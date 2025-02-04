package com.ksy.moneytrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataRecyclerViewAdapter extends RecyclerView.Adapter<DataRecyclerViewAdapter.DateViewHolder> {

    private final Context context;
    private final Map<String, List<Transaction>> groupedTransactions;
    private final List<DaySummary> daySummaries;
    private final List<String> dates;

    // Constructor
    public DataRecyclerViewAdapter(Context context, List<Transaction> transactionDataList, List<DaySummary> daySummaries) {
        this.context = context;
        this.groupedTransactions = Utils.groupTransactionsByDate(transactionDataList);
        this.dates = new ArrayList<>(groupedTransactions.keySet()); // Extract dates into a list for easy indexing
        this.daySummaries = daySummaries;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_grouped_transactions, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        String date = dates.get(position);
        List<Transaction> transactionsForDate = groupedTransactions.get(date);
        DaySummary daySummary = daySummaries.get(position);

        String[] parts = date.split("-"); // yyyy-mm-dd format
        holder.tvDate.setText(String.join("/", parts[2], parts[1])); // dd/mm format

        // Set income and expenses
        if (daySummary.getIncome() > 0)
            holder.tvIncome.setText("Income: " + String.format("%.2f", daySummary.getIncome()));
        if (daySummary.getExpenses() != 0)
            holder.tvExpenses.setText("Expenses: " + String.format("%.2f", Math.abs(daySummary.getExpenses())));

        // Populate the RecyclerView within the CardView with the transactions
        ItemListAdapter adapter = new ItemListAdapter(transactionsForDate, true);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context)); // Ensure the RecyclerView is properly initialized
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return groupedTransactions.size();
    }

    // ViewHolder for each date
    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvIncome, tvExpenses;
        RecyclerView recyclerView;
        CardView cardView;

        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvIncome = itemView.findViewById(R.id.tvIncome);
            tvExpenses = itemView.findViewById(R.id.tvExpenses);
            recyclerView = itemView.findViewById(R.id.recyclerViewTransactions);
            cardView = itemView.findViewById(R.id.cardViewTransactions);
        }
    }
}
