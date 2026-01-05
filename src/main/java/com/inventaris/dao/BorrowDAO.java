// ================================================================
// File: src/main/java/com/inventaris/dao/BorrowDAO.java
// Version: 2.0 - WITH APPROVAL WORKFLOW
// ================================================================

package com.inventaris.dao;

import com.inventaris.config.DatabaseConfig;
import com.inventaris.model.Borrow;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;    // ← TAMBAHKAN INI
import java.util.Map;        // ← TAMBAHKAN INI

/**
 * BorrowDAO v2.0 - WITH APPROVAL WORKFLOW
 * Handles: Instansi Approval, Return Management, Stock Control
 */
public class BorrowDAO {
    
    private final DatabaseConfig dbConfig;
    
    public BorrowDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    // ============================================================
    // CREATE - PENGAJUAN PEMINJAMAN (PEMINJAM)
    // ============================================================
    
    /**
     * Create peminjaman baru (status: pending_instansi)
     * Stok barang TIDAK dikurangi sampai diapprove instansi
     */
    public boolean create(Borrow borrow) {
        // 👇 UPDATE SQL: Tambahkan 'keperluan'
        String sql = "INSERT INTO borrow (id_peminjam, kode_barang, jumlah_pinjam, " +
                     "tgl_peminjaman, tgl_pinjam, dl_kembali, status_barang, status_approval, keperluan) " + // <--- Tambah keperluan
                     "VALUES (?, ?, ?, ?, ?, ?, 'pending', 'pending_instansi', ?)"; // <--- Tambah ? di akhir
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, borrow.getIdPeminjam());
            stmt.setString(2, borrow.getKodeBarang());
            stmt.setInt(3, borrow.getJumlahPinjam());
            stmt.setDate(4, Date.valueOf(borrow.getTglPeminjaman()));
            stmt.setDate(5, Date.valueOf(borrow.getTglPinjam()));
            stmt.setDate(6, Date.valueOf(borrow.getDlKembali()));
            
            // 👇 TAMBAHKAN INI (Parameter ke-7)
            stmt.setString(7, borrow.getKeperluan()); 
            
            boolean inserted = stmt.executeUpdate() > 0;
            // ... (logika return sama seperti sebelumnya)
            return inserted;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ============================================================
    // APPROVAL WORKFLOW - INSTANSI
    // ============================================================
    
    /**
     * Get pending pengajuan untuk instansi tertentu
     */
    public List<Borrow> getPendingInstansiApproval(Integer instansiId) {
        List<Borrow> list = new ArrayList<>();
        
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang, " +
                     "i.nama_instansi, br.id_instansi as id_instansi_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "WHERE b.status_approval = 'pending_instansi' " +
                     "AND br.id_instansi = ? " +
                     "ORDER BY b.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, instansiId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pending approvals: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Approve pengajuan oleh instansi
     * - Update status_approval = 'approved_instansi'
     * - Update status_barang = 'dipinjam'
     * - KURANGI stok barang
     */
    public boolean approveByInstansi(int borrowId, Integer instansiId) {
        Connection conn = null;
        
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Get borrow details
            Borrow borrow = getById(borrowId);
            if (borrow == null) {
                conn.rollback();
                return false;
            }
            
            // Update borrow status
            String sqlUpdate = "UPDATE borrow SET " +
                              "status_approval = 'approved_instansi', " +
                              "status_barang = 'dipinjam', " +
                              "id_instansi_approval = ?, " +
                              "tgl_approval_instansi = NOW() " +
                              "WHERE id_peminjaman = ?";
            
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setInt(1, instansiId);
            stmtUpdate.setInt(2, borrowId);
            boolean updated = stmtUpdate.executeUpdate() > 0;
            stmtUpdate.close();
            
            if (!updated) {
                conn.rollback();
                return false;
            }
            
            // KURANGI stok barang
            String sqlStock = "UPDATE barang SET jumlah_tersedia = jumlah_tersedia - ? " +
                             "WHERE kode_barang = ? AND jumlah_tersedia >= ?";
            
            PreparedStatement stmtStock = conn.prepareStatement(sqlStock);
            stmtStock.setInt(1, borrow.getJumlahPinjam());
            stmtStock.setString(2, borrow.getKodeBarang());
            stmtStock.setInt(3, borrow.getJumlahPinjam());
            boolean stockUpdated = stmtStock.executeUpdate() > 0;
            stmtStock.close();
            
            if (!stockUpdated) {
                System.err.println("❌ Stok tidak cukup!");
                conn.rollback();
                return false;
            }
            
            conn.commit();
            System.out.println("✅ Pengajuan disetujui, stok dikurangi");
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error approving borrow: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    
   /**
 * Reject pengajuan oleh instansi
 * ✅ FIXED v3: status_barang tetap 'pending' (bukan 'ditolak')
 */
public boolean rejectByInstansi(int borrowId, String alasan, Integer instansiId) {
    Connection conn = null;
    
    try {
        conn = dbConfig.getConnection();
        conn.setAutoCommit(false);
        
        // Validate borrow exists
        Borrow borrow = getById(borrowId);
        if (borrow == null) {
            System.err.println("❌ Borrow ID " + borrowId + " not found!");
            conn.rollback();
            return false;
        }
        
        System.out.println("📋 Current status: " + borrow.getStatusApproval());
        
        // Validate status is pending
        if (!"pending_instansi".equalsIgnoreCase(borrow.getStatusApproval())) {
            System.err.println("❌ Status bukan pending_instansi! Current: " + borrow.getStatusApproval());
            conn.rollback();
            return false;
        }
        
        // ✅ FIX: status_barang tetap 'pending' karena ditolak sebelum dipinjam
        String sql = "UPDATE borrow SET " +
                    "status_approval = 'rejected_instansi', " +
                    "status_barang = 'pending', " +  // ← CHANGED: tetap 'pending'
                    "alasan_penolakan = ?, " +
                    "id_instansi_approval = ?, " +
                    "tgl_approval_instansi = NOW() " +
                    "WHERE id_peminjaman = ?";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, alasan != null ? alasan : "Ditolak oleh instansi");
        stmt.setInt(2, instansiId);
        stmt.setInt(3, borrowId);
        
        int rowsAffected = stmt.executeUpdate();
        stmt.close();
        
        if (rowsAffected > 0) {
            conn.commit();
            System.out.println("✅ Pengajuan ID " + borrowId + " ditolak dengan alasan: " + alasan);
            return true;
        } else {
            conn.rollback();
            System.err.println("⚠️ Tidak ada baris yang diupdate");
            return false;
        }
        
    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        System.err.println("❌ Error rejecting borrow: " + e.getMessage());
        e.printStackTrace();
        return false;
        
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}    // ============================================================
    // RETURN WORKFLOW - PEMINJAM
    // ============================================================
    
    /**
     * Submit pengembalian oleh peminjam
     * - Update jumlah_baik, jumlah_rusak, jumlah_hilang
     * - Update status_approval = 'pending_return'
     * - Stok BELUM ditambah (tunggu approval instansi)
     */
    public boolean submitReturn(int borrowId, int jumlahBaik, int jumlahRusak, 
                                int jumlahHilang, String catatan) {
        
        String sql = "UPDATE borrow SET " +
                     "status_approval = 'pending_return', " +
                     "jumlah_baik = ?, " +
                     "jumlah_rusak = ?, " +
                     "jumlah_hilang = ?, " +
                     "catatan_pengembalian = ?, " +
                     "tgl_kembali = CURDATE() " +
                     "WHERE id_peminjaman = ? " +
                     "AND status_approval = 'approved_instansi'";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, jumlahBaik);
            stmt.setInt(2, jumlahRusak);
            stmt.setInt(3, jumlahHilang);
            stmt.setString(4, catatan);
            stmt.setInt(5, borrowId);
            
            boolean updated = stmt.executeUpdate() > 0;
            
            if (updated) {
                System.out.println("✅ Pengajuan pengembalian berhasil (menunggu verifikasi)");
            }
            
            return updated;
            
        } catch (SQLException e) {
            System.err.println("Error submitting return: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get pending pengembalian untuk instansi tertentu
     */
    public List<Borrow> getPendingReturnApproval(Integer instansiId) {
        List<Borrow> list = new ArrayList<>();
        
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang, " +
                     "i.nama_instansi, br.id_instansi as id_instansi_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "WHERE b.status_approval = 'pending_return' " +
                     "AND br.id_instansi = ? " +
                     "ORDER BY b.tgl_kembali DESC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, instansiId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting pending returns: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Approve pengembalian oleh instansi
     * - Update status_approval = 'approved_return'
     * - Update status_barang = 'dikembalikan'
     * - TAMBAH stok barang (HANYA jumlah_baik)
     */
    public boolean approveReturn(int borrowId, Integer instansiId) {
        Connection conn = null;
        
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Get borrow details
            Borrow borrow = getById(borrowId);
            if (borrow == null) {
                conn.rollback();
                return false;
            }
            
            // Update borrow status
            String sqlUpdate = "UPDATE borrow SET " +
                              "status_approval = 'approved_return', " +
                              "status_barang = 'dikembalikan', " +
                              "tgl_approval_pengembalian = NOW() " +
                              "WHERE id_peminjaman = ?";
            
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setInt(1, borrowId);
            stmtUpdate.executeUpdate();
            stmtUpdate.close();
            
            // TAMBAH stok barang (HANYA yang kondisi baik)
            if (borrow.getJumlahBaik() > 0) {
                String sqlStock = "UPDATE barang SET jumlah_tersedia = jumlah_tersedia + ? " +
                                 "WHERE kode_barang = ?";
                
                PreparedStatement stmtStock = conn.prepareStatement(sqlStock);
                stmtStock.setInt(1, borrow.getJumlahBaik());
                stmtStock.setString(2, borrow.getKodeBarang());
                stmtStock.executeUpdate();
                stmtStock.close();
                
                System.out.println("✅ Stok bertambah: " + borrow.getJumlahBaik() + " unit");
            }
            
            conn.commit();
            System.out.println("✅ Pengembalian disetujui");
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error approving return: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    
    /**
     * Reject pengembalian oleh instansi
     */
    public boolean rejectReturn(int borrowId, String alasan, Integer instansiId) {
        String sql = "UPDATE borrow SET " +
                     "status_approval = 'rejected_return', " +
                     "alasan_penolakan = ?, " +
                     "tgl_approval_pengembalian = NOW() " +
                     "WHERE id_peminjaman = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, alasan);
            stmt.setInt(2, borrowId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error rejecting return: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ============================================================
    // READ OPERATIONS
    // ============================================================
    
    public Borrow getById(int id) {
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang, " +
                     "i.nama_instansi, br.id_instansi as id_instansi_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "WHERE b.id_peminjaman = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractBorrowFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting borrow: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<Borrow> getAll() {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang, " +
                     "i.nama_instansi, br.id_instansi as id_instansi_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "ORDER BY b.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all borrow: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    public List<Borrow> getByPeminjamId(int peminjamId) {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang, " +
                     "i.nama_instansi, br.id_instansi as id_instansi_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "WHERE b.id_peminjam = ? " +
                     "ORDER BY b.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, peminjamId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting borrow by peminjam: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get active borrows (approved dan sedang dipinjam)
     */
    public List<Borrow> getActiveBorrows() {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang, " +
                     "i.nama_instansi, br.id_instansi as id_instansi_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "WHERE b.status_approval = 'approved_instansi' " +
                     "ORDER BY b.dl_kembali ASC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting active borrows: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get borrows by instansi (barang milik instansi tertentu)
     */
    public List<Borrow> getByInstansiBarang(Integer instansiId) {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang, " +
                     "i.nama_instansi, br.id_instansi as id_instansi_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "WHERE br.id_instansi = ? " +
                     "AND b.status_approval = 'approved_instansi' " +
                     "ORDER BY b.dl_kembali ASC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, instansiId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting borrows by instansi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get overdue borrows (untuk laporan)
     */
    public List<Borrow> getOverdueBorrows() {
        List<Borrow> list = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_peminjam, p.no_telepon, br.nama_barang, " +
                     "i.nama_instansi, br.id_instansi as id_instansi_barang " +
                     "FROM borrow b " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "WHERE b.status_approval = 'approved_instansi' " +
                     "AND b.dl_kembali < CURDATE() " +
                     "ORDER BY b.dl_kembali ASC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractBorrowFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting overdue borrows: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    // ============================================================
    // LEGACY METHODS (DEPRECATED - Untuk backward compatibility)
    // ============================================================
    
    @Deprecated
    public List<Borrow> getPendingBorrows() {
        // Sekarang gunakan getPendingInstansiApproval()
        return new ArrayList<>();
    }
    
    @Deprecated
    public boolean approve(int borrowId, int adminId) {
        // Sekarang gunakan approveByInstansi()
        return false;
    }
    
    @Deprecated
    public boolean reject(int borrowId) {
        // Sekarang gunakan rejectByInstansi()
        return false;
    }
    
    @Deprecated
    public boolean returnItem(int borrowId, String kondisi, String foto) {
        // Sekarang gunakan submitReturn() + approveReturn()
        return false;
    }
    
    // ============================================================
    // HELPER METHOD
    // ============================================================
    
    private Borrow extractBorrowFromResultSet(ResultSet rs) throws SQLException {
        Borrow borrow = new Borrow();
        
        // Basic fields
        borrow.setIdPeminjaman(rs.getInt("id_peminjaman"));
        borrow.setIdPeminjam(rs.getInt("id_peminjam"));
        
        int adminId = rs.getInt("id_admin");
        borrow.setIdAdmin(rs.wasNull() ? null : adminId);
        
        borrow.setKodeBarang(rs.getString("kode_barang"));
        borrow.setJumlahPinjam(rs.getInt("jumlah_pinjam"));
        borrow.setKondisiBarang(rs.getString("kondisi_barang"));
        
        // Handle tanggal yang mungkin null
        if (rs.getDate("tgl_peminjaman") != null)
            borrow.setTglPeminjaman(rs.getDate("tgl_peminjaman").toLocalDate());
        
        if (rs.getDate("tgl_pinjam") != null)
            borrow.setTglPinjam(rs.getDate("tgl_pinjam").toLocalDate());
        
        Date tglKembali = rs.getDate("tgl_kembali");
        borrow.setTglKembali(tglKembali != null ? tglKembali.toLocalDate() : null);
        
        if (rs.getDate("dl_kembali") != null)
            borrow.setDlKembali(rs.getDate("dl_kembali").toLocalDate());
            
        borrow.setFotoPengembalian(rs.getString("foto_pengembalian"));
        borrow.setStatusBarang(rs.getString("status_barang"));
        borrow.setCreatedAt(rs.getTimestamp("created_at"));
        
        // New fields v2.0
        borrow.setStatusApproval(rs.getString("status_approval"));
        borrow.setJumlahBaik(rs.getInt("jumlah_baik"));
        borrow.setJumlahRusak(rs.getInt("jumlah_rusak"));
        borrow.setJumlahHilang(rs.getInt("jumlah_hilang"));
        borrow.setCatatanPengembalian(rs.getString("catatan_pengembalian"));
        borrow.setAlasanPenolakan(rs.getString("alasan_penolakan"));
        
        // ========================================================
        // 👇 TAMBAHAN PENTING: AMBIL DATA KEPERLUAN/SURAT
        // ========================================================
        try {
            borrow.setKeperluan(rs.getString("keperluan")); 
        } catch (SQLException e) {
            // Abaikan jika kolom tidak ditemukan (untuk kompatibilitas)
        }
        // ========================================================

        Timestamp tglApprovalInstansi = rs.getTimestamp("tgl_approval_instansi");
        if (tglApprovalInstansi != null) {
            borrow.setTglApprovalInstansi(tglApprovalInstansi.toLocalDateTime());
        }
        
        Timestamp tglApprovalPengembalian = rs.getTimestamp("tgl_approval_pengembalian");
        if (tglApprovalPengembalian != null) {
            borrow.setTglApprovalPengembalian(tglApprovalPengembalian.toLocalDateTime());
        }
        
        int instansiApprovalId = rs.getInt("id_instansi_approval");
        borrow.setIdInstansiApproval(rs.wasNull() ? null : instansiApprovalId);
        
        // Extended properties (Join results)
        try { borrow.setNamaPeminjam(rs.getString("nama_peminjam")); } catch (Exception e) {}
        try { borrow.setNoTelepon(rs.getString("no_telepon")); } catch (Exception e) {}
        try { borrow.setNamaBarang(rs.getString("nama_barang")); } catch (Exception e) {}
        try { borrow.setNamaInstansi(rs.getString("nama_instansi")); } catch (Exception e) {}
        
        try {
            int idInstansiBarang = rs.getInt("id_instansi_barang");
            borrow.setIdInstansiBarang(rs.wasNull() ? null : idInstansiBarang);
        } catch (Exception e) {}
        
        return borrow;
    }

    
    
    public Map<Integer, Map<String, Integer>> getStatistikPeminjamanPerInstansi() {
        Map<Integer, Map<String, Integer>> result = new HashMap<>();
        
        // Query untuk menghitung SEMUA peminjaman dari riwayat
        String sql = "SELECT " +
                     "br.id_instansi, " +
                     "i.nama_instansi, " +
                     "br.nama_barang, " +
                     "COUNT(*) as jumlah_kali_dipinjam, " +
                     "SUM(b.jumlah_pinjam) as total_barang_dipinjam " +
                     "FROM borrow b " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "LEFT JOIN instansi i ON br.id_instansi = i.id_instansi " +
                     "WHERE br.id_instansi IS NOT NULL " +
                     "GROUP BY br.id_instansi, br.nama_barang " +
                     "ORDER BY br.id_instansi, total_barang_dipinjam DESC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int idInstansi = rs.getInt("id_instansi");
                String namaBarang = rs.getString("nama_barang");
                int jumlahKaliDipinjam = rs.getInt("jumlah_kali_dipinjam");
                int totalBarangDipinjam = rs.getInt("total_barang_dipinjam");
                
                // Jika instansi belum ada di map, buat entry baru
                if (!result.containsKey(idInstansi)) {
                    result.put(idInstansi, new HashMap<>());
                }
                
                // Simpan total barang yang dipinjam (bukan jumlah transaksi)
                result.get(idInstansi).put(namaBarang, totalBarangDipinjam);
            }
            
            System.out.println("✅ Loaded stats for " + result.size() + " instansi");
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting statistik peminjaman: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Get detail statistik untuk satu instansi
     * Menghitung SEMUA riwayat peminjaman untuk instansi tertentu
     */
    public Map<String, Integer> getStatistikPeminjamanInstansi(Integer instansiId) {
        Map<String, Integer> result = new HashMap<>();
        
        String sql = "SELECT " +
                     "br.nama_barang, " +
                     "COUNT(*) as jumlah_kali_dipinjam, " +
                     "SUM(b.jumlah_pinjam) as total_barang_dipinjam " +
                     "FROM borrow b " +
                     "JOIN barang br ON b.kode_barang = br.kode_barang " +
                     "WHERE br.id_instansi = ? " +
                     "GROUP BY br.nama_barang " +
                     "ORDER BY total_barang_dipinjam DESC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, instansiId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String namaBarang = rs.getString("nama_barang");
                int totalBarangDipinjam = rs.getInt("total_barang_dipinjam");
                result.put(namaBarang, totalBarangDipinjam);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting statistik instansi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }


    
}



