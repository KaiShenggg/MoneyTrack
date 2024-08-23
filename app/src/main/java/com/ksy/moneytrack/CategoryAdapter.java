package com.ksy.moneytrack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private Context context;
    private int selectedPosition = -1;

    public CategoryAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Category category = categories.get(position);
        holder.ivCategoryIcon.setImageResource(category.getIconResId());
        holder.tvCategoryTitle.setText(category.getTitle());

        // Change background if selected
        if (selectedPosition == position) {
            holder.ivCircle.setImageResource(category.getBgResId());
        } else {
            holder.ivCircle.setImageResource(R.drawable.circle_light_gray_bg);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged(); // Refresh the RecyclerView to apply the selection

            // Automatically focus on editAmount
            EditText editAmount = ((Activity) v.getContext()).findViewById(R.id.editAmount);
            if (editAmount != null) {
                editAmount.requestFocus();
                // Show the keyboard
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editAmount, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public String getSelectedCategoryTitle() {
        if (selectedPosition >= 0) {
            return categories.get(selectedPosition).getTitle();
        }
        return null;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCircle, ivCategoryIcon;
        TextView tvCategoryTitle;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCircle = itemView.findViewById(R.id.ivCircle);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategoryTitle);
        }
    }
}
