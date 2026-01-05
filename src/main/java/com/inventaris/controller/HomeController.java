package com.inventaris.controller;

import com.inventaris.dao.BorrowDAO;
import com.inventaris.dao.InstansiDAO;
import com.inventaris.model.Instansi;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

/**
 * HomeController v3.0 - Dashboard dengan Grafik Peminjaman per Instansi
 * 
 * FITUR:
 * - Menampilkan grafik batang untuk setiap instansi
 * - Layout 3 grafik per baris
 * - Auto-update ketika instansi ditambah/dihapus
 * - Menampilkan persentase dan jumlah peminjaman per barang
 */
public class HomeController implements Initializable {
    
    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane chartContainer;
    
    private final InstansiDAO instansiDAO = new InstansiDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 HomeController (Dashboard) initializing...");
        
        // Setup container
        if (chartContainer == null) {
            chartContainer = new FlowPane();
            chartContainer.setHgap(20);
            chartContainer.setVgap(20);
            chartContainer.setPadding(new Insets(30));
            chartContainer.setAlignment(Pos.TOP_CENTER);
            
            if (scrollPane != null) {
                scrollPane.setContent(chartContainer);
            }
        }
        
        // Load dashboard
        loadDashboard();
        
        System.out.println("✅ HomeController initialized");
    }
    
    /**
     * Load semua grafik instansi
     */
    public void loadDashboard() {
        System.out.println("📊 Loading dashboard...");
        
        chartContainer.getChildren().clear();
        
        // Ambil semua instansi
        List<Instansi> instansiList = instansiDAO.getAll();
        
        if (instansiList.isEmpty()) {
            showNoDataMessage();
            return;
        }
        
        // Ambil statistik peminjaman per instansi
        Map<Integer, Map<String, Integer>> stats = borrowDAO.getStatistikPeminjamanPerInstansi();
        
        // Buat grafik untuk setiap instansi
        for (Instansi instansi : instansiList) {
            Map<String, Integer> dataBarang = stats.get(instansi.getIdInstansi());
            
            // Skip jika tidak ada data peminjaman
            if (dataBarang == null || dataBarang.isEmpty()) {
                continue;
            }
            
            VBox chartBox = createChartForInstansi(instansi, dataBarang);
            chartContainer.getChildren().add(chartBox);
        }
        
        // Jika tidak ada data sama sekali
        if (chartContainer.getChildren().isEmpty()) {
            showNoDataMessage();
        }
        
        System.out.println("✅ Dashboard loaded with " + chartContainer.getChildren().size() + " charts");
    }
    
    /**
     * Create grafik untuk satu instansi
     */
    private VBox createChartForInstansi(Instansi instansi, Map<String, Integer> dataBarang) {
        VBox container = new VBox(15);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3); " +
            "-fx-padding: 20;"
        );
        container.setPrefWidth(380);
        container.setMaxWidth(380);
        container.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Label title = new Label(instansi.getNamaInstansi());
        title.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #6A5436;"
        );
        
        Label subtitle = new Label("Total Barang yang Pernah Dipinjam");
        subtitle.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #999;"
        );
        
        // Hitung total BARANG yang pernah dipinjam (SUM dari semua)
        int totalBarangDipinjam = dataBarang.values().stream().mapToInt(Integer::intValue).sum();
        
        Label totalLabel = new Label("Total: " + totalBarangDipinjam + " barang");
        totalLabel.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #8C6E63;"
        );
        
        // Create bar chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Jenis Barang");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Jumlah Total Dipinjam");
        yAxis.setTickUnit(1);
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(300);
        barChart.setPrefWidth(340);
        barChart.setAnimated(true);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Sort data berdasarkan jumlah peminjaman (descending)
        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(dataBarang.entrySet());
        sortedData.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // Ambil max 5 barang teratas
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedData) {
            if (count >= 5) break;
            
            String namaBarang = entry.getKey();
            int jumlah = entry.getValue();
            
            // Truncate nama barang jika terlalu panjang
            String displayName = namaBarang.length() > 15 
                ? namaBarang.substring(0, 15) + "..." 
                : namaBarang;
            
            XYChart.Data<String, Number> data = new XYChart.Data<>(displayName, jumlah);
            series.getData().add(data);
            
            count++;
        }
        
        barChart.getData().add(series);
        
        // Style bars
        barChart.setStyle("-fx-bar-fill: #8C6E63;");
        
        // Info detail untuk setiap barang
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label infoTitle = new Label("Detail Peminjaman:");
        infoTitle.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #6A5436;"
        );
        infoBox.getChildren().add(infoTitle);
        
        for (Map.Entry<String, Integer> entry : sortedData) {
            String namaBarang = entry.getKey();
            int jumlahTotal = entry.getValue();
            double persentase = (jumlahTotal * 100.0) / totalBarangDipinjam;
            
            Label infoLabel = new Label(String.format("• %s: %d unit (%.1f%%)", 
                namaBarang, jumlahTotal, persentase));
            infoLabel.setStyle(
                "-fx-font-size: 11px; " +
                "-fx-text-fill: #666;"
            );
            infoBox.getChildren().add(infoLabel);
        }
        
        // Add all to container
        container.getChildren().addAll(
            title,
            subtitle,
            totalLabel,
            barChart,
            infoBox
        );
        
        return container;
    }
    
    /**
     * Show message when no data available
     */
    private void showNoDataMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(100));
        messageBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        
        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 72px;");
        
        Label title = new Label("Belum Ada Data Peminjaman");
        title.setStyle(
            "-fx-font-size: 24px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #6A5436;"
        );
        
        Label subtitle = new Label("Grafik akan muncul setelah ada riwayat peminjaman barang dari instansi");
        subtitle.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #999; " +
            "-fx-wrap-text: true; " +
            "-fx-text-alignment: center;"
        );
        subtitle.setMaxWidth(400);
        
        messageBox.getChildren().addAll(icon, title, subtitle);
        chartContainer.getChildren().add(messageBox);
    }
    
    /**
     * Refresh dashboard (dipanggil dari luar jika diperlukan)
     */
    public void refresh() {
        loadDashboard();
    }
}