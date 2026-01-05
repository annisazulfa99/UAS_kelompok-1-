// ================================================================
// File: src/main/java/com/inventaris/model/Lapor.java
// ================================================================
package com.inventaris.model;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Lapor Model Class
 * Represents a report for damaged/lost items
 */
public class Lapor {
    
    // Attributes
    private int idLaporan;
    private String noLaporan;
    private int idPeminjaman;
    private String kodeBarang;
    private String status; // diproses, selesai, ditolak
    private LocalDate tglLaporan;
    private Timestamp createdAt;
    private String keterangan; 
    // Extended properties (from JOIN queries)
    private String namaPeminjam;
    private String namaBarang;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    /**
     * Default constructor
     */
    public Lapor() {
        // Empty constructor
    }
    
    
    
     public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    
    
    /**
     * Constructor with basic fields
     */
    public Lapor(String noLaporan, int idPeminjaman, String kodeBarang) {
        this.noLaporan = noLaporan;
        this.idPeminjaman = idPeminjaman;
        this.kodeBarang = kodeBarang;
        this.status = "diproses";
        this.tglLaporan = LocalDate.now();
    }
    
    /**
     * Constructor with all fields
     */
    public Lapor(int idLaporan, String noLaporan, int idPeminjaman, 
                 String kodeBarang, String status, LocalDate tglLaporan) {
        this.idLaporan = idLaporan;
        this.noLaporan = noLaporan;
        this.idPeminjaman = idPeminjaman;
        this.kodeBarang = kodeBarang;
        this.status = status;
        this.tglLaporan = tglLaporan;
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdLaporan() {
        return idLaporan;
    }
    
    public void setIdLaporan(int idLaporan) {
        this.idLaporan = idLaporan;
    }
    
    public String getNoLaporan() {
        return noLaporan;
    }
    
    public void setNoLaporan(String noLaporan) {
        this.noLaporan = noLaporan;
    }
    
    public int getIdPeminjaman() {
        return idPeminjaman;
    }
    
    public void setIdPeminjaman(int idPeminjaman) {
        this.idPeminjaman = idPeminjaman;
    }
    
    public String getKodeBarang() {
        return kodeBarang;
    }
    
    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getTglLaporan() {
        return tglLaporan;
    }
    
    public void setTglLaporan(LocalDate tglLaporan) {
        this.tglLaporan = tglLaporan;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    // Extended properties
    
    public String getNamaPeminjam() {
        return namaPeminjam;
    }
    
    public void setNamaPeminjam(String namaPeminjam) {
        this.namaPeminjam = namaPeminjam;
    }
    
    public String getNamaBarang() {
        return namaBarang;
    }
    
    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Check if report is being processed
     */
    public boolean isDiproses() {
        return "diproses".equalsIgnoreCase(status);
    }
    
    /**
     * Check if report is completed
     */
    public boolean isSelesai() {
        return "selesai".equalsIgnoreCase(status);
    }
    
    /**
     * Check if report is rejected
     */
    public boolean isDitolak() {
        return "ditolak".equalsIgnoreCase(status);
    }
    
    // ============================================================
    // OVERRIDE METHODS
    // ============================================================
    
    @Override
    public String toString() {
        return "Lapor{" +
                "idLaporan=" + idLaporan +
                ", noLaporan='" + noLaporan + '\'' +
                ", namaBarang='" + namaBarang + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Lapor lapor = (Lapor) obj;
        return idLaporan == lapor.idLaporan;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idLaporan);
    }
}