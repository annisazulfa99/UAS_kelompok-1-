// ================================================================
// File: src/main/java/com/inventaris/model/CartItem.java
// NEW MODEL: Shopping cart item for borrowing
// ================================================================
package com.inventaris.model;

import java.time.LocalDate;

/**
 * CartItem - Item dalam keranjang peminjaman
 * Temporary storage sebelum submit peminjaman
 */
public class CartItem {
    
    private Barang barang;
    private int jumlahPinjam;
    private LocalDate tglPinjam;
    private LocalDate tglKembali;
    private String catatan;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    public CartItem() {}
    
    public CartItem(Barang barang, int jumlahPinjam, LocalDate tglPinjam, LocalDate tglKembali) {
        this.barang = barang;
        this.jumlahPinjam = jumlahPinjam;
        this.tglPinjam = tglPinjam;
        this.tglKembali = tglKembali;
    }
    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public Barang getBarang() {
        return barang;
    }
    
    public void setBarang(Barang barang) {
        this.barang = barang;
    }
    
    public int getJumlahPinjam() {
        return jumlahPinjam;
    }
    
    public void setJumlahPinjam(int jumlahPinjam) {
        this.jumlahPinjam = jumlahPinjam;
    }
    
    public LocalDate getTglPinjam() {
        return tglPinjam;
    }
    
    public void setTglPinjam(LocalDate tglPinjam) {
        this.tglPinjam = tglPinjam;
    }
    
    public LocalDate getTglKembali() {
        return tglKembali;
    }
    
    public void setTglKembali(LocalDate tglKembali) {
        this.tglKembali = tglKembali;
    }
    
    public String getCatatan() {
        return catatan;
    }
    
    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Get duration in days
     */
    public long getDurasiHari() {
        if (tglPinjam != null && tglKembali != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(tglPinjam, tglKembali);
        }
        return 0;
    }
    
    /**
     * Validate if item is valid for borrowing
     */
    public boolean isValid() {
        return barang != null 
            && jumlahPinjam > 0 
            && jumlahPinjam <= barang.getJumlahTersedia()
            && tglPinjam != null 
            && tglKembali != null
            && !tglKembali.isBefore(tglPinjam);
    }
    
    /**
     * Get validation error message
     */
    public String getValidationError() {
        if (barang == null) return "Barang tidak valid";
        if (jumlahPinjam <= 0) return "Jumlah pinjam harus lebih dari 0";
        if (jumlahPinjam > barang.getJumlahTersedia()) return "Stok tidak mencukupi";
        if (tglPinjam == null) return "Tanggal pinjam belum dipilih";
        if (tglKembali == null) return "Tanggal kembali belum dipilih";
        if (tglKembali.isBefore(tglPinjam)) return "Tanggal kembali tidak valid";
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %d unit (%s s/d %s)", 
            barang.getNamaBarang(), 
            jumlahPinjam,
            tglPinjam,
            tglKembali
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CartItem cartItem = (CartItem) obj;
        return barang.getIdBarang() == cartItem.barang.getIdBarang();
    }
    
    @Override
    public int hashCode() {
        return barang != null ? barang.hashCode() : 0;
    }
}