// ================================================================
// File: src/main/java/com/inventaris/dao/LaporDAO.java
// ================================================================
package com.inventaris.dao;

import com.inventaris.config.DatabaseConfig;
import com.inventaris.model.Lapor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * LaporDAO - Data Access Object for Lapor
 * Handles all database operations related to Lapor entity
 */
public class LaporDAO {
    
    private final DatabaseConfig dbConfig;
    
    /**
     * Constructor
     */
    public LaporDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Create laporan baru
     * 
     * @param lapor Lapor object
     * @return true if successful, false otherwise
     */
   /**
     * Create laporan baru
     */
    public boolean create(Lapor lapor) {
        // Tambahkan kolom 'keterangan'
        String sql = "INSERT INTO lapor (no_laporan, id_peminjaman, kode_barang, tgl_laporan, status, keterangan) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
                     
        // --- PERBAIKAN DI BARIS INI (Gunakan dbConfig) ---
        try (Connection conn = dbConfig.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, lapor.getNoLaporan());
            ps.setInt(2, lapor.getIdPeminjaman());
            ps.setString(3, lapor.getKodeBarang());
            ps.setDate(4, java.sql.Date.valueOf(lapor.getTglLaporan()));
            ps.setString(5, lapor.getStatus());
            
            // Set Keterangan
            ps.setString(6, lapor.getKeterangan()); 

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Get laporan by ID
     * 
     * @param id ID laporan
     * @return Lapor object if found, null otherwise
     */
    public Lapor getById(int id) {
        String sql = "SELECT l.*, u.nama as nama_peminjam, br.nama_barang " +
                     "FROM lapor l " +
                     "JOIN borrow b ON l.id_peminjaman = b.id_peminjaman " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON l.kode_barang = br.kode_barang " +
                     "WHERE l.id_laporan = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractLaporFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting laporan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get laporan by nomor laporan
     * 
     * @param noLaporan Nomor laporan
     * @return Lapor object if found, null otherwise
     */
    public Lapor getByNoLaporan(String noLaporan) {
        String sql = "SELECT l.*, u.nama as nama_peminjam, br.nama_barang " +
                     "FROM lapor l " +
                     "JOIN borrow b ON l.id_peminjaman = b.id_peminjaman " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON l.kode_barang = br.kode_barang " +
                     "WHERE l.no_laporan = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, noLaporan);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractLaporFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting laporan by no: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all laporan
     * 
     * @return List of all laporan
     */
    public List<Lapor> getAll() {
        List<Lapor> list = new ArrayList<>();
        String sql = "SELECT l.*, u.nama as nama_peminjam, br.nama_barang " +
                     "FROM lapor l " +
                     "JOIN borrow b ON l.id_peminjaman = b.id_peminjaman " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON l.kode_barang = br.kode_barang " +
                     "ORDER BY l.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractLaporFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all laporan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get laporan by status
     * 
     * @param status Status laporan (diproses, selesai, ditolak)
     * @return List of laporan with specified status
     */
    public List<Lapor> getByStatus(String status) {
        List<Lapor> list = new ArrayList<>();
        String sql = "SELECT l.*, u.nama as nama_peminjam, br.nama_barang " +
                     "FROM lapor l " +
                     "JOIN borrow b ON l.id_peminjaman = b.id_peminjaman " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON l.kode_barang = br.kode_barang " +
                     "WHERE l.status = ? " +
                     "ORDER BY l.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                list.add(extractLaporFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting laporan by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get laporan by peminjaman ID
     * 
     * @param peminjamanId ID peminjaman
     * @return List of laporan for specified peminjaman
     */
    public List<Lapor> getByPeminjamanId(int peminjamanId) {
        List<Lapor> list = new ArrayList<>();
        String sql = "SELECT l.*, u.nama as nama_peminjam, br.nama_barang " +
                     "FROM lapor l " +
                     "JOIN borrow b ON l.id_peminjaman = b.id_peminjaman " +
                     "JOIN peminjam p ON b.id_peminjam = p.id_peminjam " +
                     "JOIN user u ON p.id_user = u.id_user " +
                     "JOIN barang br ON l.kode_barang = br.kode_barang " +
                     "WHERE l.id_peminjaman = ? " +
                     "ORDER BY l.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, peminjamanId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                list.add(extractLaporFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting laporan by peminjaman: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Update status laporan
     * 
     * @param id ID laporan
     * @param status New status
     * @return true if successful, false otherwise
     */
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE lapor SET status = ? WHERE id_laporan = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating laporan status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete laporan
     * 
     * @param id ID laporan
     * @return true if successful, false otherwise
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM lapor WHERE id_laporan = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting laporan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate nomor laporan
     * Format: LAP-00001, LAP-00002, etc.
     * 
     * @return Generated nomor laporan
     */
    public String generateNoLaporan() {
        String sql = "SELECT COUNT(*) as total FROM lapor";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int total = rs.getInt("total") + 1;
                return String.format("LAP-%05d", total);
            }
            
        } catch (SQLException e) {
            System.err.println("Error generating no laporan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "LAP-00001";
    }
    
    /**
     * Get total laporan count
     * 
     * @return Total count of laporan
     */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM lapor";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get count by status
     * 
     * @param status Status to count
     * @return Count of laporan with specified status
     */
    public int getCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM lapor WHERE status = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting count by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Check if peminjaman already has laporan
     * 
     * @param peminjamanId ID peminjaman
     * @return true if already has laporan, false otherwise
     */
    public boolean hasLaporan(int peminjamanId) {
        String sql = "SELECT COUNT(*) FROM lapor WHERE id_peminjaman = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, peminjamanId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking laporan: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Extract Lapor object from ResultSet
     * 
     * @param rs ResultSet
     * @return Lapor object
     * @throws SQLException if error occurs
     */
    /**
     * Helper untuk memetakan data dari ResultSet ke Object Lapor
     */
    private Lapor extractLaporFromResultSet(ResultSet rs) throws SQLException {
        Lapor lapor = new Lapor();
        lapor.setIdLaporan(rs.getInt("id_laporan"));
        lapor.setNoLaporan(rs.getString("no_laporan"));
        lapor.setIdPeminjaman(rs.getInt("id_peminjaman"));
        lapor.setKodeBarang(rs.getString("kode_barang"));
        lapor.setStatus(rs.getString("status"));
        lapor.setTglLaporan(rs.getDate("tgl_laporan").toLocalDate());
        lapor.setCreatedAt(rs.getTimestamp("created_at"));
        
        // --- [TAMBAHAN WAJIB] ---
        // Tanpa baris ini, keterangan tidak akan terbaca dari database!
        lapor.setKeterangan(rs.getString("keterangan")); 
        // ------------------------
        
        // Extended properties
        lapor.setNamaPeminjam(rs.getString("nama_peminjam"));
        lapor.setNamaBarang(rs.getString("nama_barang"));
        
        return lapor;
    }
}