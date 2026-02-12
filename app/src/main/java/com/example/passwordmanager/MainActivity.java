package com.example.passwordmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private boolean isMasterPasswordSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize DatabaseHelper
        databaseHelper = DatabaseHelper.getInstance(this);

        // Check if a master password has already been set
        isMasterPasswordSet = databaseHelper.checkMasterPasswordIsSet();

        View promptTextView = findViewById(R.id.prompt_text);
        View passwordHintView = findViewById(R.id.password_hint);
        TextInputLayout passwordInputLayout = findViewById(R.id.password_input_layout);
        TextInputLayout confirmPasswordInputLayout = findViewById(R.id.confirm_password_input_layout);
        TextInputEditText passwordInput = findViewById(R.id.password_input);
        TextInputEditText confirmPasswordInput = findViewById(R.id.confirm_password_input);
        Button submitButton = findViewById(R.id.submit_button);

        if (!isMasterPasswordSet && confirmPasswordInputLayout != null) {
            // Show confirm password field and hint on first-time setup
            confirmPasswordInputLayout.setVisibility(View.VISIBLE);
            if (passwordHintView != null) {
                passwordHintView.setVisibility(View.VISIBLE);
            }
        }

        if (promptTextView instanceof android.widget.TextView) {
            ((android.widget.TextView) promptTextView).setText(
                    isMasterPasswordSet
                            ? getString(R.string.enter_master_password)
                            : getString(R.string.create_master_password)
            );
        }

        submitButton.setOnClickListener(view -> {
            String password = passwordInput.getText() != null
                    ? passwordInput.getText().toString()
                    : "";

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                if (passwordInputLayout != null) {
                    passwordInputLayout.setError("Password is required");
                }
                return;
            } else if (passwordInputLayout != null) {
                passwordInputLayout.setError(null);
            }

            if (!isMasterPasswordSet) {
                // Check password strength first
                String strengthError = Utils.checkPasswordStrength(password);
                if (strengthError != null) {
                    Toast.makeText(MainActivity.this, strengthError, Toast.LENGTH_SHORT).show();
                    if (passwordInputLayout != null) {
                        passwordInputLayout.setError(strengthError);
                    }
                    return;
                }

                // Creating master password: require confirmation
                String confirmPassword = confirmPasswordInput != null && confirmPasswordInput.getText() != null
                        ? confirmPasswordInput.getText().toString()
                        : "";

                if (TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(MainActivity.this, "Please confirm the password", Toast.LENGTH_SHORT).show();
                    if (confirmPasswordInputLayout != null) {
                        confirmPasswordInputLayout.setError("Please confirm the password");
                    }
                    return;
                } else if (confirmPasswordInputLayout != null) {
                    confirmPasswordInputLayout.setError(null);
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(MainActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    if (confirmPasswordInputLayout != null) {
                        confirmPasswordInputLayout.setError("Passwords do not match");
                    }
                    return;
                }

                // Hash the password before storing
                String hashedPassword = hashPassword(password);
                if (hashedPassword != null && databaseHelper.setMasterPassword(hashedPassword)) {
                    isMasterPasswordSet = true;
                    Toast.makeText(MainActivity.this, "Master password created", Toast.LENGTH_SHORT).show();

                    // Navigate to Dashboard on successful creation
                    Intent intent = new Intent(MainActivity.this, Dashboard.class);
                    intent.putExtra("MASTER_PASSWORD_HASH", hashedPassword);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to save master password", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Master password already created: verify the entered value
                String hashedPassword = hashPassword(password);
                if (hashedPassword != null && verifyMasterPassword(hashedPassword)) {
                    Toast.makeText(MainActivity.this, "Master password correct!", Toast.LENGTH_SHORT).show();

                    // Navigate to Dashboard on successful verification
                    Intent intent = new Intent(MainActivity.this, Dashboard.class);
                    intent.putExtra("MASTER_PASSWORD_HASH", hashedPassword);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Incorrect master password", Toast.LENGTH_SHORT).show();
                }
            }

            // Clear password inputs for security
            passwordInput.setText("");
            if (confirmPasswordInput != null) {
                confirmPasswordInput.setText("");
            }
        });
    }

    /**
     * Hash the password using SHA-256
     * @param password the plain text password
     * @return hashed password as hex string, or null if error
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verify the master password against stored hash
     * @param hashedPassword the hashed password to verify
     * @return true if password matches, false otherwise
     */
    private boolean verifyMasterPassword(String hashedPassword) {
        try {
            return databaseHelper.verifyMasterPassword(hashedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}