// ================================================================
// File: src/main/java/com/inventaris/model/User.java
// ================================================================
package com.inventaris.model;

import java.sql.Timestamp;

/**
 * User Model Class
 * Represents a user in the system (Admin, Peminjam, or Instansi)
 */
public class User {
    
    // Attributes
    private int idUser;
    private String username;
    private String password;
    private String nama;
    private String role; // admin, peminjam, instansi
    private String status; // aktif, nonaktif
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    /**
     * Default constructor
     */
    public User() {
        // Empty constructor
    }
    
    /**
     * Constructor with basic fields
     */
    public User(String username, String password, String nama, String role) {
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.role = role;
        this.status = "aktif";
    }
    
    /**
     * Constructor with all fields
     */
    public User(int idUser, String username, String password, String nama, 
                String role, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdUser() {
        return idUser;
    }
    
    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNama() {
        return nama;
    }
    
    public void setNama(String nama) {
        this.nama = nama;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user is peminjam
     */
    public boolean isPeminjam() {
        return "peminjam".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user is instansi
     */
    public boolean isInstansi() {
        return "instansi".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user is active
     */
    public boolean isActive() {
        return "aktif".equalsIgnoreCase(this.status);
    }
    
    // ============================================================
    // OVERRIDE METHODS
    // ============================================================
    
    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", username='" + username + '\'' +
                ", nama='" + nama + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return idUser == user.idUser;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idUser);
    }
}