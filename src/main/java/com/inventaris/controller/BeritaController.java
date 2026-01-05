package com.inventaris.controller;

import com.inventaris.dao.BeritaDAO;
import com.inventaris.model.Berita;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * BeritaController - Admin Panel untuk Kelola Berita
 */
public class BeritaController implements Initializable {
    
    @FXML private TableView<Berita> beritaTable;
    @FXML private TableColumn<Berita, Integer> colId;
    @FXML private TableColumn<Berita, String> colJudul;
    @FXML private TableColumn<Berita, String> colDeskripsi;
    @FXML private TableColumn<Berita, String> colWarna;
    @FXML private TableColumn<Berita, String> colTanggal;
    @FXML private TableColumn<Berita, Void> colAction;
    
    private final BeritaDAO beritaDAO = new BeritaDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔄 BeritaController initializing...");
        
        // Check admin access
        if (!sessionManager.isAdmin()) {
            AlertUtil.showError("Access Denied", "Hanya admin yang dapat mengakses halaman ini!");
            return;
        }
        
        setupTable();
        
        System.out.println("📊 Loading berita from database...");
        loadAllBerita();
        
        System.out.println("✅ Berita Management initialized");
    }
    
    private void setupTable() {
        // ============================================================
        // UBAH BAGIAN INI: LOGIKA NOMOR URUT
        // ============================================================
        colId.setText("No"); // Ubah Header jadi "No"
        colId.setSortable(false); // Matikan sorting
        
        // Dummy factory agar cell tidak kosong
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getIdBerita()));
        
        // Cell Factory untuk generate nomor urut (1, 2, 3...)
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    // Nomor = Index Baris + 1
                    setText(String.valueOf(getIndex() + 1));
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        // ============================================================

        colJudul.setCellValueFactory(new PropertyValueFactory<>("judul"));
        
        // Deskripsi dipotong agar tidak terlalu panjang
        colDeskripsi.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getShortDeskripsi(50)
            )
        );
        
        colWarna.setCellValueFactory(new PropertyValueFactory<>("warnaBackground"));
        colTanggal.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedCreatedAt()
            )
        );
        
        // Preview warna di kolom warna
        colWarna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String warna, boolean empty) {
                super.updateItem(warna, empty);
                if (empty || warna == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(warna);
                    // Set background cell sesuai kode warna
                    setStyle("-fx-background-color: " + warna + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });
        
        // Action buttons
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button("👁️ Lihat");
            private final Button btnDelete = new Button("🗑️ Hapus");
            private final HBox buttons = new HBox(5, btnView, btnDelete);
            
            {
                btnView.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-cursor: hand;");
                btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
                
                btnView.setOnAction(e -> {
                    Berita berita = getTableView().getItems().get(getIndex());
                    viewBerita(berita);
                });
                
                btnDelete.setOnAction(e -> {
                    Berita berita = getTableView().getItems().get(getIndex());
                    deleteBerita(berita);
                });
                buttons.setAlignment(javafx.geometry.Pos.CENTER);
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
    
    private void loadAllBerita() {
        try {
            List<Berita> beritaList = beritaDAO.getAllBerita();
            ObservableList<Berita> observableList = FXCollections.observableArrayList(beritaList);
            beritaTable.setItems(observableList);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Gagal memuat data berita!");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddBerita() {
        Dialog<Berita> dialog = new Dialog<>();
        dialog.setTitle("Tambah Berita Baru");
        dialog.setHeaderText("Masukkan data berita");
        
        ButtonType btnSave = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField txtJudul = new TextField();
        txtJudul.setPromptText("Judul Berita");
        txtJudul.setPrefWidth(300);
        
        TextArea txtDeskripsi = new TextArea();
        txtDeskripsi.setPromptText("Deskripsi Berita");
        txtDeskripsi.setPrefRowCount(5);
        txtDeskripsi.setPrefWidth(300);
        txtDeskripsi.setWrapText(true);
        
        ComboBox<String> cmbWarna = new ComboBox<>();
        cmbWarna.getItems().addAll(
            "#D9696F", // Merah
            "#FFFFFF", // Putih
            "#C9C9C9", // Abu-abu
            "#6A9BD8", // Biru
            "#7BC96F", // Hijau
            "#FFD166"  // Kuning
        );
        cmbWarna.setValue("#D9696F");
        cmbWarna.setPrefWidth(150);
        
        grid.add(new Label("Judul:"), 0, 0);
        grid.add(txtJudul, 1, 0);
        grid.add(new Label("Deskripsi:"), 0, 1);
        grid.add(txtDeskripsi, 1, 1);
        grid.add(new Label("Warna:"), 0, 2);
        grid.add(cmbWarna, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSave) {
                if (txtJudul.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validasi", "Judul tidak boleh kosong!");
                    return null;
                }
                
                if (txtDeskripsi.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validasi", "Deskripsi tidak boleh kosong!");
                    return null;
                }
                
                Berita berita = new Berita(
                    txtJudul.getText().trim(),
                    txtDeskripsi.getText().trim(),
                    cmbWarna.getValue(),
                    sessionManager.getCurrentUser().getIdUser()
                );
                
                return berita;
            }
            return null;
        });
        
        Optional<Berita> result = dialog.showAndWait();
        
        result.ifPresent(berita -> {
            if (beritaDAO.addBerita(berita)) {
                AlertUtil.showSuccess("Berhasil", "Berita berhasil ditambahkan!");
                LogActivityUtil.log(
                    sessionManager.getCurrentUsername(),
                    "Tambah berita: " + berita.getJudul(),
                    "CREATE_BERITA",
                    sessionManager.getCurrentRole()
                );
                loadAllBerita();
            } else {
                AlertUtil.showError("Gagal", "Gagal menambahkan berita!");
            }
        });
    }
    
    private void viewBerita(Berita berita) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail Berita");
        alert.setHeaderText(berita.getJudul());
        alert.setContentText(berita.getDeskripsi() + "\n\n" +
                            "Dibuat oleh: " + berita.getNamaAdmin() + "\n" +
                            "Tanggal: " + berita.getFormattedCreatedAt());
        alert.showAndWait();
    }
    
    private void deleteBerita(Berita berita) {
        if (!AlertUtil.showConfirmation("Konfirmasi", 
                "Yakin ingin menghapus berita: " + berita.getJudul() + "?")) {
            return;
        }
        
        if (beritaDAO.deleteBerita(berita.getIdBerita())) {
            AlertUtil.showSuccess("Berhasil", "Berita berhasil dihapus!");
            LogActivityUtil.log(
                sessionManager.getCurrentUsername(),
                "Hapus berita: " + berita.getJudul(),
                "DELETE_BERITA",
                sessionManager.getCurrentRole()
            );
            loadAllBerita();
        } else {
            AlertUtil.showError("Gagal", "Gagal menghapus berita!");
        }
    }
}