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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.passwordmanager.DatabaseHelper.PasswordEntry;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DeleteEntryFragment extends Fragment {

    private AutoCompleteTextView urlSpinner;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_entry, container, false);

        urlSpinner = view.findViewById(R.id.spinner_delete_url);
        MaterialButton deleteButton = view.findViewById(R.id.btn_delete_confirm);

        databaseHelper = DatabaseHelper.getInstance(getContext());

        // Load URLs into dropdown
        loadUrlsIntoDropdown();

        deleteButton.setOnClickListener(v -> confirmAndDeleteEntry());

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

    private void confirmAndDeleteEntry() {
        String url = urlSpinner.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(getContext(), "Please select a URL.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete the entry for:\n" + url + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEntry(url))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEntry(String url) {
        boolean success = databaseHelper.deleteEntryFromPasswordTable(url);

        if (success) {
            Toast.makeText(getContext(), "Entry Deleted Successfully!", Toast.LENGTH_SHORT).show();
            urlSpinner.setText("");
            // Reload URLs after deletion
            loadUrlsIntoDropdown();
        } else {
            Toast.makeText(getContext(), "Error: Failed to delete entry.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload URLs when fragment becomes visible again
        loadUrlsIntoDropdown();
    }}
