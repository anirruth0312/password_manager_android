package com.example.passwordmanager;

import java.util.regex.Pattern;

public class Utils {

    // Precompiled regex patterns for performance
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*(),.?\":{}|<>].*");

    /**
     * Check the strength of the given password.
     *
     * @param password Password string to validate.
     * @return null if password is strong; otherwise an error message string describing the first failing rule.
     */
    public static String checkPasswordStrength(String password) {
        if (password == null) {
            return "Error: Password must not be null.";
        }

        // Check the length of the password
        if (password.length() < 10) {
            return "Error: Password must be at least 10 characters long.";
        }

        // Check for uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            return "Error: Password must contain at least one uppercase letter.";
        }

        // Check for lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            return "Error: Password must contain at least one lowercase letter.";
        }

        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            return "Error: Password must contain at least one digit.";
        }

        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            return "Error: Password must contain at least one special character.";
        }

        // If all conditions are met, the password is strong
        return null;
    }
}

