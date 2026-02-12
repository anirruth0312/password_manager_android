package com.example.passwordmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper for managing password storage in SQLite.
 * This class replicates the functionality of the Python password manager script.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "passwords.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_PASSWORD_LIST = "password_list";
    private static final String TABLE_FLAG_EXISTENCE = "flag_existence";

    // password_list table columns
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // flag_existence table columns
    private static final String COLUMN_FLAG = "flag";

    private static DatabaseHelper instance;

    /**
     * Private constructor to prevent direct instantiation.
     * Use getInstance() instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Get singleton instance of DatabaseHelper
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createPasswordListTable(db);
        createFlagExistenceTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORD_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FLAG_EXISTENCE);
        onCreate(db);
    }

    /**
     * Create password_list table
     * Equivalent to Python's create_password_list_db()
     */
    private void createPasswordListTable(SQLiteDatabase db) {
        String CREATE_PASSWORD_LIST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PASSWORD_LIST + " ("
                + COLUMN_URL + " TEXT PRIMARY KEY, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT UNIQUE"
                + ")";
        db.execSQL(CREATE_PASSWORD_LIST_TABLE);
    }

    /**
     * Create flag_existence table for master password tracking
     */
    private void createFlagExistenceTable(SQLiteDatabase db) {
        String CREATE_FLAG_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FLAG_EXISTENCE + " ("
                + COLUMN_FLAG + " TEXT PRIMARY KEY"
                + ")";
        db.execSQL(CREATE_FLAG_TABLE);
    }

    /**
     * Check if master password is set
     * Equivalent to Python's check_master_password_is_set()
     *
     * @return true if master password is set, false otherwise
     */
    public boolean checkMasterPasswordIsSet() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_FLAG_EXISTENCE,
                    new String[]{COLUMN_FLAG},
                    null, null, null, null, null, "1"
            );
            return cursor != null && cursor.getCount() > 0;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Set master password flag
     * Equivalent to Python's set_master_password(flag_value)
     *
     * @param flagValue the hashed master password value to store
     * @return true if successfully set, false if already exists
     */
    public boolean setMasterPassword(String flagValue) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_FLAG, flagValue);

            long result = db.insert(TABLE_FLAG_EXISTENCE, null, values);
            return result != -1; // Returns true if insert was successful
        } catch (SQLiteException e) {
            // IntegrityError - flag already exists
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Get the stored master password hash
     *
     * @return the stored master password hash, or null if not set
     */
    public String getMasterPasswordHash() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_FLAG_EXISTENCE,
                    new String[]{COLUMN_FLAG},
                    null, null, null, null, null, "1"
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FLAG));
            }
            return null;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Verify master password against stored hash
     *
     * @param hashedPassword the hashed password to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyMasterPassword(String hashedPassword) {
        String storedHash = getMasterPasswordHash();
        return storedHash != null && storedHash.equals(hashedPassword);
    }

    /**
     * Check if URL is present in password table
     * Equivalent to Python's is_url_present_in_password_table(url)
     *
     * @param url the URL to check
     * @return true if URL exists, false otherwise
     * @throws Exception if an error occurs
     */
    public boolean isUrlPresentInPasswordTable(String url) throws Exception {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_PASSWORD_LIST + " WHERE LOWER(" + COLUMN_URL + ") = LOWER(?)",
                    new String[]{url}
            );

            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                return count > 0;
            }
            return false;
        } catch (Exception e) {
            throw new Exception("An error occurred while checking URL: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Insert entry into password table
     * Equivalent to Python's insert_entry_into_password_table(url, username, password)
     *
     * @param url      the URL
     * @param username the username
     * @param password the password
     * @return DatabaseResult with success status and message
     */
    public DatabaseResult insertEntryIntoPasswordTable(String url, String username, String password) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_URL, url);
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD, password);

            long result = db.insert(TABLE_PASSWORD_LIST, null, values);
            if (result != -1) {
                return new DatabaseResult(true, "Entry inserted successfully");
            } else {
                return new DatabaseResult(false, "Error: Failed to insert entry");
            }
        } catch (SQLiteException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                return new DatabaseResult(false, "Error: URL already exists. Please try again.");
            }
            return new DatabaseResult(false, "An error occurred: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Modify entry in password table
     * Equivalent to Python's modify_entry_into_password_table(url, password)
     *
     * @param url      the URL to update
     * @param password the new password
     * @return DatabaseResult with success status and message
     */
    public DatabaseResult modifyEntryIntoPasswordTable(String url, String password) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, password);

            int rowsAffected = db.update(
                    TABLE_PASSWORD_LIST,
                    values,
                    COLUMN_URL + " = ?",
                    new String[]{url}
            );

            if (rowsAffected == 0) {
                return new DatabaseResult(false, "Error: URL does not exist. Please try again.");
            }
            return new DatabaseResult(true, "Entry updated successfully");
        } catch (SQLiteException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                return new DatabaseResult(false, "Error: Integrity error occurred. Please try again.");
            }
            return new DatabaseResult(false, "An error occurred: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Delete entry from password table
     * Equivalent to Python's delete_entry_from_password_table(url)
     *
     * @param url the URL to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteEntryFromPasswordTable(String url) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsDeleted = db.delete(
                    TABLE_PASSWORD_LIST,
                    "LOWER(" + COLUMN_URL + ") = LOWER(?)",
                    new String[]{url}
            );
            return rowsDeleted > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * View all records from password table
     * Equivalent to Python's view_all_records_from_password_table()
     *
     * @return List of PasswordEntry objects
     */
    public List<PasswordEntry> viewAllRecordsFromPasswordTable() {
        List<PasswordEntry> records = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_PASSWORD_LIST,
                    new String[]{COLUMN_URL, COLUMN_USERNAME, COLUMN_PASSWORD},
                    null, null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                    String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                    records.add(new PasswordEntry(url, username, password));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return records;
    }

    /**
     * Helper class to represent a password entry
     */
    public static class PasswordEntry {
        private String url;
        private String username;
        private String password;

        public PasswordEntry(String url, String username, String password) {
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

        @Override
        public String toString() {
            return "URL: " + url + ", Username: " + username + ", Password: " + password;
        }
    }

    /**
     * Helper class to represent database operation results
     */
    public static class DatabaseResult {
        private boolean success;
        private String message;

        public DatabaseResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}

