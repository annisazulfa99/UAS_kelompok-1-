// ================================================================
// File: src/main/java/com/inventaris/dao/BeritaDAO.java
// ================================================================
package com.inventaris.dao;

import com.inventaris.config.DatabaseConfig;
import com.inventaris.model.Berita;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BeritaDAO - Data Access Object for Berita
 * Handles all database operations related to Berita entity
 */
public class BeritaDAO {
    
    private final DatabaseConfig dbConfig;
    
    public BeritaDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Get 3 berita terbaru untuk dashboard
     */
    public List<Berita> getLatestBerita(int limit) {
        List<Berita> beritaList = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_admin " +
                     "FROM berita b " +
                     "LEFT JOIN user u ON b.created_by = u.id_user " +
                     "ORDER BY b.created_at DESC LIMIT ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                beritaList.add(extractBeritaFromResultSet(rs));
            }
            
            System.out.println("✅ Latest berita loaded: " + beritaList.size() + " items");
            
        } catch (SQLException e) {
            System.err.println("Error getting latest berita: " + e.getMessage());
            e.printStackTrace();
        }
        
        return beritaList;
    }
    
    /**
     * Get all berita (untuk halaman kelola admin)
     */
    public List<Berita> getAllBerita() {
        List<Berita> beritaList = new ArrayList<>();
        String sql = "SELECT b.*, u.nama as nama_admin " +
                     "FROM berita b " +
                     "LEFT JOIN user u ON b.created_by = u.id_user " +
                     "ORDER BY b.created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                beritaList.add(extractBeritaFromResultSet(rs));
            }
            
            System.out.println("✅ All berita loaded: " + beritaList.size() + " items");
            
        } catch (SQLException e) {
            System.err.println("Error getting all berita: " + e.getMessage());
            e.printStackTrace();
        }
        
        return beritaList;
    }
    
    /**
     * Get berita by ID
     */
    public Berita getBeritaById(int idBerita) {
        String sql = "SELECT b.*, u.nama as nama_admin " +
                     "FROM berita b " +
                     "LEFT JOIN user u ON b.created_by = u.id_user " +
                     "WHERE b.id_berita = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idBerita);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractBeritaFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting berita by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Add new berita
     */
    public boolean addBerita(Berita berita) {
        String sql = "INSERT INTO berita (judul, deskripsi, warna_background, created_by) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, berita.getJudul());
            stmt.setString(2, berita.getDeskripsi());
            stmt.setString(3, berita.getWarnaBackground());
            stmt.setInt(4, berita.getCreatedBy());
            
            boolean success = stmt.executeUpdate() > 0;
            
            if (success) {
                System.out.println("✅ Berita added: " + berita.getJudul());
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error adding berita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update berita
     */
    public boolean updateBerita(Berita berita) {
        String sql = "UPDATE berita SET judul = ?, deskripsi = ?, warna_background = ? " +
                     "WHERE id_berita = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, berita.getJudul());
            stmt.setString(2, berita.getDeskripsi());
            stmt.setString(3, berita.getWarnaBackground());
            stmt.setInt(4, berita.getIdBerita());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating berita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete berita
     */
    public boolean deleteBerita(int idBerita) {
        String sql = "DELETE FROM berita WHERE id_berita = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idBerita);
            boolean success = stmt.executeUpdate() > 0;
            
            if (success) {
                System.out.println("✅ Berita deleted: ID " + idBerita);
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("Error deleting berita: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get count of berita
     */
    public int getBeritaCount() {
        String sql = "SELECT COUNT(*) FROM berita";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting berita count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Extract User object from ResultSet
     * FIXED: Tidak lagi mengambil updated_at
     */
    private Berita extractBeritaFromResultSet(ResultSet rs) throws SQLException {
        Berita berita = new Berita();
        berita.setIdBerita(rs.getInt("id_berita"));
        berita.setJudul(rs.getString("judul"));
        berita.setDeskripsi(rs.getString("deskripsi"));
        berita.setWarnaBackground(rs.getString("warna_background"));
        berita.setCreatedBy(rs.getInt("created_by"));
        berita.setCreatedAt(rs.getTimestamp("created_at"));
        
        // updated_at dihapus karena kolom tidak ada di database
        // berita.setUpdatedAt(rs.getTimestamp("updated_at")); // ❌ DIHAPUS
        
        // Nama admin (dari JOIN)
        try {
            berita.setNamaAdmin(rs.getString("nama_admin"));
        } catch (SQLException e) {
            // Kolom nama_admin mungkin tidak ada, skip
        }
        
        return berita;
    }
}