package com.inventaris.util;

import com.inventaris.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    // 1. Instance statis (hanya ada satu di seluruh aplikasi)
    private static CartManager instance;
    
    // 2. List untuk menyimpan item
    private List<CartItem> cart = new ArrayList<>();

    // 3. Constructor private agar tidak bisa di-new sembarangan
    private CartManager() {}

    // 4. Method untuk mengambil instance (Singleton)
    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // --- OPERASI KERANJANG ---

    public List<CartItem> getCart() {
        return cart;
    }

    public void addItem(CartItem item) {
        cart.add(item);
    }

    public void removeItem(CartItem item) {
        cart.remove(item);
    }

    public void clearCart() {
        cart.clear();
    }

    // Cek apakah barang dengan ID tertentu sudah ada
    public boolean hasBarang(int idBarang) {
        return cart.stream()
                .anyMatch(c -> c.getBarang().getIdBarang() == idBarang);
    }
}