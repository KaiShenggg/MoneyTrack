package com.ksy.moneytrack;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TransactionDetailActivity extends AppCompatActivity {

    private int id;

    public static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        getSupportActionBar().setTitle(R.string.tx_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button in the app bar


        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        String type = intent.getStringExtra("type");
        int categoryBgResId = intent.getIntExtra("categoryBgResId", 0);
        int categoryIconResId = intent.getIntExtra("categoryIconResId", 0);
        String categoryTitle = intent.getStringExtra("categoryTitle");
        String amount = intent.getStringExtra("amount");
        String memo = intent.getStringExtra("memo");
        String date = intent.getStringExtra("date");

        ImageView ivCategoryBg = findViewById(R.id.ivCategoryBg);
        ImageView ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        TextView tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        TextView tvType = findViewById(R.id.tvType);
        TextView tvAmount = findViewById(R.id.tvAmount);
        TextView tvMemo = findViewById(R.id.tvMemo);
        TextView tvDate = findViewById(R.id.tvDate);

        if (categoryBgResId != 0) {
            ivCategoryBg.setImageResource(categoryBgResId);
        }
        if (categoryIconResId != 0) {
            ivCategoryIcon.setImageResource(categoryIconResId);
        }
        tvCategoryTitle.setText(categoryTitle);
        tvType.setText(type);
        tvAmount.setText(amount);
        tvMemo.setText(memo.isEmpty() ? "-" : memo);
        tvDate.setText(date);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intentToEdit = new Intent(TransactionDetailActivity.this, TransactionAddActivity.class);
            intentToEdit.putExtra("action", "edit");
            intentToEdit.putExtra("id", id);
            intentToEdit.putExtra("type", type);
            intentToEdit.putExtra("categoryTitle", categoryTitle);
            intentToEdit.putExtra("amount", amount);
            intentToEdit.putExtra("memo", memo);
            intentToEdit.putExtra("date", date);
            startActivityForResult(intentToEdit, REQUEST_CODE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_transaction, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_check);
        menuItem.setIcon(R.drawable.ic_delete);
        return true;
    }

    // Handle the delete click event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_check) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to delete this transaction?");

            builder.setPositiveButton("Yes", (dialog, which) -> {
                SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(this);
                mySQLiteAdapter.openToWrite();

                boolean isDeleted = mySQLiteAdapter.deleteTransaction(id);

                if (isDeleted) {
                    Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                }

                mySQLiteAdapter.close();
            });

            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // To update transaction view when returning from other activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Return to MainActivity after successful update
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
