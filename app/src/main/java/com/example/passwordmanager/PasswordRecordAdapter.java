package com.example.passwordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PasswordRecordAdapter extends RecyclerView.Adapter<PasswordRecordAdapter.ViewHolder> {

    private List<PasswordRecord> records;
    private List<PasswordRecord> recordsFiltered;
    private Context context;

    public PasswordRecordAdapter(Context context, List<PasswordRecord> records) {
        this.context = context;
        this.records = records;
        this.recordsFiltered = new ArrayList<>(records);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_password_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PasswordRecord record = recordsFiltered.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return recordsFiltered.size();
    }

    public void filter(String query) {
        recordsFiltered.clear();
        if (query == null || query.trim().isEmpty()) {
            recordsFiltered.addAll(records);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (PasswordRecord record : records) {
                if (record.getUrl().toLowerCase().contains(lowerCaseQuery) ||
                        record.getUsername().toLowerCase().contains(lowerCaseQuery)) {
                    recordsFiltered.add(record);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateRecords(List<PasswordRecord> newRecords) {
        this.records = newRecords;
        this.recordsFiltered = new ArrayList<>(newRecords);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView urlText;
        TextView usernameText;
        TextView passwordText;
        MaterialButton togglePasswordButton;
        MaterialButton copyPasswordButton;
        boolean isPasswordVisible = false;
        String actualPassword;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            urlText = itemView.findViewById(R.id.text_url);
            usernameText = itemView.findViewById(R.id.text_username);
            passwordText = itemView.findViewById(R.id.text_password);
            togglePasswordButton = itemView.findViewById(R.id.btn_toggle_password);
            copyPasswordButton = itemView.findViewById(R.id.btn_copy_password);

            togglePasswordButton.setOnClickListener(v -> togglePasswordVisibility());
            copyPasswordButton.setOnClickListener(v -> copyPasswordToClipboard());
        }

        void bind(PasswordRecord record) {
            urlText.setText(record.getUrl());
            usernameText.setText(record.getUsername());
            actualPassword = record.getPassword();
            isPasswordVisible = false;
            passwordText.setText("••••••••");
        }

        private void togglePasswordVisibility() {
            if (isPasswordVisible) {
                passwordText.setText("••••••••");
                isPasswordVisible = false;
            } else {
                passwordText.setText(actualPassword);
                isPasswordVisible = true;
            }
        }

        private void copyPasswordToClipboard() {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", actualPassword);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    public static class PasswordRecord {
        private String url;
        private String username;
        private String password;

        public PasswordRecord(String url, String username, String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}

