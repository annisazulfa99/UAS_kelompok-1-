// ================================================================
// File: src/main/java/com/inventaris/model/Barang.java
// UPDATED: Added foto_url field for image display
// ================================================================
package com.inventaris.model;

import java.sql.Timestamp;

public class Barang {
    
    private int idBarang;
    private Integer idInstansi;
    private String kodeBarang;
    private String namaBarang;
    private String lokasiBarang;
    private int jumlahTotal;
    private int jumlahTersedia;
    private String deskripsi;
    private String kondisiBarang;
    private String status;
    private String foto; // Path lokal
    private String fotoUrl; // URL untuk display (NEW)
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Extended property
    private String namaPemilik;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    public Barang() {}
    
    public Barang(String kodeBarang, String namaBarang, String lokasiBarang, 
                  int jumlahTotal, String deskripsi) {
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.lokasiBarang = lokasiBarang;
        this.jumlahTotal = jumlahTotal;
        this.jumlahTersedia = jumlahTotal;
        this.deskripsi = deskripsi;
        this.kondisiBarang = "baik";
        this.status = "tersedia";
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdBarang() {
        return idBarang;
    }
    
    public void setIdBarang(int idBarang) {
        this.idBarang = idBarang;
    }
    
    public Integer getIdInstansi() {
        return idInstansi;
    }
    
    public void setIdInstansi(Integer idInstansi) {
        this.idInstansi = idInstansi;
    }
    
    public String getKodeBarang() {
        return kodeBarang;
    }
    
    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }
    
    public String getNamaBarang() {
        return namaBarang;
    }
    
    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }
    
    public String getLokasiBarang() {
        return lokasiBarang;
    }
    
    public void setLokasiBarang(String lokasiBarang) {
        this.lokasiBarang = lokasiBarang;
    }
    
    public int getJumlahTotal() {
        return jumlahTotal;
    }
    
    public void setJumlahTotal(int jumlahTotal) {
        this.jumlahTotal = jumlahTotal;
    }
    
    public int getJumlahTersedia() {
        return jumlahTersedia;
    }
    
    public void setJumlahTersedia(int jumlahTersedia) {
        this.jumlahTersedia = jumlahTersedia;
    }
    
    public String getDeskripsi() {
        return deskripsi;
    }
    
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
    
    public String getKondisiBarang() {
        return kondisiBarang;
    }
    
    public void setKondisiBarang(String kondisiBarang) {
        this.kondisiBarang = kondisiBarang;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getFoto() {
        return foto;
    }
    
    public void setFoto(String foto) {
        this.foto = foto;
    }
    
    // NEW: Foto URL getter/setter
    public String getFotoUrl() {
        return fotoUrl != null ? fotoUrl : "https://via.placeholder.com/200x150?text=No+Image";
    }
    
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
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
    
    public String getNamaPemilik() {
        return namaPemilik;
    }
    
    public void setNamaPemilik(String namaPemilik) {
        this.namaPemilik = namaPemilik;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    public boolean isAvailable() {
        return jumlahTersedia > 0 && "tersedia".equalsIgnoreCase(status);
    }
    
    public boolean isGoodCondition() {
        return "baik".equalsIgnoreCase(kondisiBarang);
    }
    
    public double getStockPercentage() {
        if (jumlahTotal == 0) return 0;
        return ((double) jumlahTersedia / jumlahTotal) * 100;
    }
    
    @Override
    public String toString() {
        return namaBarang + " (" + kodeBarang + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Barang barang = (Barang) obj;
        return idBarang == barang.idBarang;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idBarang);
    }
}