package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.dao.BarangDAO;
import com.inventaris.dao.InstansiDAO;
import com.inventaris.model.Barang;
import com.inventaris.model.CartItem;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.CartManager;
import com.inventaris.util.SessionManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.FileChooser;

/**
 * DataBarangPeminjamController - Halaman Katalog Barang
 * UPDATED: Instansi tanpa keranjang + fitur tambah barang baru
 */
public class DataBarangPeminjamController implements Initializable {
    
    // --- UI Component ---
    @FXML private Label cartBadge;
    @FXML private Label lblTotalBarang;
    @FXML private Label lblResultCount;
    @FXML private ComboBox<String> filterLembaga;
    @FXML private ComboBox<String> filterBEM;
    @FXML private ComboBox<String> filterHimpunan;
    @FXML private ComboBox<String> filterUKM;
    @FXML private ComboBox<String> sortCombo;
    @FXML private FlowPane catalogGrid;
    @FXML private VBox emptyState;
    @FXML private StackPane contentArea;
    @FXML private ScrollPane filterSidebar;
    @FXML private HBox cartContainer; // ✅ NEW: Container untuk keranjang
    @FXML private HBox actionButtonContainer; // ✅ NEW: Container untuk tombol aksi
    
    private Parent currentContent;
    
    // --- Data & Tools ---
    private final BarangDAO barangDAO = new BarangDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private List<Barang> allBarang = new ArrayList<>();
    private List<Barang> filteredBarang = new ArrayList<>();
    
    // Keyword pencarian dari LayoutController
    private String currentSearchKeyword = ""; 
    
    // 🛡️ FLAG PENGAMAN: Mencegah error looping saat reset otomatis
    private boolean isUpdatingFilter = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 DataBarangPeminjam initializing...");
        System.out.println("👤 Current Role: " + sessionManager.getCurrentRole());
        System.out.println("🆔 Role ID: " + sessionManager.getCurrentRoleId());
        
        // ✅ 1. Sembunyikan Filter Sidebar untuk Instansi
        hideFilterForInstansi();
        
        // ✅ 2. Configure UI untuk Instansi (hapus keranjang, tambah button)
        configureUIForRole();
        
        // ✅ 3. Ambil Data dari Database (DENGAN FILTER ROLE!)
        loadAllBarang();

        // ✅ 4. Isi Pilihan Dropdown (SESUAI ROLE!)
        loadFilters();
        
        // 5. Pasang Logic "Saling Reset"
        setupListeners();
        
        // 6. Pastikan tampilan awal bersih
        handleResetFilter(); 
        
        // 7. Cek Keranjang (hanya untuk peminjam)
        if (!sessionManager.isInstansi()) {
            updateCartBadge();
        }
        
        System.out.println("✅ DataBarang Initialized. Total barang: " + allBarang.size());
    }
    
    // ============================================================
    // ✅ NEW: CONFIGURE UI UNTUK ROLE
    // ============================================================
    
    /**
     * ✅ Configure UI: Hapus keranjang & tambah button untuk Instansi
     */
    private void configureUIForRole() {
        if (sessionManager.isInstansi()) {
            System.out.println("🔧 Configuring UI for INSTANSI");
            
            // ❌ Sembunyikan keranjang
            if (cartContainer != null) {
                cartContainer.setVisible(false);
                cartContainer.setManaged(false);
                System.out.println("   ❌ Cart - HIDDEN");
            }
            
            // ✅ Tambahkan button "Tambah Barang"
            if (actionButtonContainer != null) {
                Button btnTambah = new Button("+ Tambah Barang Baru");
                btnTambah.setStyle(
                    "-fx-background-color: #6A5436; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 20; " +
                    "-fx-padding: 10 20; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 14px;"
                );
                btnTambah.setOnAction(e -> handleTambahBarang());
                
                actionButtonContainer.getChildren().clear();
                actionButtonContainer.getChildren().add(btnTambah);
                System.out.println("   ✅ Tambah Barang Button - ADDED");
            }
        } else {
            System.out.println("👁️ Normal mode (Peminjam/Admin) - Cart visible");
        }
    }
    
    // ============================================================
    // ✅ NEW: HIDE FILTER SIDEBAR UNTUK INSTANSI
    // ============================================================
    
    /**
     * ✅ Sembunyikan sidebar filter jika yang login adalah Instansi
     */
    private void hideFilterForInstansi() {
        if (sessionManager.isInstansi() && filterSidebar != null) {
            System.out.println("🚫 Hiding filter sidebar for Instansi");
            filterSidebar.setVisible(false);
            filterSidebar.setManaged(false);
        }
    }
    
    // ============================================================
    // ✅ PERBAIKAN UTAMA: LOAD DATA SESUAI ROLE
    // ============================================================
    
    /**
     * ✅ FIXED: Load barang sesuai role yang login
     */
    private void loadAllBarang() {
        try {
            if (sessionManager.isInstansi()) {
                // ✅ INSTANSI: Hanya barang miliknya sendiri
                Integer instansiId = sessionManager.getCurrentRoleId();
                
                if (instansiId == null) {
                    System.err.println("❌ ERROR: instansiId is NULL!");
                    AlertUtil.showError("Error", "ID Instansi tidak ditemukan!");
                    allBarang = new ArrayList<>();
                    return;
                }
                
                System.out.println("🔒 INSTANSI MODE - Loading barang untuk ID: " + instansiId);
                
                // Ambil SEMUA barang instansi ini (termasuk yang stok 0)
                allBarang = barangDAO.getByInstansi(instansiId);
                
                System.out.println("📦 Barang instansi ditemukan: " + allBarang.size() + " items");
                for (Barang b : allBarang) {
                    System.out.println("   - " + b.getKodeBarang() + " | " + b.getNamaBarang() + " | Stok: " + b.getJumlahTersedia());
                }
                
            } else {
                // ✅ ADMIN & PEMINJAM: Lihat semua barang available
                System.out.println("👁️ Loading ALL available barang (Admin/Peminjam mode)");
                allBarang = barangDAO.getAvailable();
            }
            
            lblTotalBarang.setText(allBarang.size() + " items");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading barang: " + e.getMessage());
            e.printStackTrace();
            allBarang = new ArrayList<>();
        }
    }

    /**
     * ✅ FIXED: Load filter dropdown sesuai role
     */
    private void loadFilters() {
        InstansiDAO dao = new InstansiDAO();
        
        if (sessionManager.isInstansi()) {
            // ✅ INSTANSI: Skip loading filter (sidebar sudah disembunyikan)
            System.out.println("🔒 Instansi mode - Skipping filter setup");
            
        } else {
            // ✅ ADMIN & PEMINJAM: Enable filter normal
            System.out.println("👁️ Admin/Peminjam mode - Loading all filters");
            
            fillCombo(filterLembaga, dao.getByKategori("LEMBAGA"));
            fillCombo(filterBEM, dao.getByKategori("BEM"));
            fillCombo(filterHimpunan, dao.getByKategori("HIMPUNAN"));
            fillCombo(filterUKM, dao.getByKategori("UKM"));
        }

        // Sort combo tetap aktif untuk semua role
        sortCombo.getItems().setAll("Terbaru", "Nama A-Z", "Nama Z-A", "Stok Terbanyak");
        sortCombo.setValue("Terbaru");
    }
    
    // ============================================================
    // 1. LOGIC FILTER INTERACTION
    // ============================================================
    
    private void setupListeners() {
        // Skip setup listeners untuk instansi (karena filter disembunyikan)
        if (sessionManager.isInstansi()) {
            sortCombo.setOnAction(e -> applyFiltersAndDisplay());
            return;
        }
        
        // Pasang listener khusus ke setiap ComboBox (untuk Admin & Peminjam)
        filterLembaga.setOnAction(e -> handleSingleFilterSelection(filterLembaga));
        filterBEM.setOnAction(e -> handleSingleFilterSelection(filterBEM));
        filterHimpunan.setOnAction(e -> handleSingleFilterSelection(filterHimpunan));
        filterUKM.setOnAction(e -> handleSingleFilterSelection(filterUKM));
        
        // Listener Sort beda sendiri (tidak mereset filter lain)
        sortCombo.setOnAction(e -> applyFiltersAndDisplay());
    }

    /**
     * Logic Pintar: Saat satu dipilih, yang lain otomatis jadi "Semua"
     */
    private void handleSingleFilterSelection(ComboBox<String> sourceCombo) {
        // Jika sedang proses reset, jangan jalankan logic ini
        if (isUpdatingFilter) return; 
        
        // Jika instansi yang login, skip filter logic
        if (sessionManager.isInstansi()) {
            applyFiltersAndDisplay();
            return;
        }

        String selectedValue = sourceCombo.getValue();

        // Jika user memilih sesuatu yang BUKAN "Semua"
        if (selectedValue != null && !"Semua".equals(selectedValue)) {
            
            // 🔒 Kunci pintu dulu
            isUpdatingFilter = true; 
            
            try {
                // Reset ComboBox lain selain yang sedang dipilih
                if (sourceCombo != filterLembaga) filterLembaga.setValue("Semua");
                if (sourceCombo != filterBEM) filterBEM.setValue("Semua");
                if (sourceCombo != filterHimpunan) filterHimpunan.setValue("Semua");
                if (sourceCombo != filterUKM) filterUKM.setValue("Semua");
            } finally {
                // 🔓 Buka kunci pintu
                isUpdatingFilter = false; 
            }
        }
        
        // Terapkan filter ke layar
        applyFiltersAndDisplay();
    }

    @FXML
    private void handleResetFilter() {
        // Skip untuk instansi (tidak ada filter)
        if (sessionManager.isInstansi()) {
            this.currentSearchKeyword = ""; 
            sortCombo.setValue("Terbaru");
            applyFiltersAndDisplay();
            return;
        }
        
        // 🔒 Kunci pintu agar listener di atas tidak 'kaget'
        isUpdatingFilter = true;
        
        try {
            filterLembaga.setValue("Semua");
            filterBEM.setValue("Semua");
            filterHimpunan.setValue("Semua");
            filterUKM.setValue("Semua");
            
            this.currentSearchKeyword = ""; 
            sortCombo.setValue("Terbaru");
            
        } finally {
            // 🔓 Buka kunci
            isUpdatingFilter = false;
        }
        
        // Tampilkan ulang semua data
        applyFiltersAndDisplay();
    }

    // ============================================================
    // 2. DATA PROCESSING & DISPLAY
    // ============================================================

    private void applyFiltersAndDisplay() {
        filteredBarang = new ArrayList<>(allBarang);
        
        // ✅ Jika instansi, skip filter instansi (karena sudah difilter di loadAllBarang)
        if (!sessionManager.isInstansi()) {
            // --- FILTER 1: INSTANSI (Hanya untuk Admin & Peminjam) ---
            InstansiDAO dao = new InstansiDAO();
            String selectedInstansi = null;

            if (!"Semua".equals(filterLembaga.getValue())) selectedInstansi = filterLembaga.getValue();
            else if (!"Semua".equals(filterBEM.getValue())) selectedInstansi = filterBEM.getValue();
            else if (!"Semua".equals(filterHimpunan.getValue())) selectedInstansi = filterHimpunan.getValue();
            else if (!"Semua".equals(filterUKM.getValue())) selectedInstansi = filterUKM.getValue();

            if (selectedInstansi != null) {
                int id = dao.getIdByNama(selectedInstansi);
                filteredBarang = filteredBarang.stream()
                    .filter(b -> b.getIdInstansi() != null && b.getIdInstansi() == id)
                    .collect(Collectors.toList());
            }
        }

        // --- FILTER 2: SEARCH KEYWORD (Dari LayoutController) ---
        if (!currentSearchKeyword.isEmpty()) {
            String lowerKey = currentSearchKeyword.toLowerCase();
            filteredBarang = filteredBarang.stream()
                .filter(b -> b.getNamaBarang().toLowerCase().contains(lowerKey))
                .collect(Collectors.toList());
        }

        // --- FILTER 3: SORTING ---
        String sort = sortCombo.getValue();
        if (sort != null) {
            switch (sort) {
                case "Nama A-Z": 
                    filteredBarang.sort(Comparator.comparing(Barang::getNamaBarang)); 
                    break;
                case "Nama Z-A": 
                    filteredBarang.sort(Comparator.comparing(Barang::getNamaBarang).reversed()); 
                    break;
                case "Stok Terbanyak": 
                    filteredBarang.sort(Comparator.comparingInt(Barang::getJumlahTersedia).reversed()); 
                    break;
            }
        }

        displayCatalog();
    }

    private void displayCatalog() {
        catalogGrid.getChildren().clear();
        boolean isEmpty = filteredBarang.isEmpty();
        
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
        
        lblResultCount.setText("Menampilkan " + filteredBarang.size() + " barang");

        if (!isEmpty) {
            for (Barang barang : filteredBarang) {
                catalogGrid.getChildren().add(createBarangCard(barang));
            }
        }
        
        System.out.println("📊 Displayed " + filteredBarang.size() + " items");
    }

    // ============================================================
    // 3. UTILITIES & INITIAL DATA SETUP
    // ============================================================

    private void fillCombo(ComboBox<String> combo, List<String> items) {
        combo.getItems().clear();
        combo.getItems().add("Semua");
        if (items != null) combo.getItems().addAll(items);
        combo.setValue("Semua");
    }

    public void searchBarang(String keyword) {
        this.currentSearchKeyword = (keyword == null) ? "" : keyword.trim();
        applyFiltersAndDisplay();
    }

    // ============================================================
    // 4. UI COMPONENTS (CARD & CART)
    // ============================================================

    private VBox createBarangCard(Barang barang) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(220, 320);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // --- BAGIAN IMAGE HANDLING (DIPERBAIKI) ---
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180); 
        imageView.setFitHeight(140); 
        imageView.setPreserveRatio(true);
        
        try {
            String path = barang.getFotoUrl();
            
            // Cek apakah ada path foto
            if (path != null && !path.isBlank()) {
                // 1. Cek apakah ini URL online (http)
                if (path.startsWith("http")) {
                    imageView.setImage(new Image(path, true));
                } 
                // 2. Cek File di Folder Project (Hasil Upload)
                else {
                    String projectDir = System.getProperty("user.dir");
                    File fileGambar = new File(projectDir + path); // Gabungkan folder project + path database
                    
                    if (fileGambar.exists()) {
                        // Load dari Harddisk/Folder Project
                        imageView.setImage(new Image(fileGambar.toURI().toString()));
                    } else {
                        // Jika tidak ada di folder, coba cari di resources (bawaan aplikasi)
                        InputStream is = getClass().getResourceAsStream(path);
                        if (is != null) imageView.setImage(new Image(is));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal load gambar: " + e.getMessage());
        }
        
        // Placeholder jika gambar masih null (gagal load atau belum ada)
        if (imageView.getImage() == null) {
            try {
                // Pastikan path placeholder benar (misal di folder resources)
                // Jika tidak punya file placeholder.png, bagian ini bisa dihapus atau diganti
                InputStream ph = getClass().getResourceAsStream("/images/barang/placeholder.png");
                if (ph != null) imageView.setImage(new Image(ph));
            } catch (Exception ignored) {}
        }
        // -------------------------------------------

        // Labels
        Label nameLbl = new Label(barang.getNamaBarang());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        nameLbl.setWrapText(true); 
        nameLbl.setAlignment(Pos.CENTER);

        Label stokLbl = new Label("Stok: " + barang.getJumlahTersedia());
        stokLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label ownerLbl = new Label(barang.getNamaPemilik() != null ? barang.getNamaPemilik() : "Umum");
        ownerLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6A5436;");

        // Button berbeda untuk Instansi vs Peminjam
        Button btnAdd = new Button();
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        
        if (sessionManager.isInstansi()) {
            btnAdd.setText("✏️ Edit Barang");
            btnAdd.setStyle("-fx-background-color: #8B6F47; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            btnAdd.setOnAction(e -> handleEditBarang(barang));
        } else {
            btnAdd.setText("+ Keranjang");
            btnAdd.setStyle("-fx-background-color: #6A5436; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            btnAdd.setOnAction(e -> showAddToCartDialog(barang));
        }

        card.getChildren().addAll(imageView, nameLbl, stokLbl, ownerLbl, btnAdd);
        return card;
    }

    // ============================================================
    // ✅ NEW: HANDLER TAMBAH BARANG BARU (INSTANSI)
    // ============================================================
    
    /**
     * ✅ Dialog untuk tambah barang baru (khusus instansi)
     */
   private void handleTambahBarang() {
        Dialog<Barang> dialog = new Dialog<>();
        dialog.setTitle("Tambah Barang Baru");
        dialog.setHeaderText("Tambah Barang untuk " + sessionManager.getCurrentUser().getNama());

        // Reset foto sementara
        this.tempFileFoto = null;

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450); // Lebarkan sedikit

        // --- FIELD INPUT ---
        TextField tfKode = new TextField(); tfKode.setPromptText("Kode (Contoh: PSTI-004)");
        TextField tfNama = new TextField(); tfNama.setPromptText("Nama Barang");
        TextField tfLokasi = new TextField(); tfLokasi.setPromptText("Lokasi (Contoh: Lab PSTI)");
        Spinner<Integer> spinnerTotal = new Spinner<>(1, 9999, 1); spinnerTotal.setEditable(true);
        TextArea taDeskripsi = new TextArea(); taDeskripsi.setPromptText("Deskripsi..."); taDeskripsi.setPrefRowCount(3);

        // --- BAGIAN FOTO ---
        Label lblFoto = new Label("Foto Barang:");
        ImageView imgPreview = new ImageView();
        imgPreview.setFitWidth(100); imgPreview.setFitHeight(100); imgPreview.setPreserveRatio(true);
        imgPreview.setStyle("-fx-border-color: #ccc; -fx-border-style: dashed;");
        
        Button btnPilihFoto = new Button("📷 Pilih Foto");
        Label lblPathFoto = new Label("Belum ada file");
        lblPathFoto.setStyle("-fx-font-size: 10px; -fx-text-fill: grey;");

        // Logic Tombol Pilih Foto
        btnPilihFoto.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gambar", "*.jpg", "*.png", "*.jpeg"));
            File f = fc.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (f != null) {
                this.tempFileFoto = f; // Simpan ke variabel global sementara
                imgPreview.setImage(new Image(f.toURI().toString()));
                lblPathFoto.setText(f.getName());
            }
        });

        HBox photoBox = new HBox(10, imgPreview, new VBox(5, btnPilihFoto, lblPathFoto));
        photoBox.setAlignment(Pos.CENTER_LEFT);

        // --- SUSUN LAYOUT ---
        content.getChildren().addAll(
            new Label("Kode Barang:"), tfKode,
            new Label("Nama Barang:"), tfNama,
            new Label("Lokasi:"), tfLokasi,
            new Label("Jumlah Total:"), spinnerTotal,
            new Label("Deskripsi:"), taDeskripsi,
            new Separator(),
            lblFoto, photoBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- CONVERTER HASIL ---
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (tfKode.getText().trim().isEmpty() || tfNama.getText().trim().isEmpty()) {
                    AlertUtil.showWarning("Validasi", "Kode dan Nama wajib diisi!");
                    return null;
                }

                Barang b = new Barang();
                b.setKodeBarang(tfKode.getText().trim().toUpperCase());
                b.setNamaBarang(tfNama.getText().trim());
                b.setLokasiBarang(tfLokasi.getText().trim());
                b.setJumlahTotal(spinnerTotal.getValue());
                b.setJumlahTersedia(spinnerTotal.getValue());
                b.setDeskripsi(taDeskripsi.getText().trim());
                b.setKondisiBarang("baik");
                b.setStatus("tersedia");
                b.setIdInstansi(sessionManager.getCurrentRoleId());
                
                return b;
            }
            return null;
        });

        // --- EKSEKUSI SETELAH OK DITEKAN ---
        dialog.showAndWait().ifPresent(newBarang -> {
            if (barangDAO.kodeExists(newBarang.getKodeBarang())) {
                AlertUtil.showError("Error", "Kode barang sudah digunakan!");
                return;
            }

            // PROSES UPLOAD FOTO
            if (this.tempFileFoto != null) {
                String path = uploadFoto(this.tempFileFoto, newBarang.getKodeBarang());
                newBarang.setFotoUrl(path);
            }

            if (barangDAO.create(newBarang)) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil ditambahkan!");
                loadAllBarang();
                applyFiltersAndDisplay();
            } else {
                AlertUtil.showError("Gagal", "Gagal menambahkan barang ke database.");
            }
        });
    }
    /**
     * ✅ Handler untuk edit barang (khusus instansi)
     */
   /**
     * ✅ Handler untuk edit barang (khusus instansi) - WITH DELETE OPTION
     */
    private void handleEditBarang(Barang barang) {
        Dialog<Barang> dialog = new Dialog<>();
        dialog.setTitle("Edit Barang");
        dialog.setHeaderText("Edit: " + barang.getNamaBarang());

        // Reset foto sementara
        this.tempFileFoto = null;

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        // --- FIELD INPUT ---
        Spinner<Integer> spinnerTotal = new Spinner<>(0, 9999, barang.getJumlahTotal()); spinnerTotal.setEditable(true);
        Spinner<Integer> spinnerTersedia = new Spinner<>(0, 9999, barang.getJumlahTersedia()); spinnerTersedia.setEditable(true);
        
        ComboBox<String> cbKondisi = new ComboBox<>(); cbKondisi.getItems().addAll("baik", "rusak ringan", "rusak berat");
        cbKondisi.setValue(barang.getKondisiBarang());
        
        ComboBox<String> cbStatus = new ComboBox<>(); cbStatus.getItems().addAll("tersedia", "dipinjam", "rusak", "hilang");
        cbStatus.setValue(barang.getStatus());

        // --- BAGIAN FOTO (EDIT) ---
        ImageView imgPreview = new ImageView();
        imgPreview.setFitWidth(100); imgPreview.setFitHeight(100); imgPreview.setPreserveRatio(true);
        imgPreview.setStyle("-fx-border-color: #ccc; -fx-border-style: dashed;");
        
        // Load foto lama jika ada
        if (barang.getFotoUrl() != null) {
            try {
                File oldFile = new File(System.getProperty("user.dir") + barang.getFotoUrl());
                if(oldFile.exists()) imgPreview.setImage(new Image(oldFile.toURI().toString()));
            } catch(Exception e){}
        }

        Button btnGantiFoto = new Button("📷 Ganti Foto");
        Label lblStatusFoto = new Label("Menggunakan foto lama");
        lblStatusFoto.setStyle("-fx-font-size: 10px; -fx-text-fill: grey;");

        btnGantiFoto.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Gambar", "*.jpg", "*.png", "*.jpeg"));
            File f = fc.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (f != null) {
                this.tempFileFoto = f;
                imgPreview.setImage(new Image(f.toURI().toString()));
                lblStatusFoto.setText("Foto baru dipilih: " + f.getName());
            }
        });

        HBox photoBox = new HBox(10, imgPreview, new VBox(5, btnGantiFoto, lblStatusFoto));
        photoBox.setAlignment(Pos.CENTER_LEFT);

        // --- SUSUN LAYOUT ---
        content.getChildren().addAll(
            new Label("Jumlah Total:"), spinnerTotal,
            new Label("Jumlah Tersedia:"), spinnerTersedia,
            new Label("Kondisi:"), cbKondisi,
            new Label("Status:"), cbStatus,
            new Separator(),
            new Label("Foto Barang:"), photoBox
        );

        dialog.getDialogPane().setContent(content);
        
        ButtonType btnDelete = new ButtonType("🗑️ Hapus Barang", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(btnDelete, ButtonType.OK, ButtonType.CANCEL);

        // Styling Delete Button
        Button delBtn = (Button) dialog.getDialogPane().lookupButton(btnDelete);
        delBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.setResultConverter(btn -> {
            if (btn == btnDelete) {
                handleDeleteBarangFromDialog(barang); // Panggil method hapus yang sudah ada
                return null;
            }
            if (btn == ButtonType.OK) {
                if (spinnerTersedia.getValue() > spinnerTotal.getValue()) {
                    AlertUtil.showWarning("Validasi", "Stok Tersedia tidak boleh > Total!");
                    return null;
                }
                barang.setJumlahTotal(spinnerTotal.getValue());
                barang.setJumlahTersedia(spinnerTersedia.getValue());
                barang.setKondisiBarang(cbKondisi.getValue());
                barang.setStatus(cbStatus.getValue());
                return barang;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedBarang -> {
            // PROSES UPLOAD FOTO BARU (Jika ada)
            if (this.tempFileFoto != null) {
                String newPath = uploadFoto(this.tempFileFoto, updatedBarang.getKodeBarang());
                updatedBarang.setFotoUrl(newPath);
            }

            if (barangDAO.update(updatedBarang)) {
                AlertUtil.showSuccess("Berhasil", "Barang berhasil diupdate!");
                loadAllBarang();
                applyFiltersAndDisplay();
            } else {
                AlertUtil.showError("Gagal", "Gagal mengupdate barang!");
            }
        });
    }

    /**
     * ✅ NEW: Handler untuk hapus barang dari dialog edit
     */
    private void handleDeleteBarangFromDialog(Barang barang) {
        // Konfirmasi hapus
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Barang: " + barang.getNamaBarang());
        confirmAlert.setContentText(
            "Apakah Anda yakin ingin menghapus barang ini?\n\n" +
            "Kode: " + barang.getKodeBarang() + "\n" +
            "Nama: " + barang.getNamaBarang() + "\n\n" +
            "⚠️ Tindakan ini tidak dapat dibatalkan!"
        );
        
        // Custom button
        ButtonType btnYes = new ButtonType("Ya, Hapus", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(btnYes, btnNo);
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == btnYes) {
                // Cek apakah barang sedang dipinjam
                if (isBarangDipinjam(barang.getKodeBarang())) {
                    AlertUtil.showError("Tidak Dapat Dihapus", 
                        "Barang sedang dipinjam!\n" +
                        "Tidak dapat menghapus barang yang masih dalam peminjaman.");
                    return;
                }
                
                // Hapus dari database
                if (barangDAO.delete(barang.getKodeBarang())) {
                    AlertUtil.showSuccess("Berhasil", 
                        "Barang \"" + barang.getNamaBarang() + "\" berhasil dihapus!");
                    
                    // Reload data
                    loadAllBarang();
                    applyFiltersAndDisplay();
                } else {
                    AlertUtil.showError("Gagal", "Gagal menghapus barang!");
                }
            }
        });
    }

    /**
     * ✅ NEW: Cek apakah barang sedang dipinjam
     */
    private boolean isBarangDipinjam(String kodeBarang) {
        try {
            java.sql.Connection conn = com.inventaris.config.DatabaseConfig.getInstance().getConnection();
            String sql = "SELECT COUNT(*) FROM borrow " +
                         "WHERE kode_barang = ? AND status_barang = 'dipinjam'";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, kodeBarang);
            java.sql.ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                conn.close();
                return count > 0;
            }
            
            rs.close();
            stmt.close();
            conn.close();
            return false;
            
        } catch (Exception e) {
            System.err.println("Error checking barang status: " + e.getMessage());
            e.printStackTrace();
            return true; // Anggap dipinjam jika error (untuk safety)
        }
    }

   private void showAddToCartDialog(Barang barang) {
        Dialog<CartItem> dialog = new Dialog<>();
        dialog.setTitle("Tambah Keranjang");
        dialog.setHeaderText(barang.getNamaBarang());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        HBox boxJumlah = new HBox(10);
        boxJumlah.getChildren().add(new Label("Jumlah:"));
        Spinner<Integer> spinner = new Spinner<>(1, barang.getJumlahTersedia(), 1);
        spinner.setEditable(true);
        boxJumlah.getChildren().add(spinner);
        boxJumlah.setAlignment(Pos.CENTER_LEFT);

        DatePicker dpPinjam = new DatePicker(LocalDate.now());
        DatePicker dpKembali = new DatePicker(LocalDate.now().plusDays(7));
        
        HBox boxPinjam = new HBox(10);
        boxPinjam.getChildren().addAll(new Label("Tgl Pinjam: "), dpPinjam);
        
        HBox boxKembali = new HBox(10);
        boxKembali.getChildren().addAll(new Label("Tgl Kembali:"), dpKembali);
        
        VBox dateBox = new VBox(10);
        dateBox.getChildren().addAll(boxPinjam, boxKembali);

        content.getChildren().addAll(boxJumlah, dateBox);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                CartItem item = new CartItem();
                item.setBarang(barang);
                item.setJumlahPinjam(spinner.getValue());
                item.setTglPinjam(dpPinjam.getValue());
                item.setTglKembali(dpKembali.getValue());
                return item;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(this::addToCart);
    }

    private void addToCart(CartItem item) {
        if (!item.isValid()) {
            AlertUtil.showWarning("Validasi", item.getValidationError());
            return;
        }
        if (CartManager.getInstance().hasBarang(item.getBarang().getIdBarang())) {
            AlertUtil.showWarning("Info", "Barang sudah ada di keranjang.");
            return;
        }
        CartManager.getInstance().addItem(item);
        updateCartBadge();
        AlertUtil.showSuccess("Sukses", "Masuk keranjang!");
    }

    @FXML
    public void handlePeminjaman() {
        if (LayoutController.getInstance() != null) {
            LayoutController.getInstance().handlePeminjaman();
        }
    }
    
    private void updateCartBadge() {
        if (cartBadge != null) {
            int count = CartManager.getInstance().getCart().size();
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisible(count > 0);
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            currentContent = loader.load();
            contentArea.getChildren().add(currentContent);
        } catch (IOException e) {
            System.err.println("❌ Gagal memuat halaman: " + fxmlPath);
            e.printStackTrace();
        }
    }
    
    private File tempFileFoto = null;
    /**
     * Helper: Copy file foto ke folder project dan return path database
     */
    private String uploadFoto(File file, String kodeBarang) {
        if (file == null) return null;
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
            if (i > 0) ext = originalName.substring(i);
            
            String newFileName = kodeBarang + "_" + System.currentTimeMillis() + ext;

            // 4. Copy file
            java.nio.file.Files.copy(file.toPath(), uploadDir.resolve(newFileName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 5. Return path relatif
            return "/images/barang/" + newFileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @FXML 
    private void handleApplyFilter() { 
        applyFiltersAndDisplay(); 
    }
}