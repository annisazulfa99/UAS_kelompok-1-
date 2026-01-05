// ================================================================
// File: src/main/java/com/inventaris/dao/BarangDAO.java
// UPDATED: Sesuai dengan database Anda + foto_url support
// ================================================================
package com.inventaris.dao;

import com.inventaris.config.DatabaseConfig;
import com.inventaris.model.Barang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * BarangDAO - Updated untuk support foto_url
 */
public class BarangDAO {
    
    private final DatabaseConfig dbConfig;
    
    public BarangDAO() {
        this.dbConfig = DatabaseConfig.getInstance();
    }
    
    /**
     * Create new barang
     */
    public boolean create(Barang barang) {
        String sql = "INSERT INTO barang (id_instansi, kode_barang, nama_barang, lokasi_barang, " +
                     "jumlah_total, jumlah_tersedia, deskripsi, kondisi_barang, status, foto, foto_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, barang.getIdInstansi()); // Support NULL
            stmt.setString(2, barang.getKodeBarang());
            stmt.setString(3, barang.getNamaBarang());
            stmt.setString(4, barang.getLokasiBarang());
            stmt.setInt(5, barang.getJumlahTotal());
            stmt.setInt(6, barang.getJumlahTersedia());
            stmt.setString(7, barang.getDeskripsi());
            stmt.setString(8, barang.getKondisiBarang());
            stmt.setString(9, barang.getStatus());
            stmt.setString(10, barang.getFoto());
            stmt.setString(11, barang.getFotoUrl());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating barang: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get barang by kode
     */
    public Barang getByKode(String kode) {
        String sql = "SELECT * FROM barang WHERE kode_barang = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, kode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractBarangFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting barang: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all barang dengan JOIN ke instansi (untuk nama pemilik)
     */
    public List<Barang> getAll() {
        List<Barang> list = new ArrayList<>();
        
        // Pakai VIEW yang sudah ada di database Anda
        String sql = "SELECT * FROM v_barang_with_owner ORDER BY created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Barang barang = extractBarangFromResultSet(rs);
                
                // Set nama pemilik dari VIEW
                barang.setNamaPemilik(rs.getString("nama_pemilik"));
                
                list.add(barang);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all barang: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get available barang (stok > 0 and status tersedia)
     * DENGAN JOIN untuk nama instansi
     */
    public List<Barang> getAvailable() {
        List<Barang> list = new ArrayList<>();
        
        String sql = "SELECT b.*, i.nama_instansi as nama_pemilik " +
                     "FROM barang b " +
                     "LEFT JOIN instansi i ON b.id_instansi = i.id_instansi " +
                     "WHERE b.jumlah_tersedia > 0 AND b.status = 'tersedia' " +
                     "ORDER BY b.nama_barang";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Barang barang = extractBarangFromResultSet(rs);
                barang.setNamaPemilik(rs.getString("nama_pemilik"));
                list.add(barang);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting available barang: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get barang by instansi
     */
    public List<Barang> getByInstansi(Integer instansiId) {
        List<Barang> list = new ArrayList<>();
        
        String sql = "SELECT b.*, i.nama_instansi as nama_pemilik " +
                     "FROM barang b " +
                     "LEFT JOIN instansi i ON b.id_instansi = i.id_instansi " +
                     "WHERE b.id_instansi = ? " +
                     "ORDER BY b.nama_barang";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, instansiId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Barang barang = extractBarangFromResultSet(rs);
                barang.setNamaPemilik(rs.getString("nama_pemilik"));
                list.add(barang);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting barang by instansi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Update barang
     */
    public boolean update(Barang barang) {
        String sql = "UPDATE barang SET " +
                     "nama_barang = ?, lokasi_barang = ?, jumlah_total = ?, " +
                     "jumlah_tersedia = ?, deskripsi = ?, kondisi_barang = ?, " +
                     "status = ?, foto = ?, foto_url = ?, id_instansi = ? " +
                     "WHERE kode_barang = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, barang.getNamaBarang());
            stmt.setString(2, barang.getLokasiBarang());
            stmt.setInt(3, barang.getJumlahTotal());
            stmt.setInt(4, barang.getJumlahTersedia());
            stmt.setString(5, barang.getDeskripsi());
            stmt.setString(6, barang.getKondisiBarang());
            stmt.setString(7, barang.getStatus());
            stmt.setString(8, barang.getFoto());
            stmt.setString(9, barang.getFotoUrl());
            stmt.setObject(10, barang.getIdInstansi()); // Support NULL
            stmt.setString(11, barang.getKodeBarang());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating barang: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update jumlah tersedia
     */
    public boolean updateJumlahTersedia(String kodeBarang, int jumlah, boolean isAdd) {
        String sql;
        if (isAdd) {
            sql = "UPDATE barang SET jumlah_tersedia = jumlah_tersedia + ? WHERE kode_barang = ?";
        } else {
            sql = "UPDATE barang SET jumlah_tersedia = jumlah_tersedia - ? WHERE kode_barang = ?";
        }
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, jumlah);
            stmt.setString(2, kodeBarang);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating jumlah tersedia: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete barang
     */
    public boolean delete(String kode) {
        String sql = "DELETE FROM barang WHERE kode_barang = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, kode);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting barang: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Search barang by keyword (dengan JOIN untuk nama pemilik)
     */
    public List<Barang> search(String keyword) {
        List<Barang> list = new ArrayList<>();
        
        String sql = "SELECT b.*, i.nama_instansi as nama_pemilik " +
                     "FROM barang b " +
                     "LEFT JOIN instansi i ON b.id_instansi = i.id_instansi " +
                     "WHERE b.kode_barang LIKE ? OR b.nama_barang LIKE ? OR b.lokasi_barang LIKE ? " +
                     "ORDER BY b.nama_barang";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Barang barang = extractBarangFromResultSet(rs);
                barang.setNamaPemilik(rs.getString("nama_pemilik"));
                list.add(barang);
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching barang: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Check if kode barang exists
     */
    public boolean kodeExists(String kodeBarang) {
        String sql = "SELECT COUNT(*) FROM barang WHERE kode_barang = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, kodeBarang);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking kode barang: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get total barang count
     */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM barang";
        
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
     * Get barang by status
     */
    public List<Barang> getByStatus(String status) {
        List<Barang> list = new ArrayList<>();
        
        String sql = "SELECT b.*, i.nama_instansi as nama_pemilik " +
                     "FROM barang b " +
                     "LEFT JOIN instansi i ON b.id_instansi = i.id_instansi " +
                     "WHERE b.status = ? " +
                     "ORDER BY b.nama_barang";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Barang barang = extractBarangFromResultSet(rs);
                barang.setNamaPemilik(rs.getString("nama_pemilik"));
                list.add(barang);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting barang by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    /**
     * Get barang with low stock (< 5)
     */
    public List<Barang> getLowStock() {
        List<Barang> list = new ArrayList<>();
        
        String sql = "SELECT b.*, i.nama_instansi as nama_pemilik " +
                     "FROM barang b " +
                     "LEFT JOIN instansi i ON b.id_instansi = i.id_instansi " +
                     "WHERE b.jumlah_tersedia < 5 AND b.jumlah_tersedia > 0 " +
                     "ORDER BY b.jumlah_tersedia";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Barang barang = extractBarangFromResultSet(rs);
                barang.setNamaPemilik(rs.getString("nama_pemilik"));
                list.add(barang);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting low stock barang: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Extract Barang object from ResultSet
     * UPDATED: Support foto_url
     */
private Barang extractBarangFromResultSet(ResultSet rs) throws SQLException {
    Barang barang = new Barang();
    
    // ID
    barang.setIdBarang(rs.getInt("id_barang"));
    
    // Handle NULL id_instansi
    int idInstansi = rs.getInt("id_instansi");
    barang.setIdInstansi(rs.wasNull() ? null : idInstansi);
    
    // Data utama
    barang.setKodeBarang(rs.getString("kode_barang"));
    barang.setNamaBarang(rs.getString("nama_barang"));
    barang.setLokasiBarang(rs.getString("lokasi_barang"));
    barang.setJumlahTotal(rs.getInt("jumlah_total"));
    barang.setJumlahTersedia(rs.getInt("jumlah_tersedia"));
    barang.setDeskripsi(rs.getString("deskripsi"));
    barang.setKondisiBarang(rs.getString("kondisi_barang"));
    barang.setStatus(rs.getString("status"));
    barang.setFoto(rs.getString("foto"));
    
    // Foto URL (opsional)
    try {
        barang.setFotoUrl(rs.getString("foto_url"));
    } catch (SQLException e) {
        barang.setFotoUrl(null);
    }
    
    // Timestamp
    barang.setCreatedAt(rs.getTimestamp("created_at"));
    barang.setUpdatedAt(rs.getTimestamp("updated_at"));
    
    return barang;
}

}