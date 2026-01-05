package com.inventaris.controller;

import com.inventaris.dao.BorrowDAO;
import com.inventaris.dao.BarangDAO;
import com.inventaris.model.Borrow;
import com.inventaris.model.CartItem;
import com.inventaris.util.AlertUtil;
import com.inventaris.util.CartManager;
import com.inventaris.util.LogActivityUtil;
import com.inventaris.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PeminjamanPeminjamController v2.0
 * UPDATED: Dialog pengembalian dengan 3 kondisi (Baik, Rusak, Hilang)
 */
public class PeminjamanPeminjamController implements Initializable {
    
    // --- UI ELEMENTS ---
    @FXML private ToggleButton tabPengajuan;
    @FXML private ToggleButton tabSedang;
    @FXML private ToggleButton tabRiwayat;
    
    @FXML private VBox contentPengajuan;
    @FXML private VBox contentSedang;
    @FXML private VBox contentRiwayat;
    
    // --- TAB 1: PENGAJUAN (KERANJANG) ---
    @FXML private TextField txtLampiranSurat; 
    @FXML private Label lblTotalItems;
    @FXML private TableView<CartItemDisplay> tablePengajuan;
    @FXML private TableColumn<CartItemDisplay, Integer> colPengajuanNo;
    @FXML private TableColumn<CartItemDisplay, String> colPengajuanNama;
    @FXML private TableColumn<CartItemDisplay, String> colPengajuanPemilik;
    @FXML private TableColumn<CartItemDisplay, Integer> colPengajuanJumlah;
    @FXML private TableColumn<CartItemDisplay, LocalDate> colPengajuanTglPinjam;
    @FXML private TableColumn<CartItemDisplay, LocalDate> colPengajuanTglKembali;
    @FXML private TableColumn<CartItemDisplay, Void> colPengajuanAction;
    
    // --- TAB 2: SEDANG DIPINJAM (AKTIF) ---
    @FXML private TableView<Borrow> tableSedang;
    @FXML private TableColumn<Borrow, Integer> colSedangId;
    @FXML private TableColumn<Borrow, String> colSedangNama;
    @FXML private TableColumn<Borrow, String> colSedangPemilik;
    @FXML private TableColumn<Borrow, Integer> colSedangJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colSedangTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colSedangDeadline;
    @FXML private TableColumn<Borrow, Long> colSedangSisa;
    @FXML private TableColumn<Borrow, Void> colSedangAction;
    
    // --- TAB 3: RIWAYAT ---
    @FXML private TableView<Borrow> tableRiwayat;
    @FXML private TableColumn<Borrow, Integer> colRiwayatId;
    @FXML private TableColumn<Borrow, String> colRiwayatNama;
    @FXML private TableColumn<Borrow, String> colRiwayatPemilik;
    @FXML private TableColumn<Borrow, Integer> colRiwayatJumlah;
    @FXML private TableColumn<Borrow, LocalDate> colRiwayatTglPinjam;
    @FXML private TableColumn<Borrow, LocalDate> colRiwayatTglKembali;
    @FXML private TableColumn<Borrow, String> colRiwayatStatus;

    // --- LOGIC TOOLS ---
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final BarangDAO barangDAO = new BarangDAO();
    private List<CartItem> cart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔧 PeminjamanPeminjamController v2.0 initializing...");
        
        this.cart = CartManager.getInstance().getCart();
        
        setupPengajuanTable();
        setupSedangTable();
        setupRiwayatTable();
        
        if (tableSedang != null) {
            tableSedang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        if (tableRiwayat != null) {
            tableRiwayat.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        
        loadPengajuanData();
        showPengajuanTab();
        
        System.out.println("✅ PeminjamanPeminjamController initialized");
    }

    // ============================================================
    // NAVIGATION TABS
    // ============================================================
    
    @FXML
    private void handleTabChange() {
        if (tabPengajuan.isSelected()) {
            showPengajuanTab();
        } else if (tabSedang.isSelected()) {
            showSedangTab();
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
    
    private void showRiwayatTab() {
        setVisibleTab(contentRiwayat, tabRiwayat);
        loadRiwayatData();
    }

    private void setVisibleTab(VBox content, ToggleButton tab) {
        contentPengajuan.setVisible(false); contentPengajuan.setManaged(false);
        contentSedang.setVisible(false); contentSedang.setManaged(false);
        contentRiwayat.setVisible(false); contentRiwayat.setManaged(false);
        
        String inactiveStyle = "-fx-background-color: #D9CBC1; -fx-text-fill: black; -fx-cursor: hand;";
        tabPengajuan.setStyle(inactiveStyle);
        tabSedang.setStyle(inactiveStyle);
        tabRiwayat.setStyle(inactiveStyle);
        
        if (content != null) { content.setVisible(true); content.setManaged(true); }
        if (tab != null) { tab.setStyle("-fx-background-color: #8C6E63; -fx-text-fill: white; -fx-font-weight: bold;"); }
    }
    
    // ============================================================
    // TABLE SETUP
    // ============================================================
    
    private void setupSedangTable() {
        setKolomNomor(colSedangId);
        colSedangNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        
        // Setup kolom Pemilik (NEW)
        if(colSedangPemilik != null) {
            colSedangPemilik.setCellValueFactory(cellData -> {
                String pemilik = cellData.getValue().getNamaInstansi();
                return new javafx.beans.property.SimpleStringProperty(
                    pemilik != null && !pemilik.isEmpty() ? pemilik : "Umum"
                );
            });
        }
        
        colSedangJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colSedangTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colSedangDeadline.setCellValueFactory(new PropertyValueFactory<>("dlKembali"));
        
        // Status dengan kondisi
        colSedangSisa.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSisaHari())
        );

        colSedangSisa.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Long sisaHari, boolean empty) {
                super.updateItem(sisaHari, empty);
                
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    Borrow b = getTableView().getItems().get(getIndex());
                    String status = b.getStatusApproval();

                    if ("pending_instansi".equalsIgnoreCase(status)) {
                        setText("⏳ Menunggu Persetujuan");
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold; -fx-font-style: italic;");
                        
                    } else if ("approved_instansi".equalsIgnoreCase(status)) {
                        if (sisaHari == null) sisaHari = 0L;
                        
                        if (sisaHari < 0) {
                            setText("⚠️ Terlambat (" + Math.abs(sisaHari) + " hari)");
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        } else {
                            setText("✅ Disetujui (Sisa " + sisaHari + " hari)");
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        }
                    } else if ("pending_return".equalsIgnoreCase(status)) {
                        setText("🔄 Menunggu Verifikasi");
                        setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    } else {
                        setText(status);
                        setStyle("");
                    }
                }
            }
        });

        // Tombol Kembalikan
        colSedangAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnKembali = new Button("Kembalikan");
            {
                btnKembali.setStyle("-fx-background-color: #6A5436; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                btnKembali.setOnAction(e -> handleKembalikan(getTableView().getItems().get(getIndex())));
            }
            
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { 
                    setGraphic(null); 
                } else {
                    Borrow b = getTableView().getItems().get(getIndex());
                    
                    // Tombol hanya muncul jika sudah disetujui (approved_instansi)
                    if ("approved_instansi".equalsIgnoreCase(b.getStatusApproval())) {
                        setGraphic(btnKembali);
                    } else {
                        setGraphic(null);
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    // ============================================================
    // TAB 1: LOGIKA PENGAJUAN (CART)
    // ============================================================

    @FXML
    private void handleSubmitPengajuan() {
        if (cart.isEmpty()) {
            AlertUtil.showWarning("Keranjang Kosong", "Silakan pilih barang dulu.");
            return;
        }

        if (txtLampiranSurat != null && txtLampiranSurat.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validasi", "Harap lampirkan keterangan/surat!");
            return;
        }

        if (!AlertUtil.showConfirmation("Konfirmasi", "Ajukan peminjaman " + cart.size() + " barang ini?")) return;

        Integer peminjamId = sessionManager.getCurrentRoleId();
        if (peminjamId == null) return;
        String isiSurat = (txtLampiranSurat != null) ? txtLampiranSurat.getText().trim() : "-";
        int successCount = 0;
        for (CartItem item : cart) {
            Borrow borrow = new Borrow();
            borrow.setIdPeminjam(peminjamId);
            borrow.setKodeBarang(item.getBarang().getKodeBarang());
            borrow.setJumlahPinjam(item.getJumlahPinjam());
            borrow.setTglPeminjaman(LocalDate.now());
            borrow.setTglPinjam(item.getTglPinjam());
            borrow.setDlKembali(item.getTglKembali());
            borrow.setStatusBarang("pending");
            borrow.setStatusApproval("pending_instansi");
            
            // --- TAMBAHAN: SIMPAN SURAT KE OBJECT BORROW ---
            borrow.setKeperluan(isiSurat); 
            // -----------------------------------------------

            if (borrowDAO.create(borrow)) {
                successCount++;
                LogActivityUtil.logCreate(sessionManager.getCurrentUsername(), sessionManager.getCurrentRole(), "peminjaman", item.getBarang().getNamaBarang());
            }
        }

        if (successCount > 0) {
            AlertUtil.showSuccess("Berhasil", successCount + " barang diajukan.\nMenunggu persetujuan instansi.");
            cart.clear();
            CartManager.getInstance().clearCart();
            if (txtLampiranSurat != null) txtLampiranSurat.clear();
            loadPengajuanData();
            
            tabSedang.setSelected(true);
            showSedangTab();
        } else {
            AlertUtil.showError("Gagal", "Terjadi kesalahan.");
        }
    }

    private void loadPengajuanData() {
        ObservableList<CartItemDisplay> displayItems = FXCollections.observableArrayList();
        AtomicInteger counter = new AtomicInteger(1);
        for (CartItem item : cart) {
            displayItems.add(new CartItemDisplay(counter.getAndIncrement(), item));
        }
        tablePengajuan.setItems(displayItems);
        if (lblTotalItems != null) lblTotalItems.setText(cart.size() + " barang");
    }

    private void setupPengajuanTable() {
        colPengajuanNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colPengajuanNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        colPengajuanPemilik.setCellValueFactory(new PropertyValueFactory<>("pemilik"));
        colPengajuanJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colPengajuanTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colPengajuanTglKembali.setCellValueFactory(new PropertyValueFactory<>("tglKembali"));
        
        colPengajuanAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnDelete = new Button("❌");
            {
                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand; -fx-font-weight: bold;");
                btnDelete.setOnAction(e -> {
                    CartItemDisplay item = getTableView().getItems().get(getIndex());
                    CartManager.getInstance().removeItem(item.getOriginalItem());
                    loadPengajuanData();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
                setAlignment(Pos.CENTER);
            }
        });
    }

    // ============================================================
    // TAB 2: LOGIKA SEDANG DIPINJAM (ACTIVE)
    // ============================================================

    private void loadSedangData() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) return;
            
            List<Borrow> borrows = borrowDAO.getByPeminjamId(peminjamId);
            // Filter: pending_instansi, approved_instansi, pending_return
            borrows.removeIf(b -> 
                "approved_return".equals(b.getStatusApproval()) || 
                "rejected_instansi".equals(b.getStatusApproval()) ||
                "rejected_return".equals(b.getStatusApproval())
            );
            
            tableSedang.setItems(FXCollections.observableArrayList(borrows));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ UPDATED v2.0: Dialog pengembalian dengan 3 kondisi + kuantitas
     */
    private void handleKembalikan(Borrow borrow) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Kembalikan Barang");
        dialog.setHeaderText("Kembalikan: " + borrow.getNamaBarang() + "\nTotal Dipinjam: " + borrow.getJumlahPinjam() + " unit");
        
        // Form fields
        Label lblBaik = new Label("Jumlah Kondisi BAIK:");
        TextField txtBaik = new TextField("0");
        txtBaik.setPromptText("Jumlah baik");
        
        Label lblRusak = new Label("Jumlah Kondisi RUSAK:");
        TextField txtRusak = new TextField("0");
        txtRusak.setPromptText("Jumlah rusak");
        
        Label lblHilang = new Label("Jumlah Kondisi HILANG:");
        TextField txtHilang = new TextField("0");
        txtHilang.setPromptText("Jumlah hilang");
        
        Label lblTotal = new Label("Total: 0 unit");
        lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
        
        Label lblCatatan = new Label("Catatan (opsional):");
        TextArea txtCatatan = new TextArea();
        txtCatatan.setPromptText("Jelaskan kondisi barang yang rusak/hilang...");
        txtCatatan.setPrefRowCount(3);
        txtCatatan.setWrapText(true);
        
        // Validation listener
        Runnable validateTotal = () -> {
            try {
                int baik = Integer.parseInt(txtBaik.getText().isEmpty() ? "0" : txtBaik.getText());
                int rusak = Integer.parseInt(txtRusak.getText().isEmpty() ? "0" : txtRusak.getText());
                int hilang = Integer.parseInt(txtHilang.getText().isEmpty() ? "0" : txtHilang.getText());
                int total = baik + rusak + hilang;
                
                lblTotal.setText("Total: " + total + " / " + borrow.getJumlahPinjam() + " unit");
                
                if (total == borrow.getJumlahPinjam()) {
                    lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
                } else {
                    lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                }
            } catch (NumberFormatException e) {
                lblTotal.setText("Total: Invalid");
                lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
            }
        };
        
        txtBaik.textProperty().addListener((obs, old, val) -> validateTotal.run());
        txtRusak.textProperty().addListener((obs, old, val) -> validateTotal.run());
        txtHilang.textProperty().addListener((obs, old, val) -> validateTotal.run());
        
        // Layout
        VBox content = new VBox(10);
        content.getChildren().addAll(
            lblBaik, txtBaik,
            lblRusak, txtRusak,
            lblHilang, txtHilang,
            new Separator(),
            lblTotal,
            new Separator(),
            lblCatatan, txtCatatan
        );
        content.setPrefWidth(400);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int baik = Integer.parseInt(txtBaik.getText().isEmpty() ? "0" : txtBaik.getText());
                    int rusak = Integer.parseInt(txtRusak.getText().isEmpty() ? "0" : txtRusak.getText());
                    int hilang = Integer.parseInt(txtHilang.getText().isEmpty() ? "0" : txtHilang.getText());
                    int total = baik + rusak + hilang;
                    
                    // Validasi 1: Total harus sama
                    if (total != borrow.getJumlahPinjam()) {
                        AlertUtil.showWarning("Validasi Gagal", 
                            "Total kuantitas (" + total + ") tidak sama dengan jumlah pinjam (" + borrow.getJumlahPinjam() + ")!");
                        return;
                    }
                    
                    // Validasi 2: Catatan wajib jika rusak/hilang
                    String catatan = txtCatatan.getText().trim();
                    if ((rusak > 0 || hilang > 0) && catatan.isEmpty()) {
                        AlertUtil.showWarning("Catatan Wajib", 
                            "Harap berikan catatan untuk barang yang rusak/hilang!");
                        return;
                    }
                    
                    // Submit pengembalian (status_approval = pending_return)
                    if (borrowDAO.submitReturn(borrow.getIdPeminjaman(), baik, rusak, hilang, catatan)) {
                        AlertUtil.showSuccess("Berhasil", 
                            "Pengembalian berhasil diajukan!\n" +
                            "Menunggu verifikasi dari instansi.\n\n" +
                            "Detail:\n" +
                            "- Baik: " + baik + " unit\n" +
                            "- Rusak: " + rusak + " unit\n" +
                            "- Hilang: " + hilang + " unit");
                        
                        LogActivityUtil.log(
                            sessionManager.getCurrentUsername(),
                            "Mengajukan pengembalian: " + borrow.getNamaBarang(),
                            "RETURN_REQUEST",
                            sessionManager.getCurrentRole()
                        );
                        
                        loadSedangData();
                    } else {
                        AlertUtil.showError("Gagal", "Gagal mengajukan pengembalian!");
                    }
                    
                } catch (NumberFormatException e) {
                    AlertUtil.showError("Input Error", "Jumlah harus berupa angka!");
                }
            }
        });
    }

    // ============================================================
    // TAB 3: LOGIKA RIWAYAT
    // ============================================================

    private void loadRiwayatData() {
        try {
            Integer peminjamId = sessionManager.getCurrentRoleId();
            if (peminjamId == null) return;
            
            List<Borrow> borrows = borrowDAO.getByPeminjamId(peminjamId);
            // Hanya yang sudah selesai
            borrows.removeIf(b -> 
                "pending_instansi".equals(b.getStatusApproval()) ||
                "approved_instansi".equals(b.getStatusApproval()) ||
                "pending_return".equals(b.getStatusApproval())
            );
            
            tableRiwayat.setItems(FXCollections.observableArrayList(borrows));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupRiwayatTable() {
        setKolomNomor(colRiwayatId);
        colRiwayatNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));
        
        // Setup kolom Pemilik (NEW)
        if(colRiwayatPemilik != null) {
            colRiwayatPemilik.setCellValueFactory(cellData -> {
                String pemilik = cellData.getValue().getNamaInstansi();
                return new javafx.beans.property.SimpleStringProperty(
                    pemilik != null && !pemilik.isEmpty() ? pemilik : "Umum"
                );
            });
        }
        
        colRiwayatJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlahPinjam"));
        colRiwayatTglPinjam.setCellValueFactory(new PropertyValueFactory<>("tglPinjam"));
        colRiwayatTglKembali.setCellValueFactory(new PropertyValueFactory<>("tglKembali"));
        colRiwayatStatus.setCellValueFactory(new PropertyValueFactory<>("statusApproval"));
        
        // Status dengan warna
        colRiwayatStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setStyle("");
                } else {
                    Borrow b = getTableView().getItems().get(getIndex());
                    setText(b.getStatusText());
                    
                    if ("approved_return".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if ("rejected_instansi".equalsIgnoreCase(status) || "rejected_return".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #b45309;");
                    }
                }
            }
        });
    }

    // ============================================================
    // INNER CLASS
    // ============================================================
    public static class CartItemDisplay {
        private final int no;
        private final String namaBarang;
        private final String pemilik;
        private final int jumlah;
        private final LocalDate tglPinjam;
        private final LocalDate tglKembali;
        private final CartItem originalItem;

        public CartItemDisplay(int no, CartItem item) {
            this.no = no;
            this.namaBarang = item.getBarang().getNamaBarang();
            this.pemilik = item.getBarang().getNamaPemilik() != null ? item.getBarang().getNamaPemilik() : "Umum";
            this.jumlah = item.getJumlahPinjam();
            this.tglPinjam = item.getTglPinjam();
            this.tglKembali = item.getTglKembali();
            this.originalItem = item;
        }
        
        public int getNo() { return no; }
        public String getNamaBarang() { return namaBarang; }
        public String getPemilik() { return pemilik; }
        public int getJumlah() { return jumlah; }
        public LocalDate getTglPinjam() { return tglPinjam; }
        public LocalDate getTglKembali() { return tglKembali; }
        public CartItem getOriginalItem() { return originalItem; }
    }
    
    /**
     * Helper untuk membuat kolom nomor urut otomatis
     */
   /**
     * Helper untuk mengubah kolom ID menjadi Nomor Urut Otomatis
     */
    private void setKolomNomor(TableColumn<Borrow, Integer> column) {
        if (column == null) return;
        
        // --- TAMBAHAN BARU: UBAH JUDUL HEADER ---
        column.setText("No"); 
        // ----------------------------------------
        
        column.setSortable(false); // Nomor urut tidak perlu di-sort
        
        // Dummy value factory (hanya agar cell tidak kosong/null)
        column.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(null)); 
        
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    // Logika Nomor: Index baris + 1
                    setText(String.valueOf(getIndex() + 1));
                    setAlignment(Pos.CENTER); // Rata tengah
                }
            }
        });
    }
}