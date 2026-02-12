package com.example.passwordmanager;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {

    private static final String ALGORITHM = "AES/CFB/NoPadding";
    private static final String AES = "AES";
    private static final int KEY_SIZE = 32; // 256 bits
    private static final int IV_SIZE = 16;  // 128 bits

    /**
     * Encrypts the input string using AES-256 CFB mode.
     *
     * @param inputString The plaintext string to encrypt
     * @param key         The encryption key (will be padded/truncated to 32 bytes)
     * @return Base64 encoded string containing IV + encrypted data
     * @throws Exception if encryption fails
     */
    public static String aes256Encrypt(String inputString, String key) throws Exception {
        if (inputString == null || key == null) {
            throw new IllegalArgumentException("Input string and key must not be null");
        }

        // Prepare the key: repeat until at least 32 bytes, then slice to 32 bytes
        String preparedKey = prepareKey(key);

        // Generate a random initialization vector (IV)
        byte[] iv = new byte[IV_SIZE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        // Create cipher instance
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(preparedKey.getBytes(StandardCharsets.UTF_8), AES);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Initialize cipher for encryption
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        // Encrypt the data
        byte[] encryptedData = cipher.doFinal(inputString.getBytes(StandardCharsets.UTF_8));

        // Combine IV and encrypted data
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        // Return as Base64 encoded string
        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    /**
     * Decrypts the encrypted string using AES-256 CFB mode.
     *
     * @param encryptedString Base64 encoded string containing IV + encrypted data
     * @param key             The decryption key (will be padded/truncated to 32 bytes)
     * @return The decrypted plaintext string
     * @throws Exception if decryption fails
     */
    public static String aes256Decrypt(String encryptedString, String key) throws Exception {
        if (encryptedString == null || key == null) {
            throw new IllegalArgumentException("Encrypted string and key must not be null");
        }

        // Prepare the key: repeat until at least 32 bytes, then slice to 32 bytes
        String preparedKey = prepareKey(key);

        // Decode the Base64 encoded string
        byte[] decodedData = Base64.decode(encryptedString, Base64.DEFAULT);

        // Extract the IV and the encrypted data
        byte[] iv = new byte[IV_SIZE];
        byte[] encryptedData = new byte[decodedData.length - IV_SIZE];
        System.arraycopy(decodedData, 0, iv, 0, IV_SIZE);
        System.arraycopy(decodedData, IV_SIZE, encryptedData, 0, encryptedData.length);

        // Create cipher instance
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(preparedKey.getBytes(StandardCharsets.UTF_8), AES);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // Initialize cipher for decryption
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        // Decrypt the data
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // Return as string
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * Prepares the key by repeating it until it reaches 32 bytes, then truncating to exactly 32 bytes.
     *
     * @param key The original key string
     * @return A 32-byte key string
     */
    private static String prepareKey(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }

        // Calculate how many times we need to repeat the key
        int repeatCount = (KEY_SIZE / key.length()) + 1;

        // Repeat the key
        StringBuilder repeatedKey = new StringBuilder();
        for (int i = 0; i < repeatCount; i++) {
            repeatedKey.append(key);
        }

        // Truncate to exactly 32 bytes
        return repeatedKey.substring(0, KEY_SIZE);
    }
}

