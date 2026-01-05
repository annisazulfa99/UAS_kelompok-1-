// ================================================================
// File: src/main/java/com/inventaris/model/LogActivity.java
// ================================================================
package com.inventaris.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LogActivity Model Class
 * Represents an activity log entry in the system
 */
public class LogActivity {
    
    // Attributes
    private int idLog;
    private String username;
    private String keterangan;
    private String aktifitas;
    private String userRole;
    private Timestamp createdAt;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    /**
     * Default constructor
     */
    public LogActivity() {
        // Empty constructor
    }
    
    /**
     * Constructor with basic fields
     */
    public LogActivity(String username, String keterangan, String aktifitas, String userRole) {
        this.username = username;
        this.keterangan = keterangan;
        this.aktifitas = aktifitas;
        this.userRole = userRole;
    }
    
    /**
     * Constructor with all fields
     */
    public LogActivity(int idLog, String username, String keterangan, 
                       String aktifitas, String userRole, Timestamp createdAt) {
        this.idLog = idLog;
        this.username = username;
        this.keterangan = keterangan;
        this.aktifitas = aktifitas;
        this.userRole = userRole;
        this.createdAt = createdAt;
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdLog() {
        return idLog;
    }
    
    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getKeterangan() {
        return keterangan;
    }
    
    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    
    public String getAktifitas() {
        return aktifitas;
    }
    
    public void setAktifitas(String aktifitas) {
        this.aktifitas = aktifitas;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Get formatted date time
     */
    public String getFormattedDateTime() {
        if (createdAt != null) {
            LocalDateTime dateTime = createdAt.toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            return dateTime.format(formatter);
        }
        return "";
    }
    
    /**
     * Get formatted date only
     */
    public String getFormattedDate() {
        if (createdAt != null) {
            LocalDateTime dateTime = createdAt.toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return dateTime.format(formatter);
        }
        return "";
    }
    
    /**
     * Get formatted time only
     */
    public String getFormattedTime() {
        if (createdAt != null) {
            LocalDateTime dateTime = createdAt.toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return dateTime.format(formatter);
        }
        return "";
    }
    
    // ============================================================
    // OVERRIDE METHODS
    // ============================================================
    
    @Override
    public String toString() {
        return "LogActivity{" +
                "idLog=" + idLog +
                ", username='" + username + '\'' +
                ", aktifitas='" + aktifitas + '\'' +
                ", userRole='" + userRole + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LogActivity that = (LogActivity) obj;
        return idLog == that.idLog;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idLog);
    }
}