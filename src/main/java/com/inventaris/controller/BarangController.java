// ================================================================
// File: src/main/java/com/inventaris/controller/BarangController.java
// FIXED: Instansi hanya bisa melihat barang miliknya sendiri
// ================================================================
package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.dao.BarangDAO;
import com.inventaris.model.Barang;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import com.inventaris.util.ValidationUtil;
import java.io.File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

/**
 * BarangController - Manage CRUD operations for Barang
 * UPDATED: Instansi HANYA bisa lihat & edit barang miliknya sendiri
 */
public class BarangController implements Initializable {
    
    // ============================================================
    // FXML FIELDS
    // ============================================================
    @FXML private Button btnDashboard;
    @FXML private Button btnBarang;
    @FXML private Button btnPeminjaman;
    @FXML private Button btnLaporan;
    @FXML private Button btnLogout;
    @FXML private Button btnUser;
    
    // Form Fields
    @FXML private TextField kodeBarangField;
    @FXML private TextField namaBarangField;
    @FXML private TextField lokasiField;
    @FXML private TextField jumlahTotalField;
    @FXML private TextField jumlahTersediaField;
    @FXML private TextArea deskripsiArea;
    @FXML private ComboBox<String> kondisiCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> instansiCombo;
    
    // Table
    @FXML private TableView<Barang> barangTable;
    @FXML private TableColumn<Barang, String> colKode;
    @FXML private TableColumn<Barang, String> colNama;
    @FXML private TableColumn<Barang, String> colLokasi;
    @FXML private TableColumn<Barang, Integer> colTotal;
    @FXML private TableColumn<Barang, Integer> colTersedia;
    @FXML private TableColumn<Barang, String> colKondisi;
    @FXML private TableColumn<Barang, String> colStatus;
    @FXML private TableColumn<Barang, String> colPemilik;
    
    // Buttons & Search
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private TextField searchField;
    
    // ============================================================
    // FIELDS
    // ============================================================
    
    private final BarangDAO barangDAO = new BarangDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private Barang selectedBarang;
    private boolean isEditMode = false;
    
    // ============================================================
    // MENU CONFIGURATION
    // ============================================================
    
    private void configureMenuByRole(String role) {
        switch (role) {
            case "admin":
                btnBarang.setVisible(true);
                btnPeminjaman.setVisible(true);
                btnLaporan.setVisible(true);
                btnUser.setVisible(true);
                btnUser.setManaged(true);
                break;
                
            case "peminjam":
                btnBarang.setVisible(true);
                btnPeminjaman.setVisible(true);
                btnLaporan.setVisible(true);
                btnUser.setVisible(false);
                btnUser.setManaged(false);
                break;
                
            case "instansi":
                btnBarang.setVisible(true);
                btnPeminjaman.setVisible(true);
                btnLaporan.setVisible(true);
                btnUser.setVisible(false);
                btnUser.setManaged(false);
                break;
                
            default:
                btnBarang.setVisible(false);
                btnPeminjaman.setVisible(false);
                btnLaporan.setVisible(false);
                btnUser.setVisible(false);
                btnUser.setManaged(false);
        }
    }
    
    // ============================================================
    // INITIALIZATION
    // ============================================================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ComboBoxes
        kondisiCombo.setItems(FXCollections.observableArrayList(
            "baik", "rusak ringan", "rusak berat"
        ));
        kondisiCombo.setValue("baik");
        
        statusCombo.setItems(FXCollections.observableArrayList(
            "tersedia", "dipinjam", "rusak", "hilang"
        ));
        statusCombo.setValue("tersedia");
        
        // Configure menu
        configureMenuByRole(sessionManager.getCurrentRole());
        
        // Configure table columns
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeBarang"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colLokasi.setCellValueFactory(new PropertyValueFactory<>("lokasiBarang"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("jumlahTotal"));
        colTersedia.setCellValueFactory(new PropertyValueFactory<>("jumlahTersedia"));
        colKondisi.setCellValueFactory(new PropertyValueFactory<>("kondisiBarang"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Setup column pemilik
        colPemilik.setCellValueFactory(cellData -> {
            String pemilik = cellData.getValue().getNamaPemilik();
            return new javafx.beans.property.SimpleStringProperty(
                pemilik != null ? pemilik : "Umum"
            );
        });
        
        // Table selection listener
        barangTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedBarang = newSelection;
                    populateForm(newSelection);
                    setEditMode(true);
                }
            }
        );
        
        // Load instansi list
        loadInstansiList();
        
        // ✅ PERBAIKAN UTAMA: Role-based initialization
        if (sessionManager.isPeminjam()) {
            // Peminjam: read-only, lihat semua barang
            disableEditingForPeminjam();
            loadBarangData();
            
        } else if (sessionManager.isInstansi()) {
            // ✅ INSTANSI: HANYA lihat barang miliknya sendiri
            System.out.println("🔒 INSTANSI MODE - Loading barang untuk ID: " + sessionManager.getCurrentRoleId());
            loadBarangDataForInstansi();
            
        } else if (sessionManager.isAdmin()) {
            // Admin: full access, lihat semua barang
            loadBarangData();
        }
        
        // Set initial button states
        setEditMode(false);
        
        System.out.println("✅ Barang Controller initialized for role: " + sessionManager.getCurrentRole());
        System.out.println("📊 Role ID: " + sessionManager.getCurrentRoleId());
    }
    
    // ============================================================
    // DATA LOADING
    // ============================================================
    
    /**
     * Load all barang data (untuk Admin & Peminjam)
     */
    private void loadBarangData() {
        try {
            List<Barang> barangList = barangDAO.getAll();
            
            System.out.println("📦 Loading ALL barang: " + barangList.size() + " items");
            
            ObservableList<Barang> observableList = FXCollections.observableArrayList(barangList);
            barangTable.setItems(observableList);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data barang!");
            e.printStackTrace();
        }
    }
    
    /**
     * ✅ PERBAIKAN: Load barang HANYA untuk instansi tertentu
     */
    private void loadBarangDataForInstansi() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            
            if (instansiId == null) {
                System.err.println("❌ ERROR: instansiId is NULL!");
                AlertUtil.showError("Error", "ID Instansi tidak ditemukan!");
                return;
            }
            
            System.out.println("🔍 Loading barang untuk Instansi ID: " + instansiId);
            
            List<Barang> barangList = barangDAO.getByInstansi(instansiId);
            
            System.out.println("📦 Barang ditemukan: " + barangList.size() + " items");
            for (Barang b : barangList) {
                System.out.println("   - " + b.getKodeBarang() + " | " + b.getNamaBarang() + " | id_instansi=" + b.getIdInstansi());
            }
            
            ObservableList<Barang> observableList = FXCollections.observableArrayList(barangList);
            barangTable.setItems(observableList);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data barang instansi!");
            e.printStackTrace();
        }
    }
    
    /**
     * Load instansi list for combo box
     */
    private void loadInstansiList() {
        try {
            Connection conn = com.inventaris.config.DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT i.id_instansi, i.nama_instansi FROM instansi i " +
                         "JOIN user u ON i.id_user = u.id_user " +
                         "WHERE u.status = 'aktif' ORDER BY i.nama_instansi";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<String> instansiNames = new ArrayList<>();
            instansiNames.add("Umum");
            
            while (rs.next()) {
                instansiNames.add(rs.getString("nama_instansi"));
            }
            
            instansiCombo.setItems(FXCollections.observableArrayList(instansiNames));
            
            // Jika instansi login, auto-select & disable
            if (sessionManager.isInstansi()) {
                String currentInstansi = getCurrentInstansiName();
                instansiCombo.setValue(currentInstansi);
                instansiCombo.setDisable(true);
            } else {
                instansiCombo.setValue("Umum");
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error loading instansi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private File fileFotoDipilih = null;
    
    @FXML
    private void handleAturFoto() {
        // 1. Buat Dialog Baru
        Dialog<File> dialog = new Dialog<>();
        dialog.setTitle("Atur Foto Barang");
        dialog.setHeaderText("Preview & Upload Foto");
        
        // Agar pop-up tidak bisa diklik di luar (Modal)
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(namaBarangField.getScene().getWindow());

        // 2. Buat Komponen UI secara Programmatic (Tanpa FXML)
        ImageView imgViewPopup = new ImageView();
        imgViewPopup.setFitWidth(200);
        imgViewPopup.setFitHeight(200);
        imgViewPopup.setPreserveRatio(true);
        imgViewPopup.setStyle("-fx-border-color: gray; -fx-border-width: 1;");

        Button btnBrowse = new Button("Pilih File dari Komputer...");
        
        // 3. Logika Menampilkan Gambar Awal
        // Prioritas 1: Tampilkan file yang barusan dipilih (tapi belum disave ke DB)
        if (fileFotoDipilih != null) {
            imgViewPopup.setImage(new Image(fileFotoDipilih.toURI().toString()));
        } 
        // Prioritas 2: Jika sedang Edit, tampilkan foto dari Database
        else if (selectedBarang != null && selectedBarang.getFotoUrl() != null) {
            try {
                String projectDir = System.getProperty("user.dir");
                File fileDb = new File(projectDir + selectedBarang.getFotoUrl());
                if (fileDb.exists()) {
                    imgViewPopup.setImage(new Image(fileDb.toURI().toString()));
                } else {
                    // Gambar placeholder jika file fisik hilang
                   // imgViewPopup.setImage(new Image("url_placeholder_disini")); 
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        // 4. Action Tombol Browse di dalam Pop-up
        btnBrowse.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Gambar", "*.jpg", "*.png", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                // Tampilkan preview
                imgViewPopup.setImage(new Image(file.toURI().toString()));
                // Simpan hasil pilihan ke result dialog nanti
                dialog.setResult(file); 
            }
        });

        // 5. Susun Layout Pop-up (VBox)
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(imgViewPopup, btnBrowse);
        dialog.getDialogPane().setContent(layout);

        // 6. Tambahkan Tombol OK & Cancel
        ButtonType btnOk = new ButtonType("Gunakan Foto Ini", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        // 7. Tampilkan Dialog dan Tunggu Hasil
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnOk) {
                // Jika user klik OK, kembalikan file yang dipilih (bisa null jika tidak ganti)
                return (File) dialog.getResult(); 
            }
            return null;
        });

        Optional<File> result = dialog.showAndWait();
        
        // Jika user klik OK dan sudah memilih file baru
        result.ifPresent(file -> {
            this.fileFotoDipilih = file; // Simpan ke variable global controller
            AlertUtil.showInfo("Info", "Foto dipilih: " + file.getName() + "\n(Klik Simpan/Update untuk menerapkan)");
        });
    }
    // ============================================================
    // NAVIGATION HANDLERS
    // ============================================================
    
    @FXML
    private void handleBarang() {
        Main.loadContent("Barang.fxml");
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
    
    @FXML
    private void handleUser() {
        Main.loadContent("User.fxml");
    }
    
    // ============================================================
    // CRUD OPERATIONS
    // ============================================================
    
    /**
     * Handle save button - Create new barang
     */
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            Barang barang = new Barang();
            // Ambil data dari TextFields
            String kode = kodeBarangField.getText().trim().toUpperCase();
            barang.setKodeBarang(kode);
            barang.setNamaBarang(namaBarangField.getText().trim());
            barang.setLokasiBarang(lokasiField.getText().trim());
            barang.setJumlahTotal(Integer.parseInt(jumlahTotalField.getText()));
            barang.setJumlahTersedia(Integer.parseInt(jumlahTersediaField.getText()));
            barang.setDeskripsi(deskripsiArea.getText().trim());
            barang.setKondisiBarang(kondisiCombo.getValue());
            barang.setStatus(statusCombo.getValue());
            
            // Set instansi pemilik
            String instansiName = instansiCombo.getValue();
            Integer instansiId = getInstansiIdByName(instansiName);
            barang.setIdInstansi(instansiId);
            
            // ============================================================
            // ✅ UPDATE PENTING: PROSES UPLOAD FOTO
            // ============================================================
            if (this.fileFotoDipilih != null) {
                // Panggil fungsi helper upload
                String pathGambar = uploadFoto(this.fileFotoDipilih, kode);
                // Set path ke objek barang agar tersimpan di database
                barang.setFotoUrl(pathGambar);
            }
            // ============================================================

            if (barangDAO.create(barang)) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil ditambahkan!");
                
                // Log activity
                LogActivityUtil.logCreate(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "barang",
                    barang.getKodeBarang() + " - " + barang.getNamaBarang()
                );
                
                clearForm();
                
                // Reset file foto setelah berhasil simpan
                this.fileFotoDipilih = null; 
                
                // Reload data tabel
                if (sessionManager.isInstansi()) {
                    loadBarangDataForInstansi();
                } else {
                    loadBarangData();
                }
            } else {
                AlertUtil.showError("Gagal", "Gagal menambahkan barang!");
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    /**
     * Helper untuk copy file foto ke folder project
     */
    private String uploadFoto(File file, String kodeBarang) {
        try {
            // 1. Tentukan folder tujuan: project_dir/images/barang/
            String projectDir = System.getProperty("user.dir");
            Path uploadDir = java.nio.file.Paths.get(projectDir, "images", "barang");

            // 2. Buat folder jika belum ada
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            // 3. Buat nama file unik (KODE_TIMESTAMP.ext)
            String originalName = file.getName();
            String ext = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) {
                ext = originalName.substring(i);
            }
            
            String newFileName = kodeBarang + "_" + System.currentTimeMillis() + ext;

            // 4. Copy file
            java.nio.file.Files.copy(file.toPath(), uploadDir.resolve(newFileName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 5. Return path relatif untuk disimpan di database
            return "/images/barang/" + newFileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    /**
     * Handle update button - Update existing barang
     */
    @FXML
    private void handleUpdate() {
        if (selectedBarang == null) {
            AlertUtil.showWarning("Peringatan", "Pilih barang yang akan diupdate!");
            return;
        }
        
        // ✅ Check authorization
        if (!canEdit(selectedBarang)) {
            AlertUtil.showWarning("Akses Ditolak", 
                "Anda hanya bisa mengedit barang milik instansi Anda!");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        if (!AlertUtil.showConfirmation("Konfirmasi", "Update data barang ini?")) {
            return;
        }
        
        try {
            selectedBarang.setNamaBarang(namaBarangField.getText().trim());
            selectedBarang.setLokasiBarang(lokasiField.getText().trim());
            selectedBarang.setJumlahTotal(Integer.parseInt(jumlahTotalField.getText()));
            selectedBarang.setJumlahTersedia(Integer.parseInt(jumlahTersediaField.getText()));
            selectedBarang.setDeskripsi(deskripsiArea.getText().trim());
            selectedBarang.setKondisiBarang(kondisiCombo.getValue());
            selectedBarang.setStatus(statusCombo.getValue());
            
            // Update instansi (hanya admin yang bisa ganti)
            if (sessionManager.isAdmin()) {
                String instansiName = instansiCombo.getValue();
                Integer instansiId = getInstansiIdByName(instansiName);
                selectedBarang.setIdInstansi(instansiId);
            }
            
            if (barangDAO.update(selectedBarang)) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil diupdate!");
                
                // Log activity
                LogActivityUtil.logUpdate(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "barang",
                    selectedBarang.getKodeBarang() + " - " + selectedBarang.getNamaBarang()
                );
                
                clearForm();
                
                // ✅ Reload sesuai role
                if (sessionManager.isInstansi()) {
                    loadBarangDataForInstansi();
                } else {
                    loadBarangData();
                }
                
                setEditMode(false);
            } else {
                AlertUtil.showError("Gagal", "Gagal mengupdate barang!");
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle delete button
     */
    @FXML
    private void handleDelete() {
        if (selectedBarang == null) {
            AlertUtil.showWarning("Peringatan", "Pilih barang yang akan dihapus!");
            return;
        }
        
        // ✅ Check authorization
        if (!canEdit(selectedBarang)) {
            AlertUtil.showWarning("Akses Ditolak", 
                "Anda hanya bisa menghapus barang milik instansi Anda!");
            return;
        }
        
        if (!AlertUtil.showDeleteConfirmation(selectedBarang.getNamaBarang())) {
            return;
        }
        
        try {
            if (barangDAO.delete(selectedBarang.getKodeBarang())) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil dihapus!");
                
                // Log activity
                LogActivityUtil.logDelete(
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentRole(),
                    "barang",
                    selectedBarang.getKodeBarang() + " - " + selectedBarang.getNamaBarang()
                );
                
                clearForm();
                
                // ✅ Reload sesuai role
                if (sessionManager.isInstansi()) {
                    loadBarangDataForInstansi();
                } else {
                    loadBarangData();
                }
                
                setEditMode(false);
            } else {
                AlertUtil.showError("Gagal", "Gagal menghapus barang!");
            }
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ✅ PERBAIKAN: Handle search dengan filter instansi
     */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            // ✅ Reload sesuai role
            if (sessionManager.isInstansi()) {
                loadBarangDataForInstansi();
            } else {
                loadBarangData();
            }
            return;
        }
        
        try {
            List<Barang> results = barangDAO.search(keyword);
            
            // ✅ Filter by instansi jika instansi yang login
            if (sessionManager.isInstansi()) {
                Integer instansiId = sessionManager.getCurrentRoleId();
                results.removeIf(b -> 
                    b.getIdInstansi() == null || 
                    !b.getIdInstansi().equals(instansiId)
                );
            }
            
            ObservableList<Barang> observableList = FXCollections.observableArrayList(results);
            barangTable.setItems(observableList);
            
            System.out.println("🔍 Search results: " + results.size() + " items");
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal melakukan pencarian!");
            e.printStackTrace();
        }
    }
    
    /**
     * Public method untuk search dari controller lain (DashboardController)
     */
    public void searchBarang(String keyword) {
        searchField.setText(keyword);
        handleSearch();
    }
    
    // ============================================================
    // FORM MANAGEMENT
    // ============================================================
    
    /**
     * Populate form with selected barang
     */
    private void populateForm(Barang barang) {
        kodeBarangField.setText(barang.getKodeBarang());
        namaBarangField.setText(barang.getNamaBarang());
        lokasiField.setText(barang.getLokasiBarang());
        jumlahTotalField.setText(String.valueOf(barang.getJumlahTotal()));
        jumlahTersediaField.setText(String.valueOf(barang.getJumlahTersedia()));
        deskripsiArea.setText(barang.getDeskripsi());
        kondisiCombo.setValue(barang.getKondisiBarang());
        statusCombo.setValue(barang.getStatus());
        
        // Set instansi combo
        if (barang.getNamaPemilik() != null) {
            instansiCombo.setValue(barang.getNamaPemilik());
        } else {
            instansiCombo.setValue("Umum");
        }
    }
    
    /**
     * Clear form
     */
    @FXML
    private void handleClear() {
        clearForm();
        setEditMode(false);
        barangTable.getSelectionModel().clearSelection();
    }
    
    /**
     * Clear all form fields
     */
    private void clearForm() {
        kodeBarangField.clear();
        namaBarangField.clear();
        lokasiField.clear();
        jumlahTotalField.clear();
        jumlahTersediaField.clear();
        deskripsiArea.clear();
        kondisiCombo.setValue("baik");
        statusCombo.setValue("tersedia");
        
        // Reset instansi combo
        if (sessionManager.isInstansi()) {
            instansiCombo.setValue(getCurrentInstansiName());
        } else {
            instansiCombo.setValue("Umum");
        }
        
        selectedBarang = null;
    }
    
    // ============================================================
    // VALIDATION
    // ============================================================
    
    /**
     * Validate input
     */
    private boolean validateInput() {
        if (ValidationUtil.isEmpty(kodeBarangField.getText())) {
            AlertUtil.showWarning("Validasi", "Kode barang tidak boleh kosong!");
            kodeBarangField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidKodeBarang(kodeBarangField.getText())) {
            AlertUtil.showWarning("Validasi", "Format kode barang tidak valid! (A-Z, 0-9, -)");
            kodeBarangField.requestFocus();
            return false;
        }
        
        if (ValidationUtil.isEmpty(namaBarangField.getText())) {
            AlertUtil.showWarning("Validasi", "Nama barang tidak boleh kosong!");
            namaBarangField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isNonNegativeNumber(jumlahTotalField.getText())) {
            AlertUtil.showWarning("Validasi", "Jumlah total harus berupa angka positif!");
            jumlahTotalField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isNonNegativeNumber(jumlahTersediaField.getText())) {
            AlertUtil.showWarning("Validasi", "Jumlah tersedia harus berupa angka positif!");
            jumlahTersediaField.requestFocus();
            return false;
        }
        
        int total = Integer.parseInt(jumlahTotalField.getText());
        int tersedia = Integer.parseInt(jumlahTersediaField.getText());
        
        if (tersedia > total) {
            AlertUtil.showWarning("Validasi", "Jumlah tersedia tidak boleh lebih dari jumlah total!");
            jumlahTersediaField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    // ============================================================
    // AUTHORIZATION HELPERS
    // ============================================================
    
    /**
     * ✅ Check if can edit barang (PENTING untuk keamanan!)
     */
    private boolean canEdit(Barang barang) {
        // Admin bisa edit semua
        if (sessionManager.isAdmin()) return true;
        
        // Instansi HANYA bisa edit barang miliknya sendiri
        if (sessionManager.isInstansi()) {
            Integer instansiId = sessionManager.getCurrentRoleId();
            return barang.getIdInstansi() != null && 
                   barang.getIdInstansi().equals(instansiId);
        }
        
        // Peminjam tidak bisa edit
        return false;
    }
    
    /**
     * Disable editing for peminjam (read-only)
     */
    private void disableEditingForPeminjam() {
        btnSave.setDisable(true);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        kodeBarangField.setDisable(true);
        namaBarangField.setDisable(true);
        lokasiField.setDisable(true);
        jumlahTotalField.setDisable(true);
        jumlahTersediaField.setDisable(true);
        deskripsiArea.setDisable(true);
        kondisiCombo.setDisable(true);
        statusCombo.setDisable(true);
        instansiCombo.setDisable(true);
    }
    
    /**
     * Set edit mode
     */
    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        kodeBarangField.setDisable(editMode || sessionManager.isPeminjam());
        btnSave.setDisable(editMode || sessionManager.isPeminjam());
        btnUpdate.setDisable(!editMode || sessionManager.isPeminjam());
        btnDelete.setDisable(!editMode || sessionManager.isPeminjam());
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Get current instansi name
     */
    private String getCurrentInstansiName() {
        try {
            Integer instansiId = sessionManager.getCurrentRoleId();
            Connection conn = com.inventaris.config.DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT nama_instansi FROM instansi WHERE id_instansi = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instansiId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("nama_instansi");
                rs.close();
                stmt.close();
                conn.close();
                return name;
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get instansi ID by name
     */
    private Integer getInstansiIdByName(String name) {
        if ("Umum".equals(name)) return null;
        
        try {
            Connection conn = com.inventaris.config.DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT id_instansi FROM instansi WHERE nama_instansi = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("id_instansi");
                rs.close();
                stmt.close();
                conn.close();
                return id;
            }
            
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}