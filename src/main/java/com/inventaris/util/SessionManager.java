// ================================================================
// File: src/main/java/com/inventaris/util/SessionManager.java
// ================================================================
package com.inventaris.util;

import com.inventaris.model.User;

/**
 * SessionManager - Menyimpan informasi user yang sedang login
 * Menggunakan Singleton Pattern
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    private Integer currentRoleId; // id_peminjam, id_admin, atau id_instansi
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Login user
     */
    public void login(User user, Integer roleId) {
        this.currentUser = user;
        this.currentRoleId = roleId;
        System.out.println("✅ User logged in: " + user.getUsername() + " (" + user.getRole() + ")");
    }
    
    /**
     * Logout user
     */
    public void logout() {
        System.out.println("👋 User logged out: " + (currentUser != null ? currentUser.getUsername() : "Unknown"));
        this.currentUser = null;
        this.currentRoleId = null;
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check user role - Admin
     */
    public boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }
    
    /**
     * Check user role - Peminjam
     */
    public boolean isPeminjam() {
        return currentUser != null && "peminjam".equalsIgnoreCase(currentUser.getRole());
    }
    
    /**
     * Check user role - Instansi
     */
    public boolean isInstansi() {
        return currentUser != null && "instansi".equalsIgnoreCase(currentUser.getRole());
    }
    
    // ============================================================
    // GETTERS
    // ============================================================
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public Integer getCurrentRoleId() {
        return currentRoleId;
    }
    
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    
    public String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
}