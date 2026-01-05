// ================================================================
// File: src/main/java/com/inventaris/util/AlertUtil.java
// ================================================================
package com.inventaris.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

/**
 * AlertUtil - Helper untuk menampilkan dialog dan alert
 */
public class AlertUtil {
    
    /**
     * Show success alert
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show error alert
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show warning alert
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info alert
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show confirmation dialog
     * @return true if user clicks OK, false if CANCEL
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show input dialog
     * @return user input or null if cancelled
     */
    public static String showInputDialog(String title, String header, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    /**
     * Show delete confirmation
     */
    public static boolean showDeleteConfirmation(String itemName) {
        return showConfirmation(
            "Konfirmasi Hapus",
            "Apakah Anda yakin ingin menghapus " + itemName + "?\nTindakan ini tidak dapat dibatalkan."
        );
    }
    
    /**
     * Show logout confirmation
     */
    public static boolean showLogoutConfirmation() {
        return showConfirmation(
            "Konfirmasi Logout",
            "Apakah Anda yakin ingin keluar dari aplikasi?"
        );
    }
}

