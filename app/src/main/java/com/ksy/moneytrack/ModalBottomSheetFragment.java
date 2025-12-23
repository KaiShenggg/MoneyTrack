package com.ksy.moneytrack;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ModalBottomSheetFragment extends BottomSheetDialogFragment {

    private ActivityResultLauncher<String[]> importFileLauncher;
    private OnImportListener importListener;

    // Callback interface
    public interface OnImportListener {
        void onImportSuccess();
    }

    public void setOnImportListener(OnImportListener listener) {
        this.importListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        importFileLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        importCSVFromUri(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the modal bottom sheet layout
        return inflater.inflate(R.layout.modal_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchCompat bringForwardBalanceSwitch = view.findViewById(R.id.switchBringForwardBalance);
        bringForwardBalanceSwitch.setChecked(Utils.getIsBringForwardBalance(requireContext()));
        bringForwardBalanceSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                Utils.updateBringForwardBalance(requireContext(), isChecked)
        );


        LinearLayout llExportCSV = view.findViewById(R.id.llExportCSV);
        llExportCSV.setOnClickListener(v -> exportCSV());


        LinearLayout llImportCSV = view.findViewById(R.id.llImportCSV);
        llImportCSV.setOnClickListener(v -> importCSV());


        String appVersion = Utils.getAppVersion(requireContext());

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        tvVersion.setText(getString(R.string.tx_app_version, appVersion));
    }

    private void exportCSV() {
        try {
            SQLiteAdapter adapter = new SQLiteAdapter(requireContext());
            adapter.openToRead();

            List<Transaction> transactions = adapter.queueAllTransaction();
            adapter.close();

            File file = CsvUtil.export(requireContext(), transactions);

            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);

            if (uri != null) {
                shareFile(uri);
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Transactions Export");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, "Export transaction records");
        startActivity(chooser);

        dismiss(); // Close BottomSheet
    }

    private void importCSV() {
        // Launch the file selector to allow selection of CSV file
        importFileLauncher.launch(new String[]{"text/csv", "text/comma-separated-values"});
    }

    private void importCSVFromUri(Uri uri) {
        try {
            List<Transaction> transactions = CsvUtil.parseCSV(requireContext(), uri);

            if (transactions.isEmpty()) {
                Toast.makeText(requireContext(), "CSV file is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            saveTransactions(transactions);

            // Data is returned via callback
            if (importListener != null) {
                importListener.onImportSuccess();
            }

            dismiss();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTransactions(List<Transaction> transactions) {
        SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(requireContext());
        mySQLiteAdapter.openToWrite();

        int successCount = mySQLiteAdapter.insertTransactions(transactions);

        if (transactions.size() == successCount) {
            Toast.makeText(requireContext(), successCount + " records successfully imported", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Import failed", Toast.LENGTH_SHORT).show();
        }

        mySQLiteAdapter.close();
    }

}
