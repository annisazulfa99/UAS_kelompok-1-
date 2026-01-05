// ================================================================
// File: src/main/java/com/inventaris/model/Instansi.java
// ================================================================
package com.inventaris.model;

/**
 * Instansi Model Class
 * Represents an organization/institution in the system
 */
public class Instansi {
    
    // Attributes
    private int idInstansi;
    private int idUser;
    private String namaInstansi;
    private String kategori;

    // Related object
    private User user;
    
    // ============================================================
    // CONSTRUCTORS
    // ============================================================
    
    /**
     * Default constructor
     */
    public Instansi() {
        // Empty constructor
    }
    
    /**
     * Constructor with basic fields
     */
    public Instansi(int idUser, String namaInstansi) {
        this.idUser = idUser;
        this.namaInstansi = namaInstansi;
    }
    
    
    public String getKategori() {
    return kategori;
}

public void setKategori(String kategori) {
    this.kategori = kategori;
}

    /**
     * Constructor with all fields
     */
    public Instansi(int idInstansi, int idUser, String namaInstansi, String kategori) {
    this.idInstansi = idInstansi;
    this.idUser = idUser;
    this.namaInstansi = namaInstansi;
    this.kategori = kategori;
}

    
    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================
    
    public int getIdInstansi() {
        return idInstansi;
    }
    
    public void setIdInstansi(int idInstansi) {
        this.idInstansi = idInstansi;
    }
    
    public int getIdUser() {
        return idUser;
    }
    
    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
    
    public String getNamaInstansi() {
        return namaInstansi;
    }
    
    public void setNamaInstansi(String namaInstansi) {
        this.namaInstansi = namaInstansi;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // ============================================================
    // OVERRIDE METHODS
    // ============================================================
    
    @Override
    public String toString() {
        return "Instansi{" +
                "idInstansi=" + idInstansi +
                ", idUser=" + idUser +
                ", namaInstansi='" + namaInstansi + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Instansi instansi = (Instansi) obj;
        return idInstansi == instansi.idInstansi;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idInstansi);
    }
}