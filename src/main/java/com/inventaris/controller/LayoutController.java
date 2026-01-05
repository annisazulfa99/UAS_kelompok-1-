package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.model.User;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * LayoutController v3.0 - FIXED: Tambah Dashboard untuk Instansi
 */
public class LayoutController implements Initializable {

    @FXML private Circle profileImage;
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnLapor;
    @FXML private Button btnPeminjaman;
    @FXML private Button btnLaporan;
    @FXML private Button btnBarang;
    @FXML private Button btnUser;
    @FXML private Button btnBerita;
    
    private String lastLoadedFxml = "/fxml/Home.fxml";
    
    private final String DEFAULT_STYLE = "-fx-background-color: #D9CBC1; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;";
    private final String ACTIVE_STYLE = "-fx-background-color: #8C6E63; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 16px;";
    
    @FXML private TextField txtSearch;
    @FXML public StackPane contentArea;
    private static LayoutController instance;
    
    private final SessionManager sessionManager = SessionManager.getInstance();
    private Parent currentContent;

    public static LayoutController getInstance() {
        return instance;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        
        System.out.println("🔧 LayoutController v3.0 initializing...");
        
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ No user session!");
            AlertUtil.showError("Error", "Session tidak valid!");
            handleLogout();
            return;
        }

        welcomeLabel.setText("Halo, " + currentUser.getNama());
        roleLabel.setText(getRoleDisplayName(currentUser.getRole()));
        
        System.out.println("👤 User: " + currentUser.getNama() + " (" + currentUser.getRole() + ")");

        configureMenuByRole(currentUser.getRole());

        System.out.println("🎯 Loading DEFAULT page: Home.fxml");
        handleHome();
        
        System.out.println("✅ LayoutController initialized");
    }

    @FXML
    public void handleHome() {
        System.out.println("📂 handleHome() → Loading Home.fxml");
        try {
            setActiveMenu(null);
            loadPage("/fxml/Home.fxml");
        } catch (Exception e) {
            System.err.println("❌ Error in handleHome(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboard() {
        System.out.println("📂 handleDashboard() → Loading Dashboard.fxml");
        setActiveMenu(btnDashboard);
        loadPage("/fxml/Dashboard.fxml");
    }

    @FXML
    public void handlePeminjaman() {
        System.out.println("📂 handlePeminjaman() - Checking role...");
        setActiveMenu(btnPeminjaman);
        
        String role = sessionManager.getCurrentRole();
        
        if ("peminjam".equalsIgnoreCase(role)) {
            System.out.println("👤 Peminjam → Loading PeminjamanPeminjam.fxml");
            loadPage("/fxml/PeminjamanPeminjam.fxml");
            
        } else if ("instansi".equalsIgnoreCase(role)) {
            System.out.println("🏢 Instansi → Loading PeminjamanInstansi.fxml");
            loadPage("/fxml/PeminjamanInstansi.fxml");
            
        } else {
            System.out.println("👑 Admin → Loading Peminjaman.fxml");
            loadPage("/fxml/Peminjaman.fxml");
        }
    }

    @FXML
    private void handleLaporan() {
        System.out.println("📂 handleLaporan()");
        setActiveMenu(btnLaporan);
        loadPage("/fxml/LaporanPeminjam.fxml");
    }
    
    @FXML
    private void handleLapor() {
        System.out.println("📂 handleLapor()");
        setActiveMenu(btnLapor);
        loadPage("/fxml/LaporanAdmin.fxml");
    }

    @FXML
    public void handleBarang() {
        System.out.println("📂 handleBarang()");
        setActiveMenu(btnBarang);
        loadPage("/fxml/DataBarang.fxml");
    }

    @FXML
    private void handleUser() {
        System.out.println("📂 handleUser()");
        setActiveMenu(btnUser);
        loadPage("/fxml/User.fxml");
    }

    @FXML
    private void handleBerita() {
        System.out.println("📂 handleBerita()");
        setActiveMenu(btnBerita);
        loadPage("/fxml/Berita-view.fxml");
    }

    public void updateContentArea(Parent newContent) {
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
            System.out.println("✅ Content area updated");
        }
    }

    public void setActiveBarangMenu() {
        setActiveMenu(btnBarang);
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) return;

        System.out.println("🔍 Searching: " + keyword);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DataBarang.fxml"));
            Parent page = loader.load();
            
            Object controller = loader.getController();
            
            if (controller instanceof DataBarangPeminjamController) {
                ((DataBarangPeminjamController) controller).searchBarang(keyword);
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
            setActiveMenu(btnBarang);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Gagal melakukan pencarian.");
        }
    }

    private void resetAllMenus() {
        if (btnDashboard != null) btnDashboard.setStyle(DEFAULT_STYLE);
        if (btnPeminjaman != null) btnPeminjaman.setStyle(DEFAULT_STYLE);
        if (btnLaporan != null) btnLaporan.setStyle(DEFAULT_STYLE);
        if (btnBarang != null) btnBarang.setStyle(DEFAULT_STYLE);
        if (btnUser != null) btnUser.setStyle(DEFAULT_STYLE);
        if (btnBerita != null) btnBerita.setStyle(DEFAULT_STYLE);
        if (btnLapor != null) btnLapor.setStyle(DEFAULT_STYLE);
    }

    private void setActiveMenu(Button activeButton) {
        resetAllMenus();
        if (activeButton != null) {
            activeButton.setStyle(ACTIVE_STYLE);
        }
    }

    @FXML
    private void handleLogout() {
        if (AlertUtil.showLogoutConfirmation()) {
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null) {
                LogActivityUtil.logLogout(currentUser.getUsername(), currentUser.getRole());
            }
            
            sessionManager.logout();
            
            try {
                Main.showLoginScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Refreshing: " + lastLoadedFxml);
        loadPage(lastLoadedFxml);
        
        if (txtSearch != null) {
            txtSearch.clear();
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            this.lastLoadedFxml = fxmlPath;

            System.out.println("📂 Loading: " + fxmlPath);
            
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            currentContent = loader.load();
            contentArea.getChildren().add(currentContent);
            
            System.out.println("✅ Loaded successfully: " + fxmlPath);
            
        } catch (IOException e) {
            System.err.println("❌ Failed to load: " + fxmlPath);
            e.printStackTrace();
            
            AlertUtil.showError("Error", 
                "Gagal memuat halaman!\n" +
                "File: " + fxmlPath + "\n" +
                "Error: " + e.getMessage()
            );
        }
    }

    /**
     * ✅ FIXED v3.0: Instansi sekarang memiliki menu Dashboard
     */
    private void configureMenuByRole(String role) {
        // Reset semua menu dulu
        if (btnBarang != null) { btnBarang.setVisible(false); btnBarang.setManaged(false); }
        if (btnPeminjaman != null) { btnPeminjaman.setVisible(false); btnPeminjaman.setManaged(false); }
        if (btnLaporan != null) { btnLaporan.setVisible(false); btnLaporan.setManaged(false); }
        if (btnUser != null) { btnUser.setVisible(false); btnUser.setManaged(false); }
        if (btnLapor != null) { btnLapor.setVisible(false); btnLapor.setManaged(false); }
        if (btnBerita != null) { btnBerita.setVisible(false); btnBerita.setManaged(false); }
        if (btnDashboard != null) { btnDashboard.setVisible(false); btnDashboard.setManaged(false); }

        switch (role) {
            case "admin":
                System.out.println("🔧 Configuring menu for ADMIN");
                
                if (btnDashboard != null) { 
                    btnDashboard.setVisible(true); 
                    btnDashboard.setManaged(true);
                    btnDashboard.setText("Dashboard");
                    System.out.println("   ✅ Dashboard - VISIBLE");
                }
                
                if (btnLapor != null) { 
                    btnLapor.setVisible(true); 
                    btnLapor.setManaged(true);
                    System.out.println("   ✅ Laporan - VISIBLE");
                }
                
                if (btnUser != null) { 
                    btnUser.setVisible(true); 
                    btnUser.setManaged(true);
                    System.out.println("   ✅ User Management - VISIBLE");
                }
                
                if (btnBerita != null) { 
                    btnBerita.setVisible(true); 
                    btnBerita.setManaged(true);
                    System.out.println("   ✅ Berita - VISIBLE");
                }
                break;
                
            case "peminjam":
                System.out.println("🔧 Configuring menu for PEMINJAM");
                
                if (btnDashboard != null) { 
                    btnDashboard.setVisible(true); 
                    btnDashboard.setManaged(true);
                    System.out.println("   ✅ Dashboard - VISIBLE");
                }
                
                if (btnBarang != null) { 
                    btnBarang.setVisible(true); 
                    btnBarang.setManaged(true);
                    System.out.println("   ✅ Data Barang - VISIBLE");
                }
                
                if (btnPeminjaman != null) { 
                    btnPeminjaman.setVisible(true); 
                    btnPeminjaman.setManaged(true);
                    System.out.println("   ✅ Kelola Peminjaman - VISIBLE");
                }
                
                if (btnLaporan != null) { 
                    btnLaporan.setVisible(true); 
                    btnLaporan.setManaged(true);
                    System.out.println("   ✅ Laporan - VISIBLE");
                }
                break;
                
            case "instansi":
                System.out.println("🔧 Configuring menu for INSTANSI");
                
                // ✅ NEW: Dashboard untuk Instansi
                if (btnDashboard != null) { 
                    btnDashboard.setVisible(true); 
                    btnDashboard.setManaged(true);
                    btnDashboard.setText("Dashboard");
                    System.out.println("   ✅ Dashboard - VISIBLE");
                }
                
                if (btnBarang != null) { 
                    btnBarang.setVisible(true); 
                    btnBarang.setManaged(true);
                    System.out.println("   ✅ Data Barang - VISIBLE");
                }
                
                if (btnPeminjaman != null) { 
                    btnPeminjaman.setVisible(true); 
                    btnPeminjaman.setManaged(true);
                    btnPeminjaman.setText("Kelola Peminjaman");
                    System.out.println("   ✅ Kelola Peminjaman - VISIBLE");
                }
                
                // Menu yang disembunyikan
                if (btnLaporan != null) { 
                    btnLaporan.setVisible(false); 
                    btnLaporan.setManaged(false);
                    System.out.println("   ❌ Laporan - HIDDEN");
                }
                
                if (btnLapor != null) { 
                    btnLapor.setVisible(false); 
                    btnLapor.setManaged(false);
                    System.out.println("   ❌ Lapor - HIDDEN");
                }
                
                if (btnUser != null) { 
                    btnUser.setVisible(false); 
                    btnUser.setManaged(false);
                    System.out.println("   ❌ User - HIDDEN");
                }
                
                if (btnBerita != null) { 
                    btnBerita.setVisible(false); 
                    btnBerita.setManaged(false);
                    System.out.println("   ❌ Berita - HIDDEN");
                }
                
                break;
        }
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin": return "Administrator";
            case "peminjam": return "Peminjam";
            case "instansi": return "Instansi";
            default: return role;
        }
    }
}