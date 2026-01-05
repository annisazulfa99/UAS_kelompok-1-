// ================================================================
// File: src/main/java/com/inventaris/model/Peminjam.java
// ================================================================
package com.inventaris.model;

/**
 * Peminjam Model Class
 * Represents a borrower in the system
 */
public class Peminjam {
    
    // Attributes
    private int idPeminjam;
    private int idUser;
    private String noTelepon;
    
    // Related object
    private User user;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    /**
     * Default constructor
     */
    public Peminjam() {
        // Empty constructor
    }
    
    /**
     * Constructor with basic fields
     */
    public Peminjam(int idUser, String noTelepon) {
        this.idUser = idUser;
        this.noTelepon = noTelepon;
    }
    
    /**
     * Constructor with all fields
     */
    public Peminjam(int idPeminjam, int idUser, String noTelepon) {
        this.idPeminjam = idPeminjam;
        this.idUser = idUser;
        this.noTelepon = noTelepon;
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdPeminjam() {
        return idPeminjam;
    }
    
    public void setIdPeminjam(int idPeminjam) {
        this.idPeminjam = idPeminjam;
    }
    
    public int getIdUser() {
        return idUser;
    }
    
    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
    
    public String getNoTelepon() {
        return noTelepon;
    }
    
    public void setNoTelepon(String noTelepon) {
        this.noTelepon = noTelepon;
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
        return "Peminjam{" +
                "idPeminjam=" + idPeminjam +
                ", idUser=" + idUser +
                ", noTelepon='" + noTelepon + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Peminjam peminjam = (Peminjam) obj;
        return idPeminjam == peminjam.idPeminjam;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idPeminjam);
    }
}