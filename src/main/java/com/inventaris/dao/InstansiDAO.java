package com.inventaris.dao;

import com.inventaris.config.DatabaseConfig;
import com.inventaris.model.Instansi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstansiDAO {

    private final DatabaseConfig db = DatabaseConfig.getInstance();

    // Ambil semua instansi
    public List<Instansi> getAll() {
        List<Instansi> list = new ArrayList<>();
        String sql = "SELECT * FROM instansi ORDER BY nama_instansi";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapResult(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Ambil instansi berdasarkan kategori (LEMBAGA, BEM, HIMPUNAN, UKM)
    public List<String> getByKategori(String kategori) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT nama_instansi FROM instansi WHERE kategori = ? ORDER BY nama_instansi";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, kategori);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("nama_instansi"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Ambil ID instansi dari nama
    public int getIdByNama(String namaInstansi) {
        String sql = "SELECT id_instansi FROM instansi WHERE nama_instansi = ? LIMIT 1";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, namaInstansi);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_instansi");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Tidak ditemukan
    }

    // Ambil nama instansi dari ID
    public String getNamaById(int idInstansi) {
        String sql = "SELECT nama_instansi FROM instansi WHERE id_instansi = ? LIMIT 1";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idInstansi);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nama_instansi");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Convert dari ResultSet ke model Instansi
    private Instansi mapResult(ResultSet rs) throws Exception {
        Instansi i = new Instansi();
        i.setIdInstansi(rs.getInt("id_instansi"));
        i.setIdUser(rs.getInt("id_user"));
        i.setNamaInstansi(rs.getString("nama_instansi"));
        i.setKategori(rs.getString("kategori"));
        return i;
    }
}
