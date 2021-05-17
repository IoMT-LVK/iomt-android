package com.iomt.android.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.iomt.android.DatabaseHelper;
import com.iomt.android.LoginActivity;
import com.iomt.android.R;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        SwitchCompat mobileSwitch = root.findViewById(R.id.switch_mobile);
        if (mobileSwitch != null) {
            mobileSwitch.setOnCheckedChangeListener(this);
        }

        LinearLayout leaveAccount = root.findViewById(R.id.leave_acc);
        leaveAccount.setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(requireContext());
            db.clear();
            Intent intent = new Intent(SettingsFragment.this.requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Настройки");

        return root;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(requireActivity().getApplicationContext().getString(R.string.ACC_DATA), MODE_PRIVATE).edit();
        editor.putBoolean("lte", isChecked);
        editor.apply();
    }
}