package com.example.passwordmanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.passwordmanager.DatabaseHelper.DatabaseResult;
import com.example.passwordmanager.DatabaseHelper.PasswordEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ModifyEntryFragment extends Fragment {

    private AutoCompleteTextView urlSpinner;
    private TextInputEditText passwordEditText;
    private DatabaseHelper databaseHelper;
    private String masterPasswordHash;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modify_entry, container, false);

        urlSpinner = view.findViewById(R.id.spinner_modify_url);
        passwordEditText = view.findViewById(R.id.edit_pass_modify);
        MaterialButton modifyButton = view.findViewById(R.id.btn_modify_confirm);

        databaseHelper = DatabaseHelper.getInstance(getContext());

        // Get master password hash from arguments
        if (getArguments() != null) {
            masterPasswordHash = getArguments().getString("MASTER_PASSWORD_HASH");
        }

        // Load URLs into dropdown
        loadUrlsIntoDropdown();

        modifyButton.setOnClickListener(v -> modifyEntry());

        return view;
    }

    private void loadUrlsIntoDropdown() {
        List<PasswordEntry> records = databaseHelper.viewAllRecordsFromPasswordTable();
        List<String> urls = new ArrayList<>();

        if (records != null && !records.isEmpty()) {
            for (PasswordEntry entry : records) {
                urls.add(entry.getUrl());
            }
        }

        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    urls
            );
            urlSpinner.setAdapter(adapter);
        }
    }

    private void modifyEntry() {
        String url = urlSpinner.getText().toString().trim();
        String newPassword = passwordEditText.getText() != null
                ? passwordEditText.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getContext(), "Please select a URL.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getContext(), "Please enter a new password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Encrypt the new password using AES encryption
        String encryptedPassword;
        try {
            encryptedPassword = AESEncryption.aes256Encrypt(newPassword, masterPasswordHash);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error encrypting password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        // Update the entry in the database
        DatabaseResult result = databaseHelper.modifyEntryIntoPasswordTable(url, encryptedPassword);

        if (result.isSuccess()) {
            Toast.makeText(getContext(), "Entry Updated Successfully!", Toast.LENGTH_SHORT).show();
            passwordEditText.setText("");
            urlSpinner.setText("");
        } else {
            Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload URLs when fragment becomes visible again
        loadUrlsIntoDropdown();
    }
}
