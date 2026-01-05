// ================================================================
// File: src/main/java/com/inventaris/controller/LoginController.java
// ================================================================
package com.inventaris.controller;

import com.inventaris.Main;
import com.inventaris.dao.UserDAO;
import com.inventaris.model.User;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import com.inventaris.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

/**
 * LoginController - Handle login functionality
 */
public class LoginController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Hyperlink registerLink;
    
    private final UserDAO userDAO = new UserDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Hide error label initially
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
        
        // Hide progress indicator
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        
        // Focus on username field
        if (usernameField != null) {
            usernameField.requestFocus();
        }
        
        // Enter key to login
        if (passwordField != null) {
            passwordField.setOnKeyPressed(this::handleKeyPress);
        }
        
        if (usernameField != null) {
            usernameField.setOnKeyPressed(this::handleKeyPress);
        }
        
        System.out.println("✅ Login screen initialized");
    }
    
    /**
     * Handle key press (Enter to login)
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        // Reset error message
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
        
        // Get input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (ValidationUtil.isEmpty(username)) {
            showError("Username tidak boleh kosong!");
            usernameField.requestFocus();
            return;
        }
        
        if (ValidationUtil.isEmpty(password)) {
            showError("Password tidak boleh kosong!");
            passwordField.requestFocus();
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Authenticate in background thread
        new Thread(() -> {
            try {
                // Simulate slight delay for better UX
                Thread.sleep(500);
                
                User user = userDAO.authenticate(username, password);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    
                    if (user != null) {
                        handleLoginSuccess(user);
                    } else {
                        showError("Username atau password salah!");
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    showError("Terjadi kesalahan saat login!");
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    /**
     * Handle successful login
     */
   private void handleLoginSuccess(User user) {
    try {
        Integer roleId = getRoleId(user);
        sessionManager.login(user, roleId);

        LogActivityUtil.logLogin(user.getUsername(), user.getRole());
        System.out.println("✅ Login successful: " + user.getUsername() + " (" + user.getRole() + ")");

        // Navigate ke layout utama
        Main.showMainLayout();

        System.gc();

    } catch (Exception e) {
        showError("Terjadi kesalahan saat membuka halaman utama!");
        e.printStackTrace();
    }
}

    
    /**
     * Get role-specific ID (id_peminjam, id_admin, id_instansi)
     */
    private Integer getRoleId(User user) {
        try {
            Connection conn = com.inventaris.config.DatabaseConfig.getInstance().getConnection();
            String sql = "";
            String idColumn = "";
            
            switch (user.getRole()) {
                case "peminjam":
                    sql = "SELECT id_peminjam FROM peminjam WHERE id_user = ?";
                    idColumn = "id_peminjam";
                    break;
                case "admin":
                    sql = "SELECT id_admin FROM admin WHERE id_user = ?";
                    idColumn = "id_admin";
                    break;
                case "instansi":
                    sql = "SELECT id_instansi FROM instansi WHERE id_user = ?";
                    idColumn = "id_instansi";
                    break;
                default:
                    return null;
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getIdUser());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int roleId = rs.getInt(idColumn);
                conn.close();
                return roleId;
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error getting role ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Handle register link click
     */
    @FXML
    private void handleRegister() {
        AlertUtil.showInfo(
            "Registrasi",
            "Untuk registrasi akun baru, silakan hubungi administrator."
        );
    }
    
    /**
     * Handle forgot password link
     */
    @FXML
    private void handleForgotPassword() {
        AlertUtil.showInfo(
            "Lupa Password",
            "Untuk reset password, silakan hubungi administrator."
        );
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }
    
    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        if (loginButton != null) {
            loginButton.setDisable(loading);
        }
        if (usernameField != null) {
            usernameField.setDisable(loading);
        }
        if (passwordField != null) {
            passwordField.setDisable(loading);
        }
        if (progressIndicator != null) {
            progressIndicator.setVisible(loading);
        }
    }
}