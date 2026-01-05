package com.inventaris.controller;

import com.inventaris.dao.BarangDAO;
import com.inventaris.dao.BorrowDAO;
import com.inventaris.dao.LaporDAO;
import com.inventaris.model.Borrow;
import com.inventaris.model.Lapor;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class LaporanPeminjamController implements Initializable {

    // --- UI COMPONENTS ---
    @FXML private BorderPane rootPane;
    
    // View Containers
    @FXML private VBox viewPeminjam; // Form Input untuk Peminjam
    @FXML private VBox viewAdmin;    // Tabel Monitor untuk Admin

    // Form Components (Peminjam)
    @FXML private ComboBox<Borrow> peminjamanCombo;
    @FXML private TextArea txtInstansi;    // Input manual Instansi
    @FXML private TextArea keteranganArea; // Input keterangan kerusakan
    @FXML private Button btnLapor;

    // --- DATA TOOLS ---
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final LaporDAO laporDAO = new LaporDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String role = sessionManager.getCurrentRole();

        // 1. Logika Tampilan Berdasarkan Role
        if ("admin".equals(role)) {
            if (viewAdmin != null) {
                viewAdmin.setVisible(true);
                viewAdmin.setManaged(true);
            }
            if (viewPeminjam != null) {
                viewPeminjam.setVisible(false);
                viewPeminjam.setManaged(false);
            }
        } else {
            // Tampilan untuk Peminjam
            if (viewAdmin != null) {
                viewAdmin.setVisible(false);
                viewAdmin.setManaged(false);
            }
            if (viewPeminjam != null) {
                viewPeminjam.setVisible(true);
                viewPeminjam.setManaged(true);
            }

            // Load barang yang sedang dipinjam user
            loadUserBorrows();
        }
        
        System.out.println("✅ Laporan Controller initialized for: " + role);
    }

    /**
     * Mengambil daftar barang yang statusnya 'dipinjam' oleh user ini
     */
    private void loadUserBorrows() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) return;

            List<Borrow> borrows = borrowDAO.getByPeminjamId(peminjamId);
            borrows.removeIf(b -> !"dipinjam".equals(b.getStatusBarang()));

            ObservableList<Borrow> list = FXCollections.observableArrayList(borrows);
            peminjamanCombo.setItems(list);

            peminjamanCombo.setButtonCell(new BorrowListCell());
            peminjamanCombo.setCellFactory(lv -> new BorrowListCell());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handler Tombol Kirim Laporan
     */
    @FXML
    private void handleLapor() {
        // 1. Validasi Input
        if (peminjamanCombo.getValue() == null) {
            AlertUtil.showWarning("Validasi", "Pilih barang yang ingin dilaporkan!");
            return;
        }
        if (txtInstansi.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validasi", "Nama Instansi / Pemilik tidak boleh kosong.");
            return;
        }
        if (keteranganArea.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validasi", "Mohon isi keterangan laporan.");
            return;
        }

        if (!AlertUtil.showConfirmation("Konfirmasi", "Kirim laporan ini?")) return;

        try {
            Borrow borrow = peminjamanCombo.getValue();
            String noLaporan = laporDAO.generateNoLaporan();

            Lapor lapor = new Lapor();
            lapor.setNoLaporan(noLaporan);
            lapor.setIdPeminjaman(borrow.getIdPeminjaman());
            lapor.setKodeBarang(borrow.getKodeBarang());
            lapor.setStatus("diproses");
            lapor.setTglLaporan(LocalDate.now());

            // --- TRIK PENGGABUNGAN (Tanpa Ubah Database/Model) ---
            // Kita gabungkan input Instansi ke dalam field Keterangan
            String gabungKeterangan = "[INSTANSI: " + txtInstansi.getText().trim() + "]\n" +
                                      "LAPORAN: " + keteranganArea.getText().trim();
            
            lapor.setKeterangan(gabungKeterangan); 
            // -----------------------------------------------------

            if (laporDAO.create(lapor)) {
                AlertUtil.showSuccess("Sukses", "Laporan berhasil dikirim!");
                LogActivityUtil.logCreate(sessionManager.getCurrentUsername(), sessionManager.getCurrentRole(), "Laporan", noLaporan);
                
                // Reset Form
                peminjamanCombo.setValue(null);
                txtInstansi.clear();
                keteranganArea.clear();
            } else {
                AlertUtil.showError("Gagal", "Gagal menyimpan laporan ke database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Terjadi kesalahan sistem.");
        }
    }

    /**
     * Helper tampilan ComboBox
     */
    private static class BorrowListCell extends ListCell<Borrow> {
        @Override
        protected void updateItem(Borrow item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getNamaBarang() + " (" + item.getKodeBarang() + ")");
            }
        }
    }
}