// ================================================================
// File: src/main/java/com/inventaris/model/Berita.java
// ================================================================
package com.inventaris.model;

import java.sql.Timestamp;

/**
 * Berita Model Class
 * Represents a news/announcement in the system
 */
public class Berita {
    
    private int idBerita;
    private String judul;
    private String deskripsi;
    private String warnaBackground;
    private int createdBy;
    private Timestamp createdAt;
    
    // Untuk join dengan tabel user
    private String namaAdmin;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    public Berita() {
    }
    
    public Berita(String judul, String deskripsi, String warnaBackground, int createdBy) {
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.warnaBackground = warnaBackground;
        this.createdBy = createdBy;
    }
    
    public Berita(int idBerita, String judul, String deskripsi, String warnaBackground, 
                  int createdBy, Timestamp createdAt) {
        this.idBerita = idBerita;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.warnaBackground = warnaBackground;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdBerita() {
        return idBerita;
    }
    
    public void setIdBerita(int idBerita) {
        this.idBerita = idBerita;
    }
    
    public String getJudul() {
        return judul;
    }
    
    public void setJudul(String judul) {
        this.judul = judul;
    }
    
    public String getDeskripsi() {
        return deskripsi;
    }
    
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
    
    public String getWarnaBackground() {
        return warnaBackground;
    }
    
    public void setWarnaBackground(String warnaBackground) {
        this.warnaBackground = warnaBackground;
    }
    
    public int getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getNamaAdmin() {
        return namaAdmin;
    }
    
    public void setNamaAdmin(String namaAdmin) {
        this.namaAdmin = namaAdmin;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Get formatted created date
     */
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "-";
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(createdAt);
    }
    
    /**
     * Get short description (for preview)
     */
    public String getShortDeskripsi(int maxLength) {
        if (deskripsi == null) return "";
        if (deskripsi.length() <= maxLength) return deskripsi;
        return deskripsi.substring(0, maxLength) + "...";
    }
    
    // ============================================================
    // OVERRIDE METHODS
    // ============================================================
    
    @Override
    public String toString() {
        return "Berita{" +
                "idBerita=" + idBerita +
                ", judul='" + judul + '\'' +
                ", warnaBackground='" + warnaBackground + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                '}';
    }
}