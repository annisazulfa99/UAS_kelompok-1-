
// ================================================================
// File: src/main/java/com/inventaris/util/LogActivityUtil.java
// ================================================================
package com.inventaris.util;

import com.inventaris.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * LogActivityUtil - Helper untuk logging aktivitas user
 */
public class LogActivityUtil {
    
    private static final DatabaseConfig dbConfig = DatabaseConfig.getInstance();
    
    /**
     * Log user activity
     */
    public static void log(String username, String keterangan, String aktifitas, String userRole) {
        String sql = "INSERT INTO log_activity (username, keterangan, aktifitas, user_role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, keterangan);
            stmt.setString(3, aktifitas);
            stmt.setString(4, userRole);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
        }
    }
    
    /**
     * Log login activity
     */
    public static void logLogin(String username, String role) {
        log(username, "Login ke sistem", "LOGIN", role);
    }
    
    /**
     * Log logout activity
     */
    public static void logLogout(String username, String role) {
        log(username, "Logout dari sistem", "LOGOUT", role);
    }
    
    /**
     * Log create activity
     */
    public static void logCreate(String username, String role, String entityName, String entityDesc) {
        log(username, "Menambah " + entityName + ": " + entityDesc, "CREATE_" + entityName.toUpperCase(), role);
    }
    
    /**
     * Log update activity
     */
    public static void logUpdate(String username, String role, String entityName, String entityDesc) {
        log(username, "Mengubah " + entityName + ": " + entityDesc, "UPDATE_" + entityName.toUpperCase(), role);
    }
    
    /**
     * Log delete activity
     */
    public static void logDelete(String username, String role, String entityName, String entityDesc) {
        log(username, "Menghapus " + entityName + ": " + entityDesc, "DELETE_" + entityName.toUpperCase(), role);
    }
}