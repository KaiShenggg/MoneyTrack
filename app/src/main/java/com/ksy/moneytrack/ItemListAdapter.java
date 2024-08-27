package com.ksy.moneytrack;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.TransactionViewHolder> {

    private final Context context;
    private final List<?> items;
    private final boolean isTransaction;

    public ItemListAdapter(Context context, List<?> items, boolean isTransaction) {
        this.context = context;
        this.items = items;
        this.isTransaction = isTransaction;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        if (isTransaction) {
            Transaction transaction = (Transaction) items.get(position);

            Category category = Categories.getCategoryByTitle(transaction.getCategory());

            if (category != null) {
                holder.ivCategoryBg.setImageResource(category.getBgResId());
                holder.ivCategoryIcon.setImageResource(category.getIconResId());
            }

            holder.tvCategoryTitle.setText(transaction.getCategory());
            holder.tvAmount.setText(MainActivity.decimalFormat.format(transaction.getAmount()));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), TransactionDetailActivity.class);

                intent.putExtra("id", transaction.getId());
                intent.putExtra("type", transaction.getType());
                intent.putExtra("categoryBgResId", category.getBgResId());
                intent.putExtra("categoryIconResId", category.getIconResId());
                intent.putExtra("categoryTitle", transaction.getCategory());
                intent.putExtra("amount", MainActivity.decimalFormat.format(Math.abs(transaction.getAmount())));
                intent.putExtra("memo", transaction.getMemo());
                intent.putExtra("date", transaction.getDate());

                v.getContext().startActivity(intent);
            });
        } else {
            Map.Entry<String, Float> entry = (Map.Entry<String, Float>) items.get(position);

            Category category = Categories.getCategoryByTitle(entry.getKey());

            if (category != null) {
                holder.ivCategoryBg.setImageResource(category.getBgResId());
                holder.ivCategoryIcon.setImageResource(category.getIconResId());
            }

            holder.tvCategoryTitle.setText(entry.getKey());
            holder.tvAmount.setText(MainActivity.decimalFormat.format(Math.abs(entry.getValue())));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryBg, ivCategoryIcon;
        TextView tvCategoryTitle, tvAmount;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryBg = itemView.findViewById(R.id.ivCategoryBg);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
