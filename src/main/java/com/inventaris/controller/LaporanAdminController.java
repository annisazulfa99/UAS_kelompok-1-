package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.dao.LaporDAO;
import com.inventaris.model.Lapor;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller Khusus Admin: Mengelola Laporan Masuk
 */
public class LaporanAdminController implements Initializable {

    // --- UI Components ---
    @FXML private TableView<Lapor> allLaporTable;
    @FXML private TableColumn<Lapor, String> colLaporNo;
    @FXML private TableColumn<Lapor, String> colLaporPeminjam;
    @FXML private TableColumn<Lapor, String> colLaporBarang;
    @FXML private TableColumn<Lapor, LocalDate> colLaporTgl;
    @FXML private TableColumn<Lapor, String> colLaporStatus;
    @FXML private TableColumn<Lapor, Void> colLaporAction;
    
    @FXML private ComboBox<String> filterStatusLapor;
    @FXML private TextField txtSearch;

    // --- Buttons Navigasi ---
    @FXML private Button btnDashboard, btnPeminjaman, btnLaporan, btnBarang, btnUser;

    // --- Tools ---
    private final LaporDAO laporDAO = new LaporDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Security Check
        if (!sessionManager.isAdmin()) {
            AlertUtil.showError("Access Denied", "Hanya admin yang dapat mengakses halaman ini!");
            Main.showLoginScreen();
            return;
        }

        setupTable();
        setupFilter();
        loadAllLaporan();
        
        System.out.println("✅ Laporan Admin Controller Initialized");
    }

    // ============================================================
    // LOGIKA TABEL & DATA
    // ============================================================

    private void setupTable() {
        // 1. Setting Kolom Data Biasa
        colLaporNo.setCellValueFactory(new PropertyValueFactory<>("noLaporan"));
        colLaporPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colLaporBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colLaporTgl.setCellValueFactory(new PropertyValueFactory<>("tglLaporan"));
        colLaporStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Setting Warna Warni Status
        colLaporStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { 
                    setText(null); setStyle(""); 
                } else {
                    setText(status.toUpperCase());
                    switch (status.toLowerCase()) {
                        case "diproses": setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;"); break;
                        case "selesai":  setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;"); break;
                        case "ditolak":  setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;"); break;
                        default: setStyle("");
                    }
                }
            }
        });

        colLaporAction.setCellFactory(col -> new TableCell<>() {
    // Tombol Detail (Warna Biru)
    private final Button btnDetail = new Button("👁️");
    // Tombol Selesai (Warna Hijau)
    private final Button btnSelesai = new Button("✅");
    // Tombol Tolak (Warna Merah)
    private final Button btnTolak = new Button("❌");

    // Container Tombol
    private final HBox pane = new HBox(10, btnDetail, btnSelesai, btnTolak);

    {
        // Styling
        btnDetail.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSelesai.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-cursor: hand;");
        btnTolak.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
        
        // Setup Aksi
        btnDetail.setOnAction(e -> {
            Lapor lapor = getTableView().getItems().get(getIndex());
            showDetailLaporan(lapor);
        });
        btnSelesai.setOnAction(e -> processLapor(getTableView().getItems().get(getIndex()), "selesai"));
        btnTolak.setOnAction(e -> processLapor(getTableView().getItems().get(getIndex()), "ditolak"));
        
        // Debugging: Set background pane kuning biar kelihatan kalau pane ini ada tapi kepotong
        pane.setStyle("-fx-background-color: transparent; -fx-alignment: center-left;"); 
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            Lapor lapor = getTableView().getItems().get(getIndex());
            
            // Logic Tampilan
            if ("diproses".equalsIgnoreCase(lapor.getStatus())) {
                // Tampilkan SEMUA tombol (Detail, Selesai, Tolak)
                pane.getChildren().setAll(btnDetail, btnSelesai, btnTolak);
                setGraphic(pane);
            } else {
                // Tampilkan HANYA tombol Detail
                pane.getChildren().setAll(btnDetail);
                setGraphic(pane);
            }
        }
    }
});}
    

    
    /**
     * Menampilkan Pop-up Detail Laporan
     */
    private void showDetailLaporan(Lapor lapor) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail Laporan");
        alert.setHeaderText("Laporan No: " + lapor.getNoLaporan());
        
        // Ambil data keterangan (Pastikan model Lapor punya getter ini)
        String deskripsi = lapor.getKeterangan();
        if (deskripsi == null || deskripsi.isEmpty()) {
            deskripsi = "(Tidak ada keterangan dari pelapor)";
        }

        // Format Tampilan
        String content = 
                "Pelapor   : " + lapor.getNamaPeminjam() + "\n" +
                "Barang    : " + lapor.getNamaBarang() + "\n" +
                "Tanggal   : " + lapor.getTglLaporan() + "\n" +
                "Status    : " + lapor.getStatus().toUpperCase() + "\n\n" +
                "===== KETERANGAN / KELUHAN =====" + "\n" + 
                deskripsi;

        // Pakai TextArea agar bisa di-scroll jika teks panjang
        TextArea area = new TextArea(content);
        area.setEditable(false);
        area.setWrapText(true);
        area.setMaxWidth(Double.MAX_VALUE);
        area.setMaxHeight(Double.MAX_VALUE);
        area.setPrefRowCount(10); // Tinggi area teks

        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }
    
    
    private void loadAllLaporan() {
        try {
            List<Lapor> list = laporDAO.getAll();

            // Filter Dropdown
            if (filterStatusLapor != null && filterStatusLapor.getValue() != null && 
                !"Semua".equals(filterStatusLapor.getValue())) {
                String filter = filterStatusLapor.getValue().toLowerCase();
                list.removeIf(l -> !l.getStatus().equalsIgnoreCase(filter));
            }

            // Filter Search Text
            if (txtSearch != null && !txtSearch.getText().isEmpty()) {
                String keyword = txtSearch.getText().toLowerCase();
                list.removeIf(l -> 
                    !l.getNoLaporan().toLowerCase().contains(keyword) &&
                    !l.getNamaPeminjam().toLowerCase().contains(keyword) &&
                    !l.getNamaBarang().toLowerCase().contains(keyword)
                );
            }

            allLaporTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data laporan: " + e.getMessage());
        }
    }

    private void processLapor(Lapor lapor, String newStatus) {
        String actionName = "selesai".equals(newStatus) ? "Menyelesaikan" : "Menolak";
        
        if (!AlertUtil.showConfirmation("Konfirmasi", "Yakin ingin " + actionName + " laporan ini?")) {
            return;
        }

        if (laporDAO.updateStatus(lapor.getIdLaporan(), newStatus)) {
            AlertUtil.showSuccess("Sukses", "Laporan berhasil diupdate menjadi: " + newStatus);
            
            LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                actionName + " Laporan: " + lapor.getNoLaporan(),
                "PROCESS_LAPORAN",
                sessionManager.getCurrentRole()
            );
            
            loadAllLaporan(); // Refresh table
        } else {
            AlertUtil.showError("Gagal", "Gagal mengupdate status database.");
        }
    }

    private void setupFilter() {
        filterStatusLapor.setItems(FXCollections.observableArrayList(
            "Semua", "Diproses", "Selesai", "Ditolak"
        ));
        filterStatusLapor.setValue("Semua");
        filterStatusLapor.setOnAction(e -> loadAllLaporan());
    }

    // ============================================================
    // NAVIGASI
    // ============================================================

    
}