package com.inventaris.controller;

import com.inventaris.dao.LaporDAO;
import com.inventaris.model.Lapor;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * LaporanAdminController - Kelola semua laporan (Admin Only)
 */
public class LaporanAdminController implements Initializable {

    // ============================================================
    // FXML COMPONENTS
    // ============================================================
    
    @FXML private ComboBox<String> filterStatusLapor;
    
    @FXML private TableView<Lapor> allLaporTable;
    @FXML private TableColumn<Lapor, String> colLaporNo;
    @FXML private TableColumn<Lapor, String> colLaporPeminjam;
    @FXML private TableColumn<Lapor, String> colLaporBarang;
    @FXML private TableColumn<Lapor, String> colLaporInstansi;      // ✅ BARU
    @FXML private TableColumn<Lapor, String> colLaporKeterangan;    // ✅ BARU
    @FXML private TableColumn<Lapor, LocalDate> colLaporTgl;
    @FXML private TableColumn<Lapor, String> colLaporStatus;
    @FXML private TableColumn<Lapor, Void> colLaporAction;
    
    // ============================================================
    // DEPENDENCIES
    // ============================================================
    
    private final LaporDAO laporDAO = new LaporDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    // ============================================================
    // INITIALIZE
    // ============================================================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilterStatus();
        setupTableColumns();
        loadAllLaporan();
        
        System.out.println("✅ LaporanAdminController initialized");
    }
    
    // ============================================================
    // SETUP METHODS
    // ============================================================
    
    /**
     * Setup filter status combobox
     */
    private void setupFilterStatus() {
        ObservableList<String> statusList = FXCollections.observableArrayList(
            "Semua Status", "diproses", "selesai", "ditolak"
        );
        filterStatusLapor.setItems(statusList);
        filterStatusLapor.setValue("Semua Status");
        
        // Auto-refresh on filter change
        filterStatusLapor.setOnAction(e -> loadAllLaporan());
    }
    
    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        colLaporNo.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNoLaporan()));
        
        colLaporPeminjam.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNamaPeminjam()));
        
        colLaporBarang.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNamaBarang()));
        
        // ✅ KOLOM INSTANSI - Parse dari keterangan
        colLaporInstansi.setCellValueFactory(cellData -> {
            String keterangan = cellData.getValue().getKeterangan();
            String instansi = parseInstansi(keterangan);
            return new SimpleStringProperty(instansi);
        });
        
        // ✅ KOLOM KETERANGAN - Parse dari keterangan
        colLaporKeterangan.setCellValueFactory(cellData -> {
            String keterangan = cellData.getValue().getKeterangan();
            String laporan = parseLaporan(keterangan);
            return new SimpleStringProperty(laporan);
        });
        
        colLaporTgl.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getTglLaporan()));
        
        colLaporStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Status dengan warna
        colLaporStatus.setCellFactory(col -> new TableCell<Lapor, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toUpperCase());
                    switch (status.toLowerCase()) {
                        case "diproses":
                            setStyle("-fx-background-color: #feebc8; -fx-text-fill: #7c2d12; -fx-font-weight: bold;");
                            break;
                        case "selesai":
                            setStyle("-fx-background-color: #c6f6d5; -fx-text-fill: #22543d; -fx-font-weight: bold;");
                            break;
                        case "ditolak":
                            setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #742a2a; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        // Action buttons (Selesai / Tolak)
        colLaporAction.setCellFactory(col -> new TableCell<Lapor, Void>() {
            private final Button btnSelesai = new Button("✓ Selesai");
            private final Button btnTolak = new Button("✗ Tolak");
            private final HBox buttons = new HBox(5, btnSelesai, btnTolak);
            
            {
                btnSelesai.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnTolak.setStyle("-fx-background-color: #f56565; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                
                btnSelesai.setOnAction(e -> {
                    Lapor lapor = getTableView().getItems().get(getIndex());
                    handleProsesLaporan(lapor, "selesai");
                });
                
                btnTolak.setOnAction(e -> {
                    Lapor lapor = getTableView().getItems().get(getIndex());
                    handleProsesLaporan(lapor, "ditolak");
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Lapor lapor = getTableView().getItems().get(getIndex());
                    // Hanya tampilkan tombol jika status "diproses"
                    if ("diproses".equalsIgnoreCase(lapor.getStatus())) {
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }
    
    // ============================================================
    // HELPER METHODS - PARSING
    // ============================================================
    
    /**
     * Parse Instansi dari format: [INSTANSI: nama_instansi]
     */
    private String parseInstansi(String keterangan) {
        if (keterangan != null && keterangan.startsWith("[INSTANSI:")) {
            try {
                int start = keterangan.indexOf("[INSTANSI:") + 10;
                int end = keterangan.indexOf("]");
                if (end > start) {
                    return keterangan.substring(start, end).trim();
                }
            } catch (Exception e) {
                return "-";
            }
        }
        return "-";
    }
    
    /**
     * Parse Laporan dari format: LAPORAN: isi_laporan
     */
    private String parseLaporan(String keterangan) {
        if (keterangan != null && keterangan.contains("LAPORAN:")) {
            try {
                int start = keterangan.indexOf("LAPORAN:") + 8;
                return keterangan.substring(start).trim();
            } catch (Exception e) {
                return keterangan;
            }
        }
        return keterangan != null ? keterangan : "-";
    }
    
    // ============================================================
    // DATA LOADING
    // ============================================================
    
    /**
     * Load all laporan (dengan filter status)
     */
    private void loadAllLaporan() {
        try {
            List<Lapor> laporList = laporDAO.getAll();
            
            // Filter berdasarkan status yang dipilih
            String selectedStatus = filterStatusLapor.getValue();
            if (selectedStatus != null && !"Semua Status".equals(selectedStatus)) {
                laporList.removeIf(l -> !selectedStatus.equalsIgnoreCase(l.getStatus()));
            }
            
            ObservableList<Lapor> observableList = FXCollections.observableArrayList(laporList);
            allLaporTable.setItems(observableList);
            
            System.out.println("✅ Loaded " + laporList.size() + " laporan");
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data laporan!");
            e.printStackTrace();
        }
    }
    
    // ============================================================
    // EVENT HANDLERS
    // ============================================================
    
    /**
     * Handle proses laporan (selesai/ditolak)
     */
    private void handleProsesLaporan(Lapor lapor, String status) {
        String action = "selesai".equals(status) ? "menyelesaikan" : "menolak";
        
        if (!AlertUtil.showConfirmation("Konfirmasi", 
            "Yakin " + action + " laporan ini?\n\n" +
            "No. Laporan: " + lapor.getNoLaporan() + "\n" +
            "Peminjam: " + lapor.getNamaPeminjam() + "\n" +
            "Barang: " + lapor.getNamaBarang())) {
            return;
        }
        
        if (laporDAO.updateStatus(lapor.getIdLaporan(), status)) {
            AlertUtil.showSuccess("Berhasil", "Status laporan berhasil diupdate!");
            
            // Log activity
            com.inventaris.util.LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                action + " laporan: " + lapor.getNoLaporan(),
                "UPDATE_LAPORAN",
                sessionManager.getCurrentRole()
            );
            
            loadAllLaporan(); // Refresh
            
        } else {
            AlertUtil.showError("Gagal", "Gagal mengupdate status laporan!");
        }
    }
    
    /**
     * Handle refresh button
     */
    @FXML
    private void handleRefresh() {
        loadAllLaporan();
        AlertUtil.showInfo("Refresh", "Data berhasil diperbarui!");
    }
}
