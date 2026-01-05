package com.inventaris.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Configuration Class
 * Mengelola koneksi ke MySQL Database
 */
public class DatabaseConfig {
    
    private static final String PROPERTIES_FILE = "/db.properties";
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;
    
    // Singleton instance
    private static DatabaseConfig instance;
    
    // Static block untuk load properties
    static {
        loadProperties();
        try {
            Class.forName(DB_DRIVER);
            System.out.println("✅ MySQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found!");
            e.printStackTrace();
        }
    }
    
    /**
     * Private constructor untuk Singleton pattern
     */
    private DatabaseConfig() {}
    
    /**
     * Get Singleton instance
     */
    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfig.class) {
                if (instance == null) {
                    instance = new DatabaseConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load database properties dari file
     */
    private static void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.err.println("❌ Unable to find " + PROPERTIES_FILE);
                // Fallback ke hardcoded values
                setDefaultProperties();
                return;
            }
            
            properties.load(input);
            DB_URL = properties.getProperty("db.url");
            DB_USERNAME = properties.getProperty("db.username");
            DB_PASSWORD = properties.getProperty("db.password");
            DB_DRIVER = properties.getProperty("db.driver");
            
            System.out.println("✅ Database properties loaded successfully");
            
        } catch (IOException e) {
            System.err.println("❌ Error loading database properties");
            e.printStackTrace();
            setDefaultProperties();
        }
    }
    
    /**
     * Set default properties jika file tidak ditemukan
     */
    private static void setDefaultProperties() {
        DB_URL = "jdbc:mysql://localhost:3306/myname_db";
        DB_USERNAME = "root";
        DB_PASSWORD = "123456789";
        DB_DRIVER = "com.mysql.cj.jdbc.Driver";
        System.out.println("⚠️  Using default database configuration");
    }
    
    /**
     * Mendapatkan koneksi database baru
     * 
     * @return Connection object
     * @throws SQLException jika koneksi gagal
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("✅ Database connection established");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to database");
            System.err.println("URL: " + DB_URL);
            System.err.println("Username: " + DB_USERNAME);
            throw e;
        }
    }
    
    /**
     * Test koneksi database
     * 
     * @return true jika koneksi berhasil
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Close connection dengan aman
     * 
     * @param conn Connection yang akan ditutup
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("✅ Database connection closed");
            } catch (SQLException e) {
                System.err.println("❌ Error closing connection: " + e.getMessage());
            }
        }
    }
    
    // Getters
    public static String getDbUrl() {
        return DB_URL;
    }
    
    public static String getDbUsername() {
        return DB_USERNAME;
    }
    
    // Untuk debugging
    public void printConfig() {
        System.out.println("=== Database Configuration ===");
        System.out.println("URL: " + DB_URL);
        System.out.println("Username: " + DB_USERNAME);
        System.out.println("Driver: " + DB_DRIVER);
        System.out.println("==============================");
    }
}