package com.ksy.moneytrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

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


        String appVersion = Utils.getAppVersion(requireContext());

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        tvVersion.setText(getString(R.string.tx_app_version, appVersion));
    }

}
