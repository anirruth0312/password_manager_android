package com.example.passwordmanager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanager.DatabaseHelper.PasswordEntry;
import com.example.passwordmanager.PasswordRecordAdapter.PasswordRecord;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ViewRecordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PasswordRecordAdapter adapter;
    private TextView recordsCountText;
    private TextInputEditText searchInput;
    private LinearLayout emptyStateLayout;
    private DatabaseHelper databaseHelper;
    private String masterPasswordHash;
    private List<PasswordRecord> allRecords;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_records, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_records);
        recordsCountText = view.findViewById(R.id.text_records_count);
        searchInput = view.findViewById(R.id.search_input);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        databaseHelper = DatabaseHelper.getInstance(getContext());

        // Get master password hash from arguments
        if (getArguments() != null) {
            masterPasswordHash = getArguments().getString("MASTER_PASSWORD_HASH");
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allRecords = new ArrayList<>();
        adapter = new PasswordRecordAdapter(getContext(), allRecords);
        recyclerView.setAdapter(adapter);

        // Setup search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateRecordsCount(adapter.getItemCount());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadRecords();

        return view;
    }

    private void loadRecords() {
        List<PasswordEntry> records = databaseHelper.viewAllRecordsFromPasswordTable();

        allRecords.clear();

        if (records == null || records.isEmpty()) {
            showEmptyState(true);
            updateRecordsCount(0);
            return;
        }

        showEmptyState(false);

        for (PasswordEntry entry : records) {
            String decryptedPassword;
            try {
                // Decrypt the password using AES encryption
                decryptedPassword = AESEncryption.aes256Decrypt(entry.getPassword(), masterPasswordHash);
            } catch (Exception e) {
                decryptedPassword = "[Decryption failed]";
            }

            allRecords.add(new PasswordRecord(
                    entry.getUrl(),
                    entry.getUsername(),
                    decryptedPassword
            ));
        }

        adapter.updateRecords(allRecords);
        updateRecordsCount(allRecords.size());
    }

    private void showEmptyState(boolean show) {
        if (show) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void updateRecordsCount(int count) {
        String countText = count + (count == 1 ? " record found" : " records found");
        recordsCountText.setText(countText);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload records when fragment becomes visible again
        loadRecords();
        // Clear search
        if (searchInput != null) {
            searchInput.setText("");
        }
    }
}

