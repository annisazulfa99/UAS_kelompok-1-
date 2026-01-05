package com.inventaris.controller;

import com.inventaris.dao.BarangDAO;
import com.inventaris.dao.BeritaDAO;
import com.inventaris.model.Barang;
import com.inventaris.model.Berita;
import com.inventaris.model.User;
import com.inventaris.util.SessionManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * DashboardController - Controller untuk Home.fxml
 * Menampilkan BERITA + REKOMENDASI BARANG (untuk Peminjam)
 * 
 * CATATAN: Nama file dan controller memang TERTUKAR, tapi jangan diubah!
 * - File FXML: Home.fxml
 * - Controller: DashboardController.java
 * - Fungsi: Menampilkan Berita (semua role kecuali Instansi) + Rekomendasi (hanya Peminjam)
 */
public class DashboardController implements Initializable {

    // --- BERITA CONTAINER ---
    @FXML private HBox beritaContainer;
    @FXML private VBox sectionBerita;
    // --- REKOMENDASI BARANG CONTAINER ---
    @FXML private VBox rekomendasiSection;
    @FXML private HBox rekomendasiContainer;

    // --- DAOs & Utilities ---
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BarangDAO barangDAO = new BarangDAO();
    private final BeritaDAO beritaDAO = new BeritaDAO();
    
    private Timeline refreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 DashboardController (Home.fxml) initializing...");
        
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ No user logged in!");
            return;
        }

        // 1. Load Berita (untuk Admin & Peminjam, BUKAN Instansi)
        if (beritaContainer != null) {
            if (!sessionManager.isInstansi()) {
                System.out.println("📰 Loading berita untuk: " + currentUser.getRole());
                loadBerita();
            } else {
                System.out.println("🚫 Instansi - Berita disembunyikan");
                beritaContainer.setVisible(false);
                beritaContainer.setManaged(false);
                sectionBerita.setVisible(false);
                sectionBerita.setManaged(false);
                
            }
        }

        // 2. Load Rekomendasi Barang (HANYA untuk Peminjam)
        if (rekomendasiSection != null) {
            if (sessionManager.isPeminjam()) {
                System.out.println("🎯 Loading rekomendasi barang untuk Peminjam");
                loadRekomendasiBarang();
            } else {
                System.out.println("🚫 " + currentUser.getRole() + " - Rekomendasi disembunyikan");
                rekomendasiSection.setVisible(false);
                rekomendasiSection.setManaged(false);
            }
        }

        // 3. Auto Refresh (setiap 30 detik)
        startAutoRefresh();

        System.out.println("✅ DashboardController initialized");
    }

    // =========================================================
    // LOGIKA BERITA
    // =========================================================

    private void loadBerita() {
    try {
        // UBAH DISINI: Ganti angka 3 menjadi 4
        List<Berita> beritaList = beritaDAO.getLatestBerita(3);
        
        System.out.println("📊 Berita loaded: " + beritaList.size() + " items");
        
        beritaContainer.getChildren().clear();
        
        // Jarak antar kartu. 
        // Kalau 4 kartu dirasa terlalu renggang, kurangi angka ini (misal jadi 10 atau 15)
        beritaContainer.setSpacing(15); 
        
        beritaContainer.setAlignment(Pos.CENTER); // Posisi tengah layar
        // Tambahkan padding kiri-kanan container utama supaya tidak nempel dinding layar banget
        beritaContainer.setPadding(new javafx.geometry.Insets(0, 20, 0, 20)); 
        
        if (beritaList.isEmpty()) {
            Label noBerita = new Label("Belum ada berita");
            noBerita.setStyle("-fx-font-size: 18px; -fx-text-fill: #6A5436;");
            beritaContainer.getChildren().add(noBerita);
            return;
        }
        
        for (Berita berita : beritaList) {
            VBox beritaCard = createBeritaCard(berita);
            beritaContainer.getChildren().add(beritaCard);
        }
        
        System.out.println("✅ Berita cards created successfully");
        
    } catch (Exception e) {
        System.err.println("❌ Error loading berita: " + e.getMessage());
        e.printStackTrace();
    }
}

    private VBox createBeritaCard(Berita berita) {
    VBox card = new VBox();
    card.setAlignment(Pos.CENTER); // Isi rata tengah

    // 1. SETUP UKURAN FIXED
    double fixedWidth = 280; 
    card.setPrefWidth(fixedWidth);
    card.setMinWidth(fixedWidth);
    card.setMaxWidth(fixedWidth);
    card.setPrefHeight(260); 

    // 2. SIMPAN STYLE DASAR KE VARIABLE
    // (Penting agar hover effect tidak merusak style saat mouse keluar masuk)
    String baseStyle = 
        "-fx-background-color: " + berita.getWarnaBackground() + ";" +
        "-fx-background-radius: 15;" +
        "-fx-padding: 20;" +
        "-fx-cursor: hand;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"; // Shadow dipertegas dikit

    card.setStyle(baseStyle);
    card.setSpacing(15); // Jarak antara Judul dan Isi diperlebar sedikit biar lega

    // Tentukan warna teks otomatis
    String textColor = isLightColor(berita.getWarnaBackground()) ? "#4A3B2A" : "white";

    // --- JUDUL BERITA ---
    Label lblJudul = new Label(berita.getJudul());
    lblJudul.setStyle(
        "-fx-font-size: 20px;" +
        "-fx-font-family: 'Segoe UI', sans-serif;" + // Pakai font bagus
        "-fx-font-weight: 800;" + // Lebih tebal (Bold)
        "-fx-text-fill: " + textColor + ";" +
        "-fx-text-alignment: center;"
    );
    lblJudul.setWrapText(true);
    lblJudul.setMaxWidth(fixedWidth - 40);
    // Jika judul kepanjangan, maksimal 2 baris saja, sisanya ...
    // lblJudul.setMaxHeight(60); 

    // --- ISI BERITA (PERBAIKAN UTAMA DISINI) ---
    // Jangan pakai getShortDeskripsi(60)! Pakai full getDeskripsi()
    // Karena kita sudah punya wrapText, dia akan turun sendiri.
    Label lblPreview = new Label(berita.getDeskripsi()); 
    
    lblPreview.setStyle(
        "-fx-font-size: 14px;" +
        "-fx-font-family: 'Segoe UI', sans-serif;" +
        "-fx-text-fill: " + textColor + ";" +
        "-fx-text-alignment: center;" +
        "-fx-line-spacing: 3px;" // Jarak antar baris biar gampang dibaca
    );
    lblPreview.setWrapText(true);
    lblPreview.setMaxWidth(fixedWidth - 40);
    
    // Agar teks mengisi ruang kosong tapi tidak menabrak bawah
    lblPreview.setMaxHeight(Double.MAX_VALUE);
    lblPreview.setAlignment(Pos.TOP_CENTER);

    card.getChildren().addAll(lblJudul, lblPreview);

    // Event Klik
    card.setOnMouseClicked(event -> showBeritaDetail(berita));

    // --- HOVER EFFECT YANG LEBIH AMAN ---
    // Menggunakan baseStyle + perubahan, bukan append string terus menerus
    card.setOnMouseEntered(e -> {
        card.setStyle(baseStyle + 
            "-fx-scale-x: 1.05; " + // Sedikit lebih besar (zoom)
            "-fx-scale-y: 1.05; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);" // Shadow makin tajam
        );
    });

    card.setOnMouseExited(e -> {
        card.setStyle(baseStyle); // Kembalikan ke style murni
    });

    return card;
}
    private boolean isLightColor(String hexColor) {
        try {
            String hex = hexColor.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
            return luminance > 0.5;
        } catch (Exception e) {
            return false;
        }
    }

    private void showBeritaDetail(Berita berita) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail Berita");
        alert.setHeaderText(berita.getJudul());
        alert.setContentText(berita.getDeskripsi());
        alert.getDialogPane().setMinWidth(500);
        alert.getDialogPane().setStyle("-fx-font-size: 14px;");
        alert.showAndWait();
    }

    // =========================================================
    // LOGIKA REKOMENDASI BARANG (HANYA PEMINJAM)
    // =========================================================

    private void loadRekomendasiBarang() {
        try {
            // Ambil 4 barang terbaru yang tersedia
            List<Barang> barangList = barangDAO.getAvailable()
                .stream()
                .limit(4)
                .collect(Collectors.toList());
            
            System.out.println("🎯 Rekomendasi loaded: " + barangList.size() + " items");
            
            rekomendasiContainer.getChildren().clear();
            rekomendasiContainer.setSpacing(20);
            rekomendasiContainer.setAlignment(Pos.CENTER);
            
            if (barangList.isEmpty()) {
                Label noBarang = new Label("Belum ada barang tersedia");
                noBarang.setStyle("-fx-font-size: 16px; -fx-text-fill: #6A5436;");
                rekomendasiContainer.getChildren().add(noBarang);
                return;
            }
            
            for (Barang barang : barangList) {
                VBox barangCard = createRekomendasiCard(barang);
                rekomendasiContainer.getChildren().add(barangCard);
            }
            
            System.out.println("✅ Rekomendasi cards created successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading rekomendasi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createRekomendasiCard(Barang barang) {
    VBox card = new VBox(10);
    card.setAlignment(Pos.CENTER);
    card.setPrefSize(220, 240);
    card.setMaxWidth(Double.MAX_VALUE);
    
    // Style awal
    card.setStyle(
        "-fx-background-color: #D9CBC1;" +
        "-fx-background-radius: 15;" +
        "-fx-padding: 15;" +
        "-fx-cursor: hand;"
    );
    
    // --- BAGIAN BARU: IMAGE HANDLING ---
    ImageView imageView = new ImageView();
    // Ukuran disesuaikan agar muat (sedikit lebih kecil dari card utama)
    imageView.setFitWidth(150); 
    imageView.setFitHeight(100); 
    imageView.setPreserveRatio(true);
    
    try {
        String path = barang.getFotoUrl();
        if (path != null && !path.isBlank()) {
            if (path.startsWith("http")) {
                imageView.setImage(new Image(path, true)); // true = background loading
            } else {
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is != null) imageView.setImage(new Image(is));
            }
        }
    } catch (Exception ignored) {}
    
    // Fallback Image jika gambar kosong/gagal load
    if (imageView.getImage() == null) {
        java.io.InputStream ph = getClass().getResourceAsStream("/images/barang/placeholder.png");
        if (ph != null) imageView.setImage(new Image(ph));
    }
    // -----------------------------------
    
    // Nama Barang
    Label namaLabel = new Label(barang.getNamaBarang());
    namaLabel.setStyle(
        "-fx-font-size: 16px;" + // Sedikit diperkecil agar rapi
        "-fx-font-weight: bold;" +
        "-fx-text-fill: #6A5436;" +
        "-fx-wrap-text: true;" +
        "-fx-text-alignment: center;"
    );
    namaLabel.setWrapText(true);
    namaLabel.setMaxWidth(200);
    
    // Info Stok
    Label stokLabel = new Label("Stok: " + barang.getJumlahTersedia());
    stokLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6A5436;");
    
    // Status
    Label statusLabel = new Label("Status: " + barang.getStatus());
    statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6A5436;");
    
    // Pemilik
    String pemilik = (barang.getNamaPemilik() != null && !barang.getNamaPemilik().isEmpty()) 
        ? barang.getNamaPemilik() 
        : "Lembaga";
    Label pemilikLabel = new Label("Pemilik: " + pemilik);
    pemilikLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6A5436;");
    
    // Masukkan imageView menggantikan imgPlaceholder
    card.getChildren().addAll(imageView, namaLabel, stokLabel, statusLabel, pemilikLabel);
    
    // Event klik → Pindah ke DataBarang dengan auto search
    card.setOnMouseClicked(event -> handleRekomendasiClick(barang.getNamaBarang()));
    
    // Hover effect (tetap sama)
    card.setOnMouseEntered(e -> {
        card.setStyle(
            "-fx-background-color: #C4B5A8;" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 15;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );
    });
    
    card.setOnMouseExited(e -> {
        card.setStyle(
            "-fx-background-color: #D9CBC1;" +
            "-fx-background-radius: 15;" +
            "-fx-padding: 15;" +
            "-fx-cursor: hand;"
        );
    });
    
    return card;
}

    /**
     * Handle klik rekomendasi → Pindah ke DataBarang dengan auto search
     */
    private void handleRekomendasiClick(String namaBarang) {
        System.out.println("🔍 Rekomendasi clicked: " + namaBarang);
        
        try {
            // Load halaman DataBarang
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DataBarang.fxml"));
            Parent page = loader.load();
            
            Object controller = loader.getController();
            
            // Trigger search jika controller adalah DataBarangPeminjamController
            if (controller instanceof DataBarangPeminjamController) {
                ((DataBarangPeminjamController) controller).searchBarang(namaBarang);
                System.out.println("✅ Search triggered: " + namaBarang);
            }
            
            // Update content area di LayoutController
            LayoutController layoutController = LayoutController.getInstance();
            if (layoutController != null) {
                layoutController.updateContentArea(page);
                layoutController.setActiveBarangMenu(); // Set menu "Data Barang" jadi aktif
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error handling rekomendasi click: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================
    // AUTO REFRESH
    // =========================================================

    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(
            Duration.seconds(30), // Refresh setiap 30 detik
            event -> {
                // Refresh berita (jika bukan instansi)
                if (beritaContainer != null && !sessionManager.isInstansi()) {
                    loadBerita();
                }
                
                // Refresh rekomendasi (jika peminjam)
                if (rekomendasiSection != null && sessionManager.isPeminjam()) {
                    loadRekomendasiBarang();
                }
            }
        ));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    public void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            System.out.println("🛑 Auto-refresh stopped");
        }
    }
}