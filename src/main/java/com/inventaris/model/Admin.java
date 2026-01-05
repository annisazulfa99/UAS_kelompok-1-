// ================================================================
// File: src/main/java/com/inventaris/model/Admin.java
// ================================================================
package com.inventaris.model;

/**
 * Admin Model Class
 * Represents an administrator in the system
 */
public class Admin {
    
    // Attributes
    private int idAdmin;
    private int idUser;
    
    // Related object
    private User user;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    /**
     * Default constructor
     */
    public Admin() {
        // Empty constructor
    }
    
    /**
     * Constructor with idUser
     */
    public Admin(int idUser) {
        this.idUser = idUser;
    }
    
    /**
     * Constructor with all fields
     */
    public Admin(int idAdmin, int idUser) {
        this.idAdmin = idAdmin;
        this.idUser = idUser;
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdAdmin() {
        return idAdmin;
    }
    
    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }
    
    public int getIdUser() {
        return idUser;
    }
    
    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // ============================================================
    // OVERRIDE METHODS
    // ============================================================
    
    @Override
    public String toString() {
        return "Admin{" +
                "idAdmin=" + idAdmin +
                ", idUser=" + idUser +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Admin admin = (Admin) obj;
        return idAdmin == admin.idAdmin;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idAdmin);
    }
}