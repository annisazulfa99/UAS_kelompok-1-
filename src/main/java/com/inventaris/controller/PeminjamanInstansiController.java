// ================================================================
// File: src/main/java/com/inventaris/controller/PeminjamanInstansiController.java
// ================================================================
package com.inventaris.controller;

import com.inventaris.dao.BorrowDAO;
import com.inventaris.model.Borrow;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.inventaris.dao.BarangDAO;   // Pastikan path package sesuai
import com.inventaris.model.Barang;
/**
 * PeminjamanInstansiController
 * Handle approval workflow untuk Instansi dengan Nomor Urut Otomatis
 */
public class PeminjamanInstansiController implements Initializable {
    
    // --- TAB NAVIGATION ---
    @FXML private ToggleButton tabPengajuan;
    @FXML private ToggleButton tabSedang;
    @FXML private ToggleButton tabPengembalian;
    @FXML private ToggleButton tabRiwayat;
    
    @FXML private VBox contentPengajuan;
    @FXML private VBox contentSedang;
    @FXML private VBox contentPengembalian;
    @FXML private VBox contentRiwayat;
    
    // --- TAB 1: PENGAJUAN MASUK (pending_instansi) ---
    @FXML private TableView<Borrow> tablePengajuan;
    @FXML private TableColumn<Borrow, Integer> colPengajuanId; // Ini akan jadi No Urut
    @FXML private TableColumn<Borrow, String> colPengajuanPeminjam;
    @FXML private TableColumn<Borrow, String> colPengajuanBarang;
    @FXML private TableColumn<Borrow, Integer> colPengajuanJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colPengajuanTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colPengajuanDeadline;
    @FXML private TableColumn<Borrow, Void> colPengajuanAction;
    
    // --- TAB 2: SEDANG DIPINJAM (approved_instansi) ---
    @FXML private TableView<Borrow> tableSedang;
    @FXML private TableColumn<Borrow, Integer> colSedangId; // Ini akan jadi No Urut
    @FXML private TableColumn<Borrow, String> colSedangPeminjam;
    @FXML private TableColumn<Borrow, String> colSedangBarang;
    @FXML private TableColumn<Borrow, Integer> colSedangJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colSedangTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colSedangDeadline;
    @FXML private TableColumn<Borrow, Long> colSedangSisa;
    
    // --- TAB 3: PENGAJUAN PENGEMBALIAN (pending_return) ---
    @FXML private TableView<Borrow> tablePengembalian;
    @FXML private TableColumn<Borrow, Integer> colPengembalianId; // Ini akan jadi No Urut
    @FXML private TableColumn<Borrow, String> colPengembalianPeminjam;
    @FXML private TableColumn<Borrow, String> colPengembalianBarang;
    @FXML private TableColumn<Borrow, Integer> colPengembalianBaik;
    @FXML private TableColumn<Borrow, Integer> colPengembalianRusak;
    @FXML private TableColumn<Borrow, Integer> colPengembalianHilang;
    @FXML private TableColumn<Borrow, String> colPengembalianCatatan;
    @FXML private TableColumn<Borrow, Void> colPengembalianAction;
    
    // --- TAB 4: RIWAYAT ---
    @FXML private TableView<Borrow> tableRiwayat;
    @FXML private TableColumn<Borrow, Integer> colRiwayatId; // Ini akan jadi No Urut
    @FXML private TableColumn<Borrow, String> colRiwayatPeminjam;
    @FXML private TableColumn<Borrow, String> colRiwayatBarang;
    @FXML private TableColumn<Borrow, Integer> colRiwayatJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colRiwayatTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colRiwayatTglKembali;
    @FXML private TableColumn<Borrow, String> colRiwayatStatus;
    
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔧 PeminjamanInstansiController initializing...");
        
        // Setup tables
        setupPengajuanTable();
        setupSedangTable();
        setupPengembalianTable();
        setupRiwayatTable();
        
        // Load data
        loadAllData();
        
        // Show default tab
        showPengajuanTab();
        
        System.out.println("✅ PeminjamanInstansiController initialized");
    }
    
    // ============================================================
    // TAB NAVIGATION
    // ============================================================
    
    @FXML
    private void handleTabChange() {
        if (tabPengajuan.isSelected()) {
            showPengajuanTab();
        } else if (tabSedang.isSelected()) {
            showSedangTab();
        } else if (tabPengembalian.isSelected()) {
            showPengembalianTab();
        } else if (tabRiwayat.isSelected()) {
            showRiwayatTab();
        }
    }
    
    private void showPengajuanTab() {
        setVisibleTab(contentPengajuan, tabPengajuan);
        loadPengajuanData();
    }
    
    private void showSedangTab() {
        setVisibleTab(contentSedang, tabSedang);
        loadSedangData();
    }
    
    private void showPengembalianTab() {
        setVisibleTab(contentPengembalian, tabPengembalian);
        loadPengembalianData();
    }
    
    private void showRiwayatTab() {
        setVisibleTab(contentRiwayat, tabRiwayat);
        loadRiwayatData();
    }
    
    private void setVisibleTab(VBox content, ToggleButton tab) {
        // Hide all
        contentPengajuan.setVisible(false); contentPengajuan.setManaged(false);
        contentSedang.setVisible(false); contentSedang.setManaged(false);
        contentPengembalian.setVisible(false); contentPengembalian.setManaged(false);
        contentRiwayat.setVisible(false); contentRiwayat.setManaged(false);
        
        // Reset styles
        String inactiveStyle = "-fx-background-color: #D9CBC1; -fx-text-fill: black; -fx-cursor: hand;";
        tabPengajuan.setStyle(inactiveStyle);
        tabSedang.setStyle(inactiveStyle);
        tabPengembalian.setStyle(inactiveStyle);
        tabRiwayat.setStyle(inactiveStyle);
        
        // Show selected
        if (content != null) { content.setVisible(true); content.setManaged(true); }
        if (tab != null) { 
            tab.setStyle("-fx-background-color: #8C6E63; -fx-text-fill: white; -fx-font-weight: bold;"); 
        }
    }
    
    // ============================================================
    // TABLE SETUP
    // ============================================================
    
   private void setupPengajuanTable() {
        // --- UBAH DISINI: Pakai helper nomNomor ---
        setKolomNomor(colPengajuanId); 
        // ----------------------------------------------
        
        colPengajuanPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colPengajuanBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colPengajuanJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colPengajuanTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colPengajuanDeadline.setCellValueFactory(new PropertyValueFactory<>("dlKembali"));
        
        colPengajuanAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnApprove = new Button("✓");
            private final Button btnReject = new Button("✗");
            private final Button btnSurat = new Button("📄");
            private final HBox buttons = new HBox(5, btnSurat, btnApprove, btnReject);
            
            {
                btnApprove.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                btnReject.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                btnSurat.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btnSurat.setTooltip(new Tooltip("Lihat Lampiran/Surat"));

                btnApprove.setOnAction(e -> handleApprovePengajuan(getTableView().getItems().get(getIndex())));
                btnReject.setOnAction(e -> handleRejectPengajuan(getTableView().getItems().get(getIndex())));
                
                btnSurat.setOnAction(e -> {
                    Borrow b = getTableView().getItems().get(getIndex());
                    showSuratDialog(b);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void showSuratDialog(Borrow borrow) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Lampiran Pengajuan");
        alert.setHeaderText("Pesan dari: " + borrow.getNamaPeminjam());
        
        String isiPesan = borrow.getKeperluan(); 
        if (isiPesan == null || isiPesan.trim().isEmpty()) {
            isiPesan = "(Tidak ada catatan/lampiran surat)";
        }
        
        TextArea textArea = new TextArea(isiPesan);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        VBox content = new VBox(10);
        content.getChildren().add(new Label("Isi Lampiran/Surat:"));
        content.getChildren().add(textArea);
        
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private void setupSedangTable() {
        // --- UBAH DISINI: Pakai helper setKolomNomor ---
        setKolomNomor(colSedangId);
        // ----------------------------------------------
        
        colSedangPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colSedangBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colSedangJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colSedangTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colSedangDeadline.setCellValueFactory(new PropertyValueFactory<>("dlKembali"));
        
        colSedangSisa.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSisaHari())
        );
        
        colSedangSisa.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long sisaHari, boolean empty) {
                super.updateItem(sisaHari, empty);
                if (empty || sisaHari == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(sisaHari + " hari");
                    if (sisaHari < 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (sisaHari <= 2) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
    }
    
    private void setupPengembalianTable() {
        // --- UBAH DISINI: Pakai helper setKolomNomor ---
        setKolomNomor(colPengembalianId);
        // ----------------------------------------------
        
        colPengembalianPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colPengembalianBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colPengembalianBaik.setCellValueFactory(new PropertyValueFactory<>("jumlahBaik"));
        colPengembalianRusak.setCellValueFactory(new PropertyValueFactory<>("jumlahRusak"));
        colPengembalianHilang.setCellValueFactory(new PropertyValueFactory<>("jumlahHilang"));
        colPengembalianCatatan.setCellValueFactory(new PropertyValueFactory<>("catatanPengembalian"));
        
        colPengembalianRusak.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item > 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        colPengembalianHilang.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item > 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        colPengembalianAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnApprove = new Button("✓ Terima");
            private final Button btnReject = new Button("✗ Tolak");
            private final HBox buttons = new HBox(5, btnApprove, btnReject);
            
            {
                btnApprove.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
                btnReject.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
                
                btnApprove.setOnAction(e -> handleApprovePengembalian(getTableView().getItems().get(getIndex())));
                btnReject.setOnAction(e -> handleRejectPengembalian(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
                setAlignment(Pos.CENTER);
            }
        });
    }
    
    private void setupRiwayatTable() {
        // --- UBAH DISINI: Pakai helper setKolomNomor ---
        setKolomNomor(colRiwayatId);
        // ----------------------------------------------
        
        colRiwayatPeminjam.setCellValueFactory(new PropertyValueFactory<>("namaPeminjam"));
        colRiwayatBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colRiwayatJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colRiwayatTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colRiwayatTglKembali.setCellValueFactory(new PropertyValueFactory<>("tglKembali"));
        colRiwayatStatus.setCellValueFactory(new PropertyValueFactory<>("statusApproval"));
        
        colRiwayatStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Borrow b = getTableView().getItems().get(getIndex());
                    setText(b.getStatusText());
                    
                    if ("approved_return".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if ("rejected_instansi".equalsIgnoreCase(status) || "rejected_return".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: orange;");
                    }
                }
            }
        });
    }
    
    // ============================================================
    // DATA LOADING
    // ============================================================
    
    private void loadAllData() {
        loadPengajuanData();
        loadSedangData();
        loadPengembalianData();
        loadRiwayatData();
    }
    
    private void loadPengajuanData() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            if (instansiId == null) {
                System.err.println("❌ Instansi ID is NULL!");
                return;
            }
            
            List<Borrow> borrows = borrowDAO.getPendingInstansiApproval(instansiId);
            tablePengajuan.setItems(FXCollections.observableArrayList(borrows));
            
        } catch (Exception e) {
            System.err.println("❌ Error loading pengajuan data!");
            e.printStackTrace();
        }
    }
    
    private void loadSedangData() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            if (instansiId == null) return;
            
            List<Borrow> borrows = borrowDAO.getByInstansiBarang(instansiId);
            tableSedang.setItems(FXCollections.observableArrayList(borrows));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadPengembalianData() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            if (instansiId == null) return;
            
            List<Borrow> borrows = borrowDAO.getPendingReturnApproval(instansiId);
            tablePengembalian.setItems(FXCollections.observableArrayList(borrows));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadRiwayatData() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            if (instansiId == null) return;
            
            List<Borrow> allBorrows = borrowDAO.getAll();
            allBorrows.removeIf(b -> 
                b.getIdInstansiBarang() == null ||
                !b.getIdInstansiBarang().equals(instansiId) ||
                ("pending_instansi".equals(b.getStatusApproval()) || 
                 "approved_instansi".equals(b.getStatusApproval()) ||
                 "pending_return".equals(b.getStatusApproval()))
            );
            
            tableRiwayat.setItems(FXCollections.observableArrayList(allBorrows));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ============================================================
    // ACTION HANDLERS
    // ============================================================
    
    private void handleApprovePengajuan(Borrow borrow) {
        if (!AlertUtil.showConfirmation("Konfirmasi Approval", 
            "Setujui peminjaman:\n" +
            "Peminjam: " + borrow.getNamaPeminjam() + "\n" +
            "Barang: " + borrow.getNamaBarang() + "\n" +
            "Jumlah: " + borrow.getJumlahPinjam() + " unit\n\n" +
            "Stok akan dikurangi setelah disetujui.")) {
            return;
        }
        
        Integer instansiId = sessionManager.getCurrentRoleId();
        if (instansiId == null) return;
        
        if (borrowDAO.approveByInstansi(borrow.getIdPeminjaman(), instansiId)) {
            AlertUtil.showSuccess("Berhasil", 
                "Pengajuan disetujui!\nStok telah dikurangi.");
            
            LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                "Menyetujui pengajuan peminjaman ID: " + borrow.getIdPeminjaman(),
                "APPROVE_PENGAJUAN",
                sessionManager.getCurrentRole()
            );
            
            loadAllData();
        } else {
            AlertUtil.showError("Gagal", 
                "Gagal menyetujui pengajuan!\nPastikan stok mencukupi.");
        }
    }
    
    private void handleRejectPengajuan(Borrow borrow) {
        // ... (Logic sama seperti sebelumnya) ...
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Tolak Pengajuan");
        dialog.setHeaderText("Tolak pengajuan dari: " + borrow.getNamaPeminjam());
        dialog.setContentText("Alasan penolakan:");
        
        dialog.showAndWait().ifPresent(alasan -> {
            if (alasan == null || alasan.trim().isEmpty()) {
                AlertUtil.showWarning("Alasan Wajib", "Harap berikan alasan penolakan!");
                return;
            }
            
            Integer instansiId = sessionManager.getCurrentRoleId();
            if (instansiId == null) return;
            
            if (borrowDAO.rejectByInstansi(borrow.getIdPeminjaman(), alasan.trim(), instansiId)) {
                AlertUtil.showSuccess("Berhasil", "Pengajuan ditolak.");
                
                LogActivityUtil.log(
                    sessionManager.getCurrentUsername(),
                    "Menolak pengajuan ID: " + borrow.getIdPeminjaman(),
                    "REJECT_PENGAJUAN",
                    sessionManager.getCurrentRole()
                );
                
                loadAllData();
            } else {
                AlertUtil.showError("Gagal", "Gagal menolak pengajuan!");
            }
        });
    }
    
    private void handleApprovePengembalian(Borrow borrow) {
        if (!AlertUtil.showConfirmation("Konfirmasi Pengembalian", "Terima pengembalian ini?")) {
            return;
        }
        
        Integer instansiId = sessionManager.getCurrentRoleId();
        if (instansiId == null) return;
        
        if (borrowDAO.approveReturn(borrow.getIdPeminjaman(), instansiId)) {
            AlertUtil.showSuccess("Berhasil", "Pengembalian diterima!");
            loadAllData();
            
            if (borrow.hasProblematicReturn()) {
                AlertUtil.showInfo("Info Laporan", "Terdapat barang rusak/hilang! Silakan buat laporan.");
            }
        } else {
            AlertUtil.showError("Gagal", "Gagal menerima pengembalian!");
        }
    }
    
    private void handleRejectPengembalian(Borrow borrow) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Tolak Pengembalian");
        dialog.setHeaderText("Tolak pengembalian dari: " + borrow.getNamaPeminjam());
        dialog.setContentText("Alasan penolakan:");
        
        dialog.showAndWait().ifPresent(alasan -> {
            if (alasan.trim().isEmpty()) {
                AlertUtil.showWarning("Alasan Wajib", "Harap berikan alasan penolakan!");
                return;
            }
            
            Integer instansiId = sessionManager.getCurrentRoleId();
            if (instansiId == null) return;
            
            if (borrowDAO.rejectReturn(borrow.getIdPeminjaman(), alasan, instansiId)) {
                AlertUtil.showSuccess("Berhasil", "Pengembalian ditolak.");
                loadAllData();
            } else {
                AlertUtil.showError("Gagal", "Gagal menolak pengembalian!");
            }
        });
    }

    // ============================================================
    // HELPER METHODS (Disini logikanya)
    // ============================================================

    /**
     * Mengatur kolom agar menampilkan nomor urut (1, 2, 3...)
     * bukan mengambil data dari database.
     */
    // ============================================================
    // HELPER METHODS
    // ============================================================

    private void setKolomNomor(TableColumn<Borrow, Integer> column) {
        // 1. Ubah Judul Header jadi "No"
        column.setText("No"); 
        
        column.setSortable(false);
        
        // 3. (Opsional) Bikin angkanya rata tengah biar rapi
        column.setStyle("-fx-alignment: CENTER;"); 

        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    // getIndex() mulai dari 0, jadi ditambah 1
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
    }
}