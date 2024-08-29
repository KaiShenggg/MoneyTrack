package com.ksy.moneytrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final List<DaySummary> daySummaries;
    private final Context context;

    public CalendarAdapter(Context context, List<DaySummary> daySummaries) {
        this.context = context;
        this.daySummaries = daySummaries;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        DaySummary daySummary = daySummaries.get(position);

        // Set date text
        holder.dateTextView.setText(String.valueOf(daySummary.getDayOfMonth()));

        // Set income and expenses
        if (daySummary.getIncome() > 0)
            holder.incomeTextView.setText(String.format("%.2f", daySummary.getIncome()));
        if (daySummary.getExpenses() != 0)
            holder.expensesTextView.setText(String.format("%.2f", daySummary.getExpenses()));
    }

    @Override
    public int getItemCount() {
        return daySummaries.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {

        TextView dateTextView, incomeTextView, expensesTextView;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            incomeTextView = itemView.findViewById(R.id.incomeTextView);
            expensesTextView = itemView.findViewById(R.id.expensesTextView);
        }
    }
}
