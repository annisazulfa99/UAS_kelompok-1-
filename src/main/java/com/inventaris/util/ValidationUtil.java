// ================================================================
// File: src/main/java/com/inventaris/util/ValidationUtil.java
// ================================================================
package com.inventaris.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * ValidationUtil - Helper untuk validasi input
 */
public class ValidationUtil {
    
    // Regex patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern KODE_BARANG_PATTERN = Pattern.compile("^[A-Z0-9-]{3,20}$");
    
    /**
     * Validate if string is empty or null
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        return !isEmpty(username) && USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return !isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        return !isEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validate kode barang format
     */
    public static boolean isValidKodeBarang(String kode) {
        return !isEmpty(kode) && KODE_BARANG_PATTERN.matcher(kode).matches();
    }
    
    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (isEmpty(password)) return false;
        return password.length() >= 6; // Minimal 6 karakter
    }
    
    /**
     * Validate if passwords match
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * Validate positive number
     */
    public static boolean isPositiveNumber(String value) {
        try {
            int num = Integer.parseInt(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate non-negative number
     */
    public static boolean isNonNegativeNumber(String value) {
        try {
            int num = Integer.parseInt(value);
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate date range (start before end)
     */
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return false;
        return !startDate.isAfter(endDate);
    }
    
    /**
     * Validate date is not in the past
     */
    public static boolean isNotPastDate(LocalDate date) {
        if (date == null) return false;
        return !date.isBefore(LocalDate.now());
    }
    
    /**
     * Get validation error message
     */
    public static String getValidationMessage(String fieldName, String errorType) {
        switch (errorType) {
            case "empty":
                return fieldName + " tidak boleh kosong!";
            case "invalid_format":
                return "Format " + fieldName + " tidak valid!";
            case "too_short":
                return fieldName + " terlalu pendek!";
            case "too_long":
                return fieldName + " terlalu panjang!";
            case "not_match":
                return fieldName + " tidak cocok!";
            case "invalid_number":
                return fieldName + " harus berupa angka!";
            case "negative_number":
                return fieldName + " tidak boleh negatif!";
            case "past_date":
                return fieldName + " tidak boleh tanggal lampau!";
            case "invalid_range":
                return "Range " + fieldName + " tidak valid!";
            default:
                return fieldName + " tidak valid!";
        }
    }
    
    /**
     * Sanitize input (remove special characters)
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[<>\"']", "");
    }
}

