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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ModalBottomSheetFragment extends BottomSheetDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
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

}
