// ================================================================
// File: src/main/java/com/inventaris/controller/LaporanController.java
// ================================================================
package com.inventaris.controller;

import com.inventaris.Main;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;



/**
 * LaporanController - Handle reporting and statistics
 */
public class LaporanController implements Initializable {
    
    // Form Laporan Baru
    @FXML private ComboBox<Borrow> peminjamanCombo;
    @FXML private TextArea keteranganArea;
    @FXML private Button btnLapor;
    
    // Table Laporan
    @FXML private TableView<Lapor> laporTable;
    @FXML private TableColumn<Lapor, String> colNoLaporan;
    @FXML private TableColumn<Lapor, String> colPeminjam;
    @FXML private TableColumn<Lapor, String> colBarang;
    @FXML private TableColumn<Lapor, LocalDate> colTglLaporan;
    @FXML private TableColumn<Lapor, String> colStatus;
    @FXML private TableColumn<Lapor, Void> colAction;
    
    // Statistics Labels
    @FXML private Label totalPeminjamanLabel;
    @FXML private Label aktifLabel;
    @FXML private Label selesaiLabel;
    @FXML private Label terlambatLabel;
    @FXML private Label totalLaporanLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnBarang;
    @FXML private TextField txtSearch;
    @FXML private Button btnPeminjaman;
    @FXML private Button btnLaporan;
    @FXML private Button btnUser;
    
    @FXML
private ScrollPane rootPane;



    
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final LaporDAO laporDAO = new LaporDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    
    
@FXML
private void handleSearch() {
    try {
        String keyword = txtSearch.getText().trim();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Barang.fxml"));
        Parent barangPage = loader.load();

        // Ambil controller DataBarang
        BarangController controller = loader.getController();

        // Kirim keyword ke method pencarian
        controller.searchBarang(keyword);

        // Ganti konten ScrollPane dengan halaman DataBarang
        rootPane.setContent(barangPage);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
private void configureMenuByRole(String role) {
    switch (role) {
        case "admin":
            btnBarang.setVisible(true);
            btnPeminjaman.setVisible(true);
            btnLaporan.setVisible(true);
            btnUser.setVisible(true);
            btnUser.setManaged(true); // ✅ TAMBAH BARIS INI
            break;
            
        case "peminjam":
            btnBarang.setVisible(true);
            btnPeminjaman.setVisible(true);
            btnLaporan.setVisible(true);
            btnUser.setVisible(false);
            btnUser.setManaged(false); // ✅ TAMBAH BARIS INI
            break;
            
        case "instansi":
            btnBarang.setVisible(true);
            btnPeminjaman.setVisible(true);
            btnLaporan.setVisible(true);
            btnUser.setVisible(false);
            btnUser.setManaged(false); // ✅ TAMBAH BARIS INI
            break;
            
        default:
            btnBarang.setVisible(false);
            btnPeminjaman.setVisible(false);
            btnLaporan.setVisible(false);
            btnUser.setVisible(false);
            btnUser.setManaged(false); // ✅ TAMBAH BARIS INI
    }
}
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureMenuByRole(sessionManager.getCurrentRole());
        // Setup table
        setupLaporTable();
        
        // Load user's active borrows for reporting
        loadUserBorrows();
        
        // Load laporan
        loadLaporan();
        
        // Load statistics
        loadStatistics();
        
        System.out.println("✅ Laporan Controller initialized");
    }
    
    /**
     * Setup laporan table
     */
    private void setupLaporTable() {
        colNoLaporan.setCellValueFactory(new PropertyValueFactory<>("noLaporan"));
        colPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colTglLaporan.setCellValueFactory(new PropertyValueFactory<>("tglLaporan"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Status with color
        colStatus.setCellFactory(col -> new TableCell<Lapor, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "diproses":
                            setStyle("-fx-background-color: #feebc8; -fx-text-fill: #7c2d12;");
                            break;
                        case "selesai":
                            setStyle("-fx-background-color: #c6f6d5; -fx-text-fill: #22543d;");
                            break;
                        case "ditolak":
                            setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #742a2a;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
            
    
        
        // Action buttons (Admin only)
        if (sessionManager.isAdmin()) {
            colAction.setCellFactory(col -> new TableCell<Lapor, Void>() {
                private final Button btnProses = new Button("✓ Selesai");
                private final Button btnTolak = new Button("✗ Tolak");
                private final HBox buttons = new HBox(5, btnProses, btnTolak);
                
                {
                    btnProses.getStyleClass().add("btn-success");
                    btnTolak.getStyleClass().add("btn-danger");
                    
                    btnProses.setOnAction(e -> {
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
                        if ("diproses".equals(lapor.getStatus())) {
                            setGraphic(buttons);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
        }
    }
    
    @FXML
    private void handleUser() {
      
        Main.loadContent("User.fxml");
    }
    
    @FXML
    private void handleBarang() {
      
        Main.loadContent("DataBarang.fxml");
    }
    
    @FXML
    private void handlePeminjaman() {
        System.out.println("➡️ Tombol Peminjaman ditekan");
        Main.loadContent("Peminjaman.fxml");
    }

    
    @FXML
    private void handleLaporan() {
        
        Main.loadContent("Laporan.fxml");
    }
    
    @FXML
    private void handleHome(){
       Main.loadContent("Home.fxml");
    }
    
    @FXML
    private void handleDashboard(){
        Main.showDashboard();
    }
    /**
     * Load user's active borrows
     */
    private void loadUserBorrows() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) {
                return;
            }
            
            List<Borrow> borrows = borrowDAO.getByPeminjamId(peminjamId);
            borrows.removeIf(b -> !"dipinjam".equals(b.getStatusBarang()));
            
            ObservableList<Borrow> observableList = FXCollections.observableArrayList(borrows);
            peminjamanCombo.setItems(observableList);
            
            // Custom cell factory
            peminjamanCombo.setCellFactory(lv -> new ListCell<Borrow>() {
                @Override
                protected void updateItem(Borrow item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("ID:" + item.getIdPeminjaman() + " - " + item.getNamaBarang() + 
                               " (Pinjam: " + item.getTglPinjam() + ")");
                    }
                }
            });
            
            peminjamanCombo.setButtonCell(new ListCell<Borrow>() {
                @Override
                protected void updateItem(Borrow item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("ID:" + item.getIdPeminjaman() + " - " + item.getNamaBarang());
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error loading user borrows: " + e.getMessage());
        }
    }
    
    /**
     * Load laporan
     */
    private void loadLaporan() {
        try {
            List<Lapor> laporList = laporDAO.getAll();
            
            // Filter by user if not admin
            if (!sessionManager.isAdmin()) {
                String username = sessionManager.getCurrentUsername();
                laporList.removeIf(l -> !username.equals(l.getNamaPeminjam()));
            }
            // Filter untuk instansi
if (sessionManager.isInstansi()) {
    Integer instansiId = sessionManager.getCurrentRoleId();
    laporList.removeIf(l -> {
        com.inventaris.model.Barang barang = 
            new com.inventaris.dao.BarangDAO().getByKode(l.getKodeBarang());
        return barang == null || 
               barang.getIdInstansi() == null || 
               !barang.getIdInstansi().equals(instansiId);
    });
}
            ObservableList<Lapor> observableList = FXCollections.observableArrayList(laporList);
            laporTable.setItems(observableList);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data laporan!");
            e.printStackTrace();
        }
    }
    
    /**
     * Load statistics
     */
    private void loadStatistics() {
        try {
            List<Borrow> allBorrows;
            
            if (sessionManager.isAdmin()) {
                allBorrows = borrowDAO.getAll();
            } else {
                Integer peminjamId = sessionManager.getCurrentRoleId();
                if (peminjamId != null) {
                    allBorrows = borrowDAO.getByPeminjamId(peminjamId);
                } else {
                    allBorrows = FXCollections.observableArrayList();
                }
            }
            
            int total = allBorrows.size();
            long aktif = allBorrows.stream().filter(b -> "dipinjam".equals(b.getStatusBarang())).count();
            long selesai = allBorrows.stream().filter(b -> "dikembalikan".equals(b.getStatusBarang())).count();
            
            List<Borrow> overdue = borrowDAO.getOverdueBorrows();
            if (!sessionManager.isAdmin()) {
                Integer peminjamId = sessionManager.getCurrentRoleId();
                final Integer finalPeminjamId = peminjamId;
                overdue.removeIf(b -> !b.getIdPeminjam().equals(finalPeminjamId));
            }
            int terlambat = overdue.size();
            
            List<Lapor> laporan = laporDAO.getAll();
            if (!sessionManager.isAdmin()) {
                String username = sessionManager.getCurrentUsername();
                laporan.removeIf(l -> !username.equals(l.getNamaPeminjam()));
            }
            int totalLaporan = laporan.size();
            
            // Update labels
            if (totalPeminjamanLabel != null) totalPeminjamanLabel.setText(String.valueOf(total));
            if (aktifLabel != null) aktifLabel.setText(String.valueOf(aktif));
            if (selesaiLabel != null) selesaiLabel.setText(String.valueOf(selesai));
            if (terlambatLabel != null) terlambatLabel.setText(String.valueOf(terlambat));
            if (totalLaporanLabel != null) totalLaporanLabel.setText(String.valueOf(totalLaporan));
            
        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }
    
    /**
     * Handle submit laporan
     */
    @FXML
    private void handleLapor() {
        if (peminjamanCombo.getValue() == null) {
            AlertUtil.showWarning("Validasi", "Pilih peminjaman yang akan dilaporkan!");
            return;
        }
        
        if (keteranganArea.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validasi", "Keterangan laporan tidak boleh kosong!");
            keteranganArea.requestFocus();
            return;
        }
        
        if (!AlertUtil.showConfirmation("Konfirmasi", "Kirim laporan ini?")) {
            return;
        }
        
        try {
            Borrow borrow = peminjamanCombo.getValue();
            
            // Generate nomor laporan
            String noLaporan = laporDAO.generateNoLaporan();
            
            Lapor lapor = new Lapor();
            lapor.setNoLaporan(noLaporan);
            lapor.setIdPeminjaman(borrow.getIdPeminjaman());
            lapor.setKodeBarang(borrow.getKodeBarang());
            lapor.setStatus("diproses");
            lapor.setTglLaporan(LocalDate.now());
            
            if (laporDAO.create(lapor)) {
                AlertUtil.showSuccess("Berhasil", 
                    "Laporan berhasil dikirim!\nNo. Laporan: " + noLaporan);
                
                // Log activity
                LogActivityUtil.logCreate(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "laporan",
                    noLaporan + " - " + borrow.getNamaBarang()
                );
                
                clearForm();
                loadLaporan();
                loadStatistics();
                
            } else {
                AlertUtil.showError("Gagal", "Gagal mengirim laporan!");
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle proses laporan (Admin)
     */
    private void handleProsesLaporan(Lapor lapor, String status) {
        String action = "selesai".equals(status) ? "menyelesaikan" : "menolak";
        
        if (!AlertUtil.showConfirmation("Konfirmasi", "Yakin " + action + " laporan ini?")) {
            return;
        }
        
        if (laporDAO.updateStatus(lapor.getIdLaporan(), status)) {
            AlertUtil.showSuccess("Berhasil", "Status laporan berhasil diupdate!");
            
            LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                action + " laporan: " + lapor.getNoLaporan(),
                "UPDATE_LAPORAN",
                sessionManager.getCurrentRole()
            );
            
            loadLaporan();
            loadStatistics();
            
        } else {
            AlertUtil.showError("Gagal", "Gagal mengupdate status laporan!");
        }
    }
    
    /**
     * Clear form
     */
    private void clearForm() {
        peminjamanCombo.setValue(null);
        keteranganArea.clear();
    }
    
    /**
     * Refresh data
     */
    @FXML
    private void handleRefresh() {
        loadUserBorrows();
        loadLaporan();
        loadStatistics();
        AlertUtil.showInfo("Refresh", "Data berhasil diperbarui!");
    }
    
    /**
     * Export to CSV (simplified version)
     */
    @FXML
    private void handleExport() {
        AlertUtil.showInfo("Export", 
            "Fitur export akan tersedia di versi mendatang.\n" +
            "Data dapat di-copy dari tabel dan paste ke Excel.");
    }
}