// ================================================================
// File: src/main/java/com/inventaris/Main.java
// FIXED: Routing halaman awal setelah login → Home.fxml (Berita)
// ================================================================
package com.inventaris;

import com.inventaris.config.DatabaseConfig;
import com.inventaris.controller.DashboardController;
import com.inventaris.controller.PeminjamanController;
import com.inventaris.util.SessionManager;
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main Application Class
 * Entry point untuk aplikasi Inventaris Barang
 */
public class Main extends Application {
    @FXML private StackPane contentArea; 
    private static Stage primaryStage;
    private static Scene currentScene;
    private Parent currentContent;
    private static Object currentController;
    
    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;

            System.out.println("=================================");
            System.out.println("  SIMAK - INVENTARIS BARANG");
            System.out.println("  SISTEM MANAJEMEN INVENTARIS");
            System.out.println("=================================");

            DatabaseConfig dbConfig = DatabaseConfig.getInstance();
            if (dbConfig.testConnection()) {
                System.out.println("✅ Database connection successful");
            } else {
                System.err.println("❌ Database connection failed!");
                showErrorAndExit("Database connection failed!\nPlease check your database configuration.");
                return;
            }

            // Load login screen first
            showLoginScreen();

            primaryStage.setTitle("SIMAK - Inventaris Barang");
            primaryStage.setResizable(false);

            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
            } catch (Exception e) {
                System.out.println("⚠️ App icon not found, using default");
            }

            primaryStage.setOnCloseRequest(event -> {
                System.out.println("🔴 Application closing...");
                cleanupCurrentController();
            });

            primaryStage.show();

            System.out.println("✅ Application started successfully");
            System.out.println("=================================\n");

        } catch (Exception e) {
            System.err.println("❌ Failed to start application");
            e.printStackTrace();
            showErrorAndExit("Failed to start application!\n" + e.getMessage());
        }
    }

    /**
     * Show login screen
     */
    public static void showLoginScreen() {
        try {
            System.out.println("🔄 Loading Login screen...");
            
            cleanupCurrentController();
            
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
            
            currentScene = scene;
            currentController = loader.getController();
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("SIMAK - Login");
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            
            System.out.println("✅ Login screen loaded successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR loading Login screen:");
            e.printStackTrace();
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load Login Screen");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * ✅ Show dashboard (alias untuk showMainLayout untuk backward compatibility)
     */
    public static void showDashboard() {
        showMainLayout();
    }

    /**
     * ✅ FIXED: Show main layout setelah login
     * Langsung load Home.fxml (Berita + Rekomendasi) sebagai halaman default
     */
    public static void showMainLayout() {
        try {
            System.out.println("🔄 Loading Main Layout...");

            cleanupCurrentController();

            // Pilih layout berdasarkan role
            String role = SessionManager.getInstance().getCurrentRole();
            String fxmlPath = "/fxml/Layout.fxml"; // Default untuk Peminjam

            if ("admin".equalsIgnoreCase(role) || "instansi".equalsIgnoreCase(role)) {
                fxmlPath = "/fxml/Layout_2.fxml"; // Layout untuk Admin/Instansi
                System.out.println("🤖 Loading Admin/Instansi Layout");
            } else {
                System.out.println("👤 Loading Peminjam Layout");
            }

            // Load Layout FXML
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            
            URL cssUrl = Main.class.getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            currentScene = scene;
            currentController = loader.getController();

            primaryStage.setScene(scene);
            primaryStage.setTitle("SIMAK - Dashboard (" + role + ")");
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.sizeToScene(); 
            primaryStage.show();

            System.out.println("✅ Main layout loaded: " + fxmlPath);
            
            // ✅ PENTING: LayoutController.initialize() akan otomatis load Home.fxml
            // Tidak perlu manual load lagi di sini!

        } catch (Exception e) {
            System.err.println("❌ Failed to load main layout");
            e.printStackTrace();
        }
    }

    /**
     * Show any screen with custom size
     */
    public static void showScreen(String fxmlFile, String title, double width, double height) {
        try {
            cleanupCurrentController();
            
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
            
            currentScene = scene;
            currentController = loader.getController();
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("SIMAK - " + title);
            primaryStage.centerOnScreen();
            
            System.out.println("📄 Screen loaded: " + fxmlFile);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading screen: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Show screen with parameter (for search, etc)
     */
    public static void showScreenWithParam(String fxmlFile, String title, double width, double height, String param) {
        try {
            cleanupCurrentController();

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();

            // Check if controller has receiveSearchKeyword method
            try {
                controller.getClass().getMethod("receiveSearchKeyword", String.class)
                        .invoke(controller, param);
            } catch (NoSuchMethodException e) {
                System.out.println("Controller doesn't have receiveSearchKeyword method");
            }

            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());

            currentScene = scene;
            currentController = controller;

            primaryStage.setScene(scene);
            primaryStage.setTitle("SIMAK - " + title);
            primaryStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load content into existing layout (used by LayoutController)
     */
    public static void loadContent(String fxmlName) {
        try {
            cleanupCurrentController();
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/" + fxmlName));
            Parent content = loader.load();

            // If loading Home.fxml (Berita), create new scene
            if (fxmlName.equals("Home.fxml")) {
                Scene scene = new Scene(content, 1200, 700);
                scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
                currentScene = scene;
                primaryStage.setScene(scene);
                primaryStage.centerOnScreen();
                currentController = loader.getController();
                return;
            }

            // For other pages, insert into center of BorderPane
            if (currentScene != null && currentScene.getRoot() instanceof BorderPane) {
                BorderPane root = (BorderPane) currentScene.getRoot();
                root.setCenter(content);
            } else {
                Scene scene = new Scene(content, 1200, 700);
                currentScene = scene;
                primaryStage.setScene(scene);
            }

            currentController = loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Cleanup current controller (stop auto-refresh timelines)
     */
    private static void cleanupCurrentController() {
        if (currentController != null) {
            try {
                // Stop auto-refresh in DashboardController
                if (currentController instanceof DashboardController) {
                    ((DashboardController) currentController).stopAutoRefresh();
                    System.out.println("🛑 DashboardController auto-refresh stopped");
                }
                
                // Stop auto-refresh in PeminjamanController
                if (currentController instanceof PeminjamanController) {
                    ((PeminjamanController) currentController).stopAutoRefresh();
                    System.out.println("🛑 PeminjamanController auto-refresh stopped");
                }
                
            } catch (Exception e) {
                System.err.println("⚠️ Error cleaning up controller: " + e.getMessage());
            }
            
            currentController = null;
        }
    }
    
    /**
     * Get primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Get current scene
     */
    public static Scene getCurrentScene() {
        return currentScene;
    }
    
    /**
     * Get current controller
     */
    public static Object getCurrentController() {
        return currentController;
    }
    
    /**
     * Show error alert and exit
     */
    private void showErrorAndExit(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Error");
        alert.setHeaderText("Application Error");
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }
    
    /**
     * Load page into content area (for embedded loading)
     */
    public void loadPage(String fxmlPath) {
        try {
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            currentContent = loader.load();
            contentArea.getChildren().add(currentContent);
        } catch (IOException e) {
            System.err.println("❌ Failed to load page: " + fxmlPath);
            e.printStackTrace();
        }
    }
    
    /**
     * Application shutdown hook
     */
    @Override
    public void stop() {
        System.out.println("\n=================================");
        System.out.println("  APPLICATION SHUTTING DOWN");
        System.out.println("=================================");
        
        cleanupCurrentController();
        
        System.out.println("✅ Cleanup completed");
        System.out.println("👋 Goodbye!");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}