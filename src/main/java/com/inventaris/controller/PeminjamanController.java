// ================================================================
// File: src/main/java/com/inventaris/controller/PeminjamanController.java
// ================================================================
package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.dao.BarangDAO;
import com.inventaris.dao.BorrowDAO;
import com.inventaris.model.Barang;
import com.inventaris.model.Borrow;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import com.inventaris.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.util.stream.Collectors;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * PeminjamanController - Handle borrowing and returning items
 */
public class PeminjamanController implements Initializable {
    
    // Form Fields untuk Peminjaman Baru
    @FXML private ComboBox<Barang> barangCombo;
    @FXML private TextField jumlahPinjamField;
    @FXML private DatePicker tglPinjamPicker;
    @FXML private DatePicker dlKembaliPicker;
    @FXML private Label stokTersediaLabel;
    @FXML private Button btnPinjam;
    
    // Tab Panes
    @FXML private TabPane tabPane;
    @FXML private Tab tabPengajuan;
    @FXML private Tab tabAktif;
    @FXML private Tab tabRiwayat;
    @FXML private Tab tabPending;
    
    // Table Peminjaman Aktif
    @FXML private TableView<Borrow> activeTable;
    @FXML private TableColumn<Borrow, Integer> colActiveId;
    @FXML private TableColumn<Borrow, String> colActivePeminjam;
    @FXML private TableColumn<Borrow, String> colActiveBarang;
    @FXML private TableColumn<Borrow, Integer> colActiveJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colActiveTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colActiveDeadline;
    @FXML private TableColumn<Borrow, Long> colActiveSisaHari;
    @FXML private TableColumn<Borrow, Void> colActiveAction;
    
    // Table Pending Approval
    @FXML private TableView<Borrow> pendingTable;
    @FXML private TableColumn<Borrow, Integer> colPendingId;
    @FXML private TableColumn<Borrow, String> colPendingPeminjam;
    @FXML private TableColumn<Borrow, String> colPendingBarang;
    @FXML private TableColumn<Borrow, Integer> colPendingJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colPendingTgl;
    @FXML private TableColumn<Borrow, Void> colPendingAction;
    
    // Table Riwayat
    @FXML private TableView<Borrow> historyTable;
    @FXML private TableColumn<Borrow, Integer> colHistoryId;
    @FXML private TableColumn<Borrow, String> colHistoryPeminjam;
    @FXML private TableColumn<Borrow, String> colHistoryBarang;
    @FXML private TableColumn<Borrow, LocalDate> colHistoryTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colHistoryTglKembali;
    @FXML private TableColumn<Borrow, String> colHistoryStatus;
    
    @FXML private Button btnDashboard;
    @FXML private Button btnBarang;
    @FXML private Button btnPeminjaman;
    @FXML private Button btnLaporan;
    private Button btnUser;
    private ScrollPane rootPane;
    @FXML private TextField txtSearch;
    
    private Timeline refreshTimeline;
    private final BarangDAO barangDAO = new BarangDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    @FXML
    private Label lblSimak;
    
    
    
    private void configureMenuByRole(String role) {
        switch (role) {
            case "admin":
                // Admin can access everything
                btnBarang.setVisible(true);
                btnPeminjaman.setVisible(true);
                btnLaporan.setVisible(true);
                btnUser.setVisible(true);
                break;
                
            case "peminjam":
                // Peminjam can only access their own borrowing
                btnBarang.setVisible(true);
                btnPeminjaman.setVisible(true);
                btnLaporan.setVisible(true);
                btnUser.setVisible(false);
              
                break;
                
            case "instansi":
                // Instansi similar to peminjam
                btnBarang.setVisible(true);
                btnPeminjaman.setVisible(true);
                btnLaporan.setVisible(true);
                btnUser.setVisible(false);
                break;
                
            default:
                btnBarang.setVisible(false);
                btnPeminjaman.setVisible(false);
                btnLaporan.setVisible(false);
                btnUser.setVisible(false);
           
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize date pickers
        tglPinjamPicker.setValue(LocalDate.now());
        dlKembaliPicker.setValue(LocalDate.now().plusDays(7)); // Default 7 hari
        
        // Load available barang
        loadAvailableBarang();
        
        // Setup barang combo listener
        barangCombo.setOnAction(e -> updateStokInfo());
        
        // Configure tables
        setupActiveTable();
        setupPendingTable();
        setupHistoryTable();
        
        // Load data
        loadAllData();
        
        // Hide pending tab if not admin
        if (!sessionManager.isAdmin()) {
            tabPane.getTabs().remove(tabPending);
        }
        // Hide tabs untuk instansi
        if (sessionManager.isInstansi()) {
            tabPane.getTabs().remove(tabPengajuan); // Ga bisa pinjam
            tabPane.getTabs().remove(tabPending);
        }

        // Start auto-refresh
        startAutoRefresh();
        System.out.println("✅ Peminjaman Controller initialized");
    }
    
    
    
    /**
 * Start auto-refresh
 */
private void startAutoRefresh() {
    refreshTimeline = new Timeline(new KeyFrame(
        Duration.seconds(3),
        event -> loadAllData()
    ));
    refreshTimeline.setCycleCount(Timeline.INDEFINITE);
    refreshTimeline.play();
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
    private void handleUser() {
      
        Main.loadContent("User.fxml");
    }
/**
 * 
 * Stop auto-refresh
 */
public void stopAutoRefresh() {
    if (refreshTimeline != null) {
        refreshTimeline.stop();
    }
}
    /**
     * Load available barang for borrowing
     */
    private void loadAvailableBarang() {
    List<Barang> availableBarang = barangDAO.getAvailable();
    
    // Jika instansi, jangan tampilkan barang sendiri
    if (sessionManager.isInstansi()) {
        Integer instansiId = sessionManager.getCurrentRoleId();
        availableBarang.removeIf(b -> 
            b.getIdInstansi() != null && 
            b.getIdInstansi().equals(instansiId)
        );
    }
    
    ObservableList<Barang> observableList = FXCollections.observableArrayList(availableBarang);
    barangCombo.setItems(observableList);
    
    // ... rest of cell factory code stays same
}
    
    /**
     * Update stock info when barang selected
     */
    private void updateStokInfo() {
        Barang selected = barangCombo.getValue();
        if (selected != null) {
            stokTersediaLabel.setText("Stok Tersedia: " + selected.getJumlahTersedia());
            if (selected.getJumlahTersedia() > 0) {
                stokTersediaLabel.setTextFill(Color.GREEN);
            } else {
                stokTersediaLabel.setTextFill(Color.RED);
            }
        } else {
            stokTersediaLabel.setText("Pilih barang terlebih dahulu");
            stokTersediaLabel.setTextFill(Color.GRAY);
        }
    }
    
    /**
     * Setup Active Borrowing Table
     */
    private void setupActiveTable() {
        colActiveId.setCellValueFactory(new PropertyValueFactory<>("idPeminjaman"));
        colActivePeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colActiveBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colActiveJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colActiveTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colActiveDeadline.setCellValueFactory(new PropertyValueFactory<>("dlKembali"));
        
        // Sisa hari dengan color
        colActiveSisaHari.setCellFactory(col -> new TableCell<Borrow, Long>() {
            @Override
            protected void updateItem(Long sisaHari, boolean empty) {
                super.updateItem(sisaHari, empty);
                if (empty || sisaHari == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    long days = borrow.getSisaHari();
                    setText(days + " hari");
                    
                    if (days < 0) {
                        setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #742a2a; -fx-font-weight: bold;");
                    } else if (days <= 2) {
                        setStyle("-fx-background-color: #feebc8; -fx-text-fill: #7c2d12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #c6f6d5; -fx-text-fill: #22543d;");
                    }
                }
            }
        });
        
        // Action buttons
        colActiveAction.setCellFactory(col -> new TableCell<Borrow, Void>() {
            private final Button btnKembalikan = new Button("✓ Kembalikan");
            
            {
                btnKembalikan.getStyleClass().add("btn-success");
                btnKembalikan.setOnAction(e -> {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    handleKembalikan(borrow);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    
                    // Only show return button for admin or owner
                    if (sessionManager.isAdmin() || 
                        (sessionManager.getCurrentRoleId() != null && 
                         sessionManager.getCurrentRoleId().equals(borrow.getIdPeminjam()))) {
                        setGraphic(btnKembalikan);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }
    
    /**
     * Setup Pending Table
     */
    private void setupPendingTable() {
        if (pendingTable == null) return;
        
        colPendingId.setCellValueFactory(new PropertyValueFactory<>("idPeminjaman"));
        colPendingPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colPendingBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colPendingJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colPendingTgl.setCellValueFactory(new PropertyValueFactory<>("tglPeminjaman"));
        
        // Action buttons (Admin only)
        colPendingAction.setCellFactory(col -> new TableCell<Borrow, Void>() {
            private final Button btnApprove = new Button("✓ Setujui");
            private final Button btnReject = new Button("✗ Tolak");
            private final HBox buttons = new HBox(5, btnApprove, btnReject);
            
            {
                btnApprove.getStyleClass().add("btn-success");
                btnReject.getStyleClass().add("btn-danger");
                
                btnApprove.setOnAction(e -> {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    handleApprove(borrow);
                });
                
                btnReject.setOnAction(e -> {
                    Borrow borrow = getTableView().getItems().get(getIndex());
                    handleReject(borrow);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }
    
    /**
     * Setup History Table
     */
    private void setupHistoryTable() {
        colHistoryId.setCellValueFactory(new PropertyValueFactory<>("idPeminjaman"));
        colHistoryPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colHistoryBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colHistoryTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colHistoryTglKembali.setCellValueFactory(new PropertyValueFactory<>("tglKembali"));
        colHistoryStatus.setCellValueFactory(new PropertyValueFactory<>("statusBarang"));
        
        // Status dengan color
        colHistoryStatus.setCellFactory(col -> new TableCell<Borrow, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "dikembalikan":
                            setStyle("-fx-background-color: #c6f6d5; -fx-text-fill: #22543d;");
                            break;
                        case "hilang":
                        case "rusak":
                            setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #742a2a;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }
    
    /**
     * Load all data
     */
    private void loadAllData() {
        loadActiveborrows();
        if (sessionManager.isAdmin()) {
            loadPendingBorrows();
        }
        loadHistory();
    }
    
    /**
     * Load active borrows
     */
    private void loadActiveborrows() {
    try {
        List<Borrow> borrows;

        if (sessionManager.isAdmin()) {
            // Admin: lihat semua peminjaman aktif
            borrows = borrowDAO.getActiveBorrows();
        } else if (sessionManager.isInstansi()) {
            // Instansi: peminjaman barang milik mereka
            Integer instansiId = sessionManager.getCurrentRoleId();
            List<Borrow> allBorrows = borrowDAO.getAll();
            borrows = allBorrows.stream()
                    .filter(b -> {
                        Barang barang = barangDAO.getByKode(b.getKodeBarang());
                        return barang != null &&
                               barang.getIdInstansi() != null &&
                               barang.getIdInstansi().equals(instansiId) &&
                               "dipinjam".equals(b.getStatusBarang());
                    })
                    .collect(Collectors.toList());
        } else {
            // Peminjam: hanya peminjaman mereka sendiri
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId != null) {
                borrows = borrowDAO.getByPeminjamId(peminjamId);
                borrows.removeIf(b -> !"dipinjam".equals(b.getStatusBarang()));
            } else {
                borrows = FXCollections.observableArrayList();
            }
        }

        ObservableList<Borrow> observableList = FXCollections.observableArrayList(borrows);
        activeTable.setItems(observableList);

    } catch (Exception e) {
        AlertUtil.showError("Error", "Gagal memuat data peminjaman aktif!");
        e.printStackTrace();
    }
}

    
    /**
     * Load pending borrows (Admin only)
     */
    private void loadPendingBorrows() {
        try {
            List<Borrow> borrows = borrowDAO.getPendingBorrows();
            ObservableList<Borrow> observableList = FXCollections.observableArrayList(borrows);
            pendingTable.setItems(observableList);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data pending!");
            e.printStackTrace();
        }
    }
    
    /**
     * Load history
     */
    private void loadHistory() {
        try {
            List<Borrow> borrows;
            
            if (sessionManager.isAdmin()) {
                borrows = borrowDAO.getAll();
            } else {
                Integer peminjamId = sessionManager.getCurrentRoleId();
                if (peminjamId != null) {
                    borrows = borrowDAO.getByPeminjamId(peminjamId);
                } else {
                    borrows = FXCollections.observableArrayList();
                }
            }
            
            // Filter only returned/lost/damaged
            borrows.removeIf(b -> "dipinjam".equals(b.getStatusBarang()) || "pending".equals(b.getStatusBarang()));
            
            ObservableList<Borrow> observableList = FXCollections.observableArrayList(borrows);
            historyTable.setItems(observableList);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat riwayat!");
            e.printStackTrace();
        }
    }
    
    /**
     * Handle pinjam button
     */
    @FXML
    private void handlePinjam() {
        if (!validatePinjamInput()) {
            return;
        }
        
        try {
            Barang barang = barangCombo.getValue();
            int jumlah = Integer.parseInt(jumlahPinjamField.getText());
            
            // Check stock
            if (jumlah > barang.getJumlahTersedia()) {
                AlertUtil.showWarning("Stok Tidak Cukup", 
                    "Jumlah yang diminta melebihi stok tersedia!\nTersedia: " + barang.getJumlahTersedia());
                return;
            }
            
            if (!AlertUtil.showConfirmation("Konfirmasi", 
                "Ajukan peminjaman " + jumlah + " unit " + barang.getNamaBarang() + "?")) {
                return;
            }
            
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) {
                AlertUtil.showError("Error", "Session tidak valid!");
                return;
            }
            
            Borrow borrow = new Borrow();
            borrow.setIdPeminjam(peminjamId);
            borrow.setKodeBarang(barang.getKodeBarang());
            borrow.setJumlahPinjam(jumlah);
            borrow.setTglPeminjaman(LocalDate.now());
            borrow.setTglPinjam(tglPinjamPicker.getValue());
            borrow.setDlKembali(dlKembaliPicker.getValue());
            borrow.setStatusBarang("pending");
            
            if (borrowDAO.create(borrow)) {
                AlertUtil.showSuccess("Berhasil", 
                    "Pengajuan peminjaman berhasil!\nMenunggu persetujuan admin.");
                
                // Log activity
                LogActivityUtil.logCreate(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "peminjaman",
                    barang.getNamaBarang() + " - " + jumlah + " unit"
                );
                
                clearPinjamForm();
                loadAllData();
                loadAvailableBarang();
                
            } else {
                AlertUtil.showError("Gagal", "Gagal mengajukan peminjaman!");
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle approve
     */
    private void handleApprove(Borrow borrow) {
        if (!AlertUtil.showConfirmation("Konfirmasi", "Setujui peminjaman ini?")) {
            return;
        }
        
        Integer adminId = sessionManager.getCurrentRoleId();
        if (adminId == null) {
            AlertUtil.showError("Error", "Session tidak valid!");
            return;
        }
        
        if (borrowDAO.approve(borrow.getIdPeminjaman(), adminId)) {
            AlertUtil.showSuccess("Berhasil", "Peminjaman disetujui!");
            
            LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                "Menyetujui peminjaman ID: " + borrow.getIdPeminjaman(),
                "APPROVE_PEMINJAMAN",
                sessionManager.getCurrentRole()
            );
            
            loadAllData();
        } else {
            AlertUtil.showError("Gagal", "Gagal menyetujui peminjaman!");
        }
    }
    
    /**
     * Handle reject
     */
    private void handleReject(Borrow borrow) {
        if (!AlertUtil.showConfirmation("Konfirmasi", "Tolak peminjaman ini?\nStok akan dikembalikan.")) {
            return;
        }
        
        if (borrowDAO.reject(borrow.getIdPeminjaman())) {
            AlertUtil.showSuccess("Berhasil", "Peminjaman ditolak!");
            
            LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                "Menolak peminjaman ID: " + borrow.getIdPeminjaman(),
                "REJECT_PEMINJAMAN",
                sessionManager.getCurrentRole()
            );
            
            loadAllData();
            loadAvailableBarang();
        } else {
            AlertUtil.showError("Gagal", "Gagal menolak peminjaman!");
        }
    }
    
    /**
     * Handle kembalikan
     */
    private void handleKembalikan(Borrow borrow) {
        // Create return dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Kembalikan Barang");
        dialog.setHeaderText("Kembalikan: " + borrow.getNamaBarang());
        
        ComboBox<String> kondisiCombo = new ComboBox<>();
        kondisiCombo.getItems().addAll("baik", "rusak ringan", "rusak berat", "hilang");
        kondisiCombo.setValue("baik");
        
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
            new Label("Kondisi Barang:"),
            kondisiCombo
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String kondisi = kondisiCombo.getValue();
                
                if (borrowDAO.returnItem(borrow.getIdPeminjaman(), kondisi, null)) {
                    AlertUtil.showSuccess("Berhasil", "Barang berhasil dikembalikan!");
                    
                    LogActivityUtil.log(
                        sessionManager.getCurrentUsername(),
                        "Mengembalikan barang: " + borrow.getNamaBarang(),
                        "RETURN_BARANG",
                        sessionManager.getCurrentRole()
                    );
                    
                    loadAllData();
                    loadAvailableBarang();
                } else {
                    AlertUtil.showError("Gagal", "Gagal mengembalikan barang!");
                }
            }
        });
    }
    
    /**
     * Validate pinjam input
     */
    private boolean validatePinjamInput() {
        if (barangCombo.getValue() == null) {
            AlertUtil.showWarning("Validasi", "Pilih barang yang akan dipinjam!");
            return false;
        }
        
        if (!ValidationUtil.isPositiveNumber(jumlahPinjamField.getText())) {
            AlertUtil.showWarning("Validasi", "Jumlah pinjam harus berupa angka positif!");
            jumlahPinjamField.requestFocus();
            return false;
        }
        
        if (tglPinjamPicker.getValue() == null) {
            AlertUtil.showWarning("Validasi", "Pilih tanggal pinjam!");
            return false;
        }
        
        if (dlKembaliPicker.getValue() == null) {
            AlertUtil.showWarning("Validasi", "Pilih deadline kembali!");
            return false;
        }
        
        if (!ValidationUtil.isValidDateRange(tglPinjamPicker.getValue(), dlKembaliPicker.getValue())) {
            AlertUtil.showWarning("Validasi", "Deadline harus setelah tanggal pinjam!");
            return false;
        }
        
        return true;
    }
    
    /**
     * Clear pinjam form
     */
    private void clearPinjamForm() {
        barangCombo.setValue(null);
        jumlahPinjamField.clear();
        tglPinjamPicker.setValue(LocalDate.now());
        dlKembaliPicker.setValue(LocalDate.now().plusDays(7));
        stokTersediaLabel.setText("Pilih barang terlebih dahulu");
        stokTersediaLabel.setTextFill(Color.GRAY);
    }
    
    /**
     * Refresh data
     */
    @FXML
    private void handleRefresh() {
        loadAllData();
        loadAvailableBarang();
        AlertUtil.showInfo("Refresh", "Data berhasil diperbarui!");
    }
    // search
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
}