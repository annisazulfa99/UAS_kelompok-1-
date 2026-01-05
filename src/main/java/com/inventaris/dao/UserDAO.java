// ================================================================
    // File: src/main/java/com/inventaris/dao/UserDAO.java
    // ================================================================
    package com.inventaris.dao;

    import com.inventaris.config.DatabaseConfig;
    import com.inventaris.model.User;
    import org.mindrot.jbcrypt.BCrypt;

    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.sql.Statement;
    import java.util.ArrayList;
    import java.util.List;

    /**
     * UserDAO - Data Access Object for User
     * Handles all database operations related to User 
     * * MODIFIED: Menyimpan password sebagai PLAIN TEXT (Sesuai Permintaan)
     */
    public class UserDAO {

        private final DatabaseConfig dbConfig;

        /**
         * Constructor
         */
        public UserDAO() {
            this.dbConfig = DatabaseConfig.getInstance();
        }

        /**
         * Authenticate user login
         * 
         * @param username Username
         * @param password Password (plain text)
         * @return User object if authenticated, null otherwise
         */
        public User authenticate(String username, String password) {
            String sql = "SELECT * FROM user WHERE username = ? AND status = 'aktif'";

            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    // Cek 1: Apakah password di DB sama persis dengan input? (Plain Text)
                if (password.equals(storedPassword)) {
                    return extractUserFromResultSet(rs);
                }
                // Cek 2: Apakah password di DB ter-enkripsi BCrypt? (Support data lama)
                else if (storedPassword.startsWith("$2") && BCrypt.checkpw(password, storedPassword)) {
                    return extractUserFromResultSet(rs);
                    }
                }

            } catch (SQLException e) {
                System.err.println("Error authenticating user: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Register new user with role
         * 
         * @param user User object
         * @param noTelepon Phone number (for peminjam)
         * @param namaInstansi Institution name (for instansi)
         * @return true if successful, false otherwise
         */
        public boolean register(User user, String noTelepon, String namaInstansi) {
            Connection conn = null;

            try {
                conn = dbConfig.getConnection();
                conn.setAutoCommit(false);

                // Insert user
                String sqlUser = "INSERT INTO user (username, password, nama, role, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);

                stmtUser.setString(1, user.getUsername());
                stmtUser.setString(2, user.getPassword());
                stmtUser.setString(3, user.getNama());
                stmtUser.setString(4, user.getRole());
                stmtUser.setString(5, user.getStatus());

                int affected = stmtUser.executeUpdate();

                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                // Get generated user ID
                ResultSet rs = stmtUser.getGeneratedKeys();
                int userId = 0;
                if (rs.next()) {
                    userId = rs.getInt(1);
                }

                // Insert role-specific table
                boolean roleInserted = false;

                if ("peminjam".equals(user.getRole())) {
                    String sqlPeminjam = "INSERT INTO peminjam (id_user, no_telepon) VALUES (?, ?)";
                    PreparedStatement stmtPeminjam = conn.prepareStatement(sqlPeminjam);
                    stmtPeminjam.setInt(1, userId);
                    stmtPeminjam.setString(2, noTelepon);
                    roleInserted = stmtPeminjam.executeUpdate() > 0;
                    stmtPeminjam.close();

                } else if ("admin".equals(user.getRole())) {
                    String sqlAdmin = "INSERT INTO admin (id_user) VALUES (?)";
                    PreparedStatement stmtAdmin = conn.prepareStatement(sqlAdmin);
                    stmtAdmin.setInt(1, userId);
                    roleInserted = stmtAdmin.executeUpdate() > 0;
                    stmtAdmin.close();

                } else if ("instansi".equals(user.getRole())) {
                    String sqlInstansi = "INSERT INTO instansi (id_user, nama_instansi) VALUES (?, ?)";
                    PreparedStatement stmtInstansi = conn.prepareStatement(sqlInstansi);
                    stmtInstansi.setInt(1, userId);
                    stmtInstansi.setString(2, namaInstansi);
                    roleInserted = stmtInstansi.executeUpdate() > 0;
                    stmtInstansi.close();
                }

                if (roleInserted) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }

            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                System.err.println("Error registering user: " + e.getMessage());
                e.printStackTrace();
                return false;

            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
 * Register new user with role and kategori (for instansi)
 * 
 * @param user User object
 * @param noTelepon Phone number (for peminjam)
 * @param namaInstansi Institution name (for instansi)
 * @param kategori Category of instansi (Himpunan/UKM)
 * @return true if successful, false otherwise
 */
public boolean registerWithKategori(User user, String noTelepon, String namaInstansi, String kategori) {
    Connection conn = null;

    try {
        conn = dbConfig.getConnection();
        conn.setAutoCommit(false);

        // Insert user
        String sqlUser = "INSERT INTO user (username, password, nama, role, status) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);

        stmtUser.setString(1, user.getUsername());
        stmtUser.setString(2, user.getPassword());
        stmtUser.setString(3, user.getNama());
        stmtUser.setString(4, user.getRole());
        stmtUser.setString(5, user.getStatus());

        int affected = stmtUser.executeUpdate();

        if (affected == 0) {
            conn.rollback();
            return false;
        }

        // Get generated user ID
        ResultSet rs = stmtUser.getGeneratedKeys();
        int userId = 0;
        if (rs.next()) {
            userId = rs.getInt(1);
        }

        // Insert role-specific table
        boolean roleInserted = false;

        if ("peminjam".equals(user.getRole())) {
            String sqlPeminjam = "INSERT INTO peminjam (id_user, no_telepon) VALUES (?, ?)";
            PreparedStatement stmtPeminjam = conn.prepareStatement(sqlPeminjam);
            stmtPeminjam.setInt(1, userId);
            stmtPeminjam.setString(2, noTelepon);
            roleInserted = stmtPeminjam.executeUpdate() > 0;
            stmtPeminjam.close();

        } else if ("admin".equals(user.getRole())) {
            String sqlAdmin = "INSERT INTO admin (id_user) VALUES (?)";
            PreparedStatement stmtAdmin = conn.prepareStatement(sqlAdmin);
            stmtAdmin.setInt(1, userId);
            roleInserted = stmtAdmin.executeUpdate() > 0;
            stmtAdmin.close();

        } else if ("instansi".equals(user.getRole())) {
            // ✅ PERBEDAAN UTAMA: Insert dengan kategori
            String sqlInstansi = "INSERT INTO instansi (id_user, nama_instansi, kategori) VALUES (?, ?, ?)";
            PreparedStatement stmtInstansi = conn.prepareStatement(sqlInstansi);
            stmtInstansi.setInt(1, userId);
            stmtInstansi.setString(2, namaInstansi);
            stmtInstansi.setString(3, kategori); // ✅ BARIS INI YANG BARU!
            roleInserted = stmtInstansi.executeUpdate() > 0;
            stmtInstansi.close();
        }

        if (roleInserted) {
            conn.commit();
            return true;
        } else {
            conn.rollback();
            return false;
        }

    } catch (SQLException e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        System.err.println("Error registering user with kategori: " + e.getMessage());
        e.printStackTrace();
        return false;

    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
        
        /**
         * Get user by ID
         * 
         * @param id User ID
         * @return User object if found, null otherwise
         */
        public User getUserById(int id) {
            String sql = "SELECT * FROM user WHERE id_user = ?";

            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }

            } catch (SQLException e) {
                System.err.println("Error getting user: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Get user by username
         * 
         * @param username Username
         * @return User object if found, null otherwise
         */
        public User getUserByUsername(String username) {
            String sql = "SELECT * FROM user WHERE username = ?";

            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }

            } catch (SQLException e) {
                System.err.println("Error getting user by username: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Get all users
         * 
         * @return List of all users
         */
        public List<User> getAllUsers() {
            List<User> users = new ArrayList<>();
            String sql = "SELECT * FROM user ORDER BY created_at DESC";

            try (Connection conn = dbConfig.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }

            } catch (SQLException e) {
                System.err.println("Error getting all users: " + e.getMessage());
                e.printStackTrace();
            }

            return users;
        }

    /**
     * Get users by role
     * 
     * @param role User role (admin, peminjam, instansi)
     * @return List of users with specified role
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = ? ORDER BY created_at DESC";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting users by role: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Update user information
     * 
     * @param user User object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE user SET username = ?, nama = ?, status = ? WHERE id_user = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername()); // Parameter 1: username baru
            stmt.setString(2, user.getNama());     // Parameter 2: nama baru
            stmt.setString(3, user.getStatus());   // Parameter 3: status (tetap diperlukan untuk fitur toggle)
            stmt.setInt(4, user.getIdUser());      // Parameter 4: id_user (kunci pencarian)
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Change user password
     * 
     * @param userId User ID
     * @param newPassword New password (plain text)
     * @return true if successful, false otherwise
     */
    public boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE user SET password = ? WHERE id_user = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete user
     * 
     * @param userId User ID
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM user WHERE id_user = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if username exists
     * 
     * @param username Username to check
     * @return true if exists, false otherwise
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    /**
     * Extract User object from ResultSet
     * 
     * @param rs ResultSet
     * @return User object
     * @throws SQLException if error occurs
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNama(rs.getString("nama"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
    
    /**
     * Hash password using BCrypt
     * 
     * @param password Plain text password
     * @return Hashed password
     */
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}