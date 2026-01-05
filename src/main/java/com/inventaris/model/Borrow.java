// ================================================================
// File: src/main/java/com/inventaris/model/Borrow.java
// Version: 2.0 - WITH APPROVAL WORKFLOW
// ================================================================
package com.inventaris.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Borrow Model Class v2.0
 * WITH Approval Workflow & Return Management
 */
public class Borrow {
    
    // ============================================================
    // BASIC ATTRIBUTES
    // ============================================================
    private int idPeminjaman;
    private Integer idPeminjam;
    private Integer idAdmin;
    private String kodeBarang;
    private int jumlahPinjam;
    private String kondisiBarang;
    private LocalDate tglPeminjaman;
    private LocalDate tglPinjam;
    private LocalDate tglKembali;
    private LocalDate dlKembali;
    private String fotoPengembalian;
    private String statusBarang; // dipinjam, dikembalikan, hilang, rusak, pending
    private Timestamp createdAt;
    
    // ============================================================
    // NEW ATTRIBUTES v2.0 - APPROVAL WORKFLOW
    // ============================================================
    private String statusApproval; // pending_instansi, approved_instansi, rejected_instansi, 
                                   // pending_return, approved_return, rejected_return
    
    private int jumlahBaik;       // Jumlah barang kondisi baik saat dikembalikan
    private int jumlahRusak;      // Jumlah barang rusak saat dikembalikan
    private int jumlahHilang;     // Jumlah barang hilang saat dikembalikan
    
    private String catatanPengembalian;  // Catatan dari peminjam
    private String alasanPenolakan;      // Alasan instansi menolak
    
    private LocalDateTime tglApprovalInstansi;      // Waktu approval pengajuan
    private LocalDateTime tglApprovalPengembalian;  // Waktu approval pengembalian
    
    private Integer idInstansiApproval;  // ID instansi yang melakukan approval
    
    // ============================================================
    // EXTENDED PROPERTIES (from JOIN queries)
    // ============================================================
    private String namaPeminjam;
    private String namaBarang;
    private String noTelepon;
    private String namaInstansi;
    private Integer idInstansiBarang; // ID instansi pemilik barang
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    
    // Di class Borrow.java pastikan ada:
private String keperluan; 

public String getKeperluan() { return keperluan; }
public void setKeperluan(String keperluan) { this.keperluan = keperluan; }

    public Borrow() {
        this.statusApproval = "pending_instansi";
        this.statusBarang = "pending";
    }
    
    public Borrow(int idPeminjam, String kodeBarang, int jumlahPinjam, 
                  LocalDate tglPinjam, LocalDate dlKembali) {
        this.idPeminjam = idPeminjam;
        this.kodeBarang = kodeBarang;
        this.jumlahPinjam = jumlahPinjam;
        this.tglPeminjaman = LocalDate.now();
        this.tglPinjam = tglPinjam;
        this.dlKembali = dlKembali;
        this.statusBarang = "pending";
        this.statusApproval = "pending_instansi";
    }
    
    // ============================================================
    // GETTERS AND SETTERS - BASIC
    // ============================================================
    
    public int getIdPeminjaman() { return idPeminjaman; }
    public void setIdPeminjaman(int idPeminjaman) { this.idPeminjaman = idPeminjaman; }
    
    public Integer getIdPeminjam() { return idPeminjam; }
    public void setIdPeminjam(Integer idPeminjam) { this.idPeminjam = idPeminjam; }
    
    public Integer getIdAdmin() { return idAdmin; }
    public void setIdAdmin(Integer idAdmin) { this.idAdmin = idAdmin; }
    
    public String getKodeBarang() { return kodeBarang; }
    public void setKodeBarang(String kodeBarang) { this.kodeBarang = kodeBarang; }
    
    public int getJumlahPinjam() { return jumlahPinjam; }
    public void setJumlahPinjam(int jumlahPinjam) { this.jumlahPinjam = jumlahPinjam; }
    
    public String getKondisiBarang() { return kondisiBarang; }
    public void setKondisiBarang(String kondisiBarang) { this.kondisiBarang = kondisiBarang; }
    
    public LocalDate getTglPeminjaman() { return tglPeminjaman; }
    public void setTglPeminjaman(LocalDate tglPeminjaman) { this.tglPeminjaman = tglPeminjaman; }
    
    public LocalDate getTglPinjam() { return tglPinjam; }
    public void setTglPinjam(LocalDate tglPinjam) { this.tglPinjam = tglPinjam; }
    
    public LocalDate getTglKembali() { return tglKembali; }
    public void setTglKembali(LocalDate tglKembali) { this.tglKembali = tglKembali; }
    
    public LocalDate getDlKembali() { return dlKembali; }
    public void setDlKembali(LocalDate dlKembali) { this.dlKembali = dlKembali; }
    
    public String getFotoPengembalian() { return fotoPengembalian; }
    public void setFotoPengembalian(String fotoPengembalian) { this.fotoPengembalian = fotoPengembalian; }
    
    public String getStatusBarang() { return statusBarang; }
    public void setStatusBarang(String statusBarang) { this.statusBarang = statusBarang; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    // ============================================================
    // GETTERS AND SETTERS - NEW v2.0
    // ============================================================
    
    public String getStatusApproval() { return statusApproval; }
    public void setStatusApproval(String statusApproval) { this.statusApproval = statusApproval; }
    
    public int getJumlahBaik() { return jumlahBaik; }
    public void setJumlahBaik(int jumlahBaik) { this.jumlahBaik = jumlahBaik; }
    
    public int getJumlahRusak() { return jumlahRusak; }
    public void setJumlahRusak(int jumlahRusak) { this.jumlahRusak = jumlahRusak; }
    
    public int getJumlahHilang() { return jumlahHilang; }
    public void setJumlahHilang(int jumlahHilang) { this.jumlahHilang = jumlahHilang; }
    
    public String getCatatanPengembalian() { return catatanPengembalian; }
    public void setCatatanPengembalian(String catatanPengembalian) { this.catatanPengembalian = catatanPengembalian; }
    
    public String getAlasanPenolakan() { return alasanPenolakan; }
    public void setAlasanPenolakan(String alasanPenolakan) { this.alasanPenolakan = alasanPenolakan; }
    
    public LocalDateTime getTglApprovalInstansi() { return tglApprovalInstansi; }
    public void setTglApprovalInstansi(LocalDateTime tglApprovalInstansi) { 
        this.tglApprovalInstansi = tglApprovalInstansi; 
    }
    
    public LocalDateTime getTglApprovalPengembalian() { return tglApprovalPengembalian; }
    public void setTglApprovalPengembalian(LocalDateTime tglApprovalPengembalian) { 
        this.tglApprovalPengembalian = tglApprovalPengembalian; 
    }
    
    public Integer getIdInstansiApproval() { return idInstansiApproval; }
    public void setIdInstansiApproval(Integer idInstansiApproval) { 
        this.idInstansiApproval = idInstansiApproval; 
    }
    
    // ============================================================
    // GETTERS AND SETTERS - EXTENDED
    // ============================================================
    
    public String getNamaPeminjam() { return namaPeminjam; }
    public void setNamaPeminjam(String namaPeminjam) { this.namaPeminjam = namaPeminjam; }
    
    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }
    
    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
    
    public String getNamaInstansi() { return namaInstansi; }
    public void setNamaInstansi(String namaInstansi) { this.namaInstansi = namaInstansi; }
    
    public Integer getIdInstansiBarang() { return idInstansiBarang; }
    public void setIdInstansiBarang(Integer idInstansiBarang) { this.idInstansiBarang = idInstansiBarang; }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Calculate remaining days until deadline
     */
    public long getSisaHari() {
        if (dlKembali != null) {
            return ChronoUnit.DAYS.between(LocalDate.now(), dlKembali);
        }
        return 0;
    }
    
    
    /**
     * Check if borrowing is overdue
     */
    public boolean isOverdue() {
        if (dlKembali != null && "approved_instansi".equalsIgnoreCase(statusApproval)) {
            return LocalDate.now().isAfter(dlKembali);
        }
        return false;
    }
    
    /**
     * Check if borrowing is active (approved and borrowed)
     */
    public boolean isActive() {
        return "approved_instansi".equalsIgnoreCase(statusApproval) && 
               "dipinjam".equalsIgnoreCase(statusBarang);
    }
    
    /**
     * Check if waiting for instansi approval
     */
    public boolean isPendingInstansi() {
        return "pending_instansi".equalsIgnoreCase(statusApproval);
    }
    
    /**
     * Check if waiting for return approval
     */
    public boolean isPendingReturn() {
        return "pending_return".equalsIgnoreCase(statusApproval);
    }
    
    /**
     * Check if rejected by instansi
     */
    public boolean isRejected() {
        return "rejected_instansi".equalsIgnoreCase(statusApproval) ||
               "rejected_return".equalsIgnoreCase(statusApproval);
    }
    
    /**
     * Check if completed (return approved)
     */
    public boolean isCompleted() {
        return "approved_return".equalsIgnoreCase(statusApproval);
    }
    
    /**
     * Get total jumlah pengembalian
     */
    public int getTotalPengembalian() {
        return jumlahBaik + jumlahRusak + jumlahHilang;
    }
    
    /**
     * Check if pengembalian valid (total = jumlah_pinjam)
     */
    public boolean isPengembalianValid() {
        return getTotalPengembalian() == jumlahPinjam;
    }
    
    /**
     * Check if ada barang rusak/hilang
     */
    public boolean hasProblematicReturn() {
        return jumlahRusak > 0 || jumlahHilang > 0;
    }
    
    /**
     * Get status text untuk display
     */
    public String getStatusText() {
        if (statusApproval == null) return "Unknown";
        
        switch (statusApproval) {
            case "pending_instansi":
                return "⏳ Menunggu Persetujuan Instansi";
            case "approved_instansi":
                return "✅ Sedang Dipinjam";
            case "rejected_instansi":
                return "❌ Ditolak Instansi";
            case "pending_return":
                return "🔄 Menunggu Verifikasi Pengembalian";
            case "approved_return":
                return "✓ Selesai";
            case "rejected_return":
                return "❌ Pengembalian Ditolak";
            default:
                return statusApproval;
        }
    }
    
    // ============================================================
    // OVERRIDE METHODS
    // ============================================================
    
    @Override
    public String toString() {
        return "Borrow{" +
                "id=" + idPeminjaman +
                ", peminjam='" + namaPeminjam + '\'' +
                ", barang='" + namaBarang + '\'' +
                ", status='" + statusApproval + '\'' +
                ", jumlah=" + jumlahPinjam +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Borrow borrow = (Borrow) obj;
        return idPeminjaman == borrow.idPeminjaman;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idPeminjaman);
    }
    
    
    
}