package com.example.passwordmanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.passwordmanager.DatabaseHelper.DatabaseResult;

public class AddEntryFragment extends Fragment {

    private EditText urlEditText;
    private EditText userEditText;
    private EditText passEditText;
    private DatabaseHelper databaseHelper;
    private String masterPasswordHash;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_entry, container, false);

        urlEditText = view.findViewById(R.id.edit_url_add);
        userEditText = view.findViewById(R.id.edit_user_add);
        passEditText = view.findViewById(R.id.edit_pass_add);
        Button addButton = view.findViewById(R.id.btn_add_confirm);

        databaseHelper = DatabaseHelper.getInstance(getContext());

        // Get master password hash from arguments
        if (getArguments() != null) {
            masterPasswordHash = getArguments().getString("MASTER_PASSWORD_HASH");
        }

        addButton.setOnClickListener(v -> addEntry());

        return view;
    }

    private void addEntry() {
        String url = urlEditText.getText().toString().trim();
        String user = userEditText.getText().toString().trim();
        String pwd = passEditText.getText().toString().trim();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (databaseHelper.isUrlPresentInPasswordTable(url)) {
                Toast.makeText(getContext(), "Error: URL already exists.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error checking URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Encrypt the password using AES encryption
        String encryptedPassword;
        try {
            encryptedPassword = AESEncryption.aes256Encrypt(pwd, masterPasswordHash);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error encrypting password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        // Store the encrypted password
        DatabaseResult result = databaseHelper.insertEntryIntoPasswordTable(url, user, encryptedPassword);

        if (result.isSuccess()) {
            Toast.makeText(getContext(), "Entry Added!", Toast.LENGTH_SHORT).show();
            urlEditText.setText("");
            userEditText.setText("");
            passEditText.setText("");
        } else {
            Toast.makeText(getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
