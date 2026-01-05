-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jan 05, 2026 at 01:35 AM
-- Server version: 8.0.43
-- PHP Version: 8.1.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `inventaris_barang`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_proses_peminjaman` (IN `p_id_peminjam` INT, IN `p_kode_barang` VARCHAR(50), IN `p_jumlah` INT, IN `p_tgl_pinjam` DATE, IN `p_dl_kembali` DATE)   BEGIN
    DECLARE v_tersedia INT;
    
    -- Cek ketersediaan
    SELECT jumlah_tersedia INTO v_tersedia 
    FROM barang 
    WHERE kode_barang = p_kode_barang;
    
    IF v_tersedia >= p_jumlah THEN
        -- Insert peminjaman
        INSERT INTO borrow (id_peminjam, kode_barang, jumlah_pinjam, tgl_peminjaman, tgl_pinjam, dl_kembali, status_barang)
        VALUES (p_id_peminjam, p_kode_barang, p_jumlah, CURDATE(), p_tgl_pinjam, p_dl_kembali, 'pending');
        
        -- Update jumlah tersedia
        UPDATE barang 
        SET jumlah_tersedia = jumlah_tersedia - p_jumlah
        WHERE kode_barang = p_kode_barang;
        
        SELECT 'SUCCESS' AS status, 'Peminjaman berhasil diajukan' AS message;
    ELSE
        SELECT 'FAILED' AS status, 'Stok tidak mencukupi' AS message;
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_proses_pengembalian` (IN `p_id_peminjaman` INT, IN `p_kondisi` VARCHAR(50), IN `p_foto` VARCHAR(255))   BEGIN
    DECLARE v_kode_barang VARCHAR(50);
    DECLARE v_jumlah INT;
    
    -- Ambil data peminjaman
    SELECT kode_barang, jumlah_pinjam 
    INTO v_kode_barang, v_jumlah
    FROM borrow 
    WHERE id_peminjaman = p_id_peminjaman;
    
    -- Update peminjaman
    UPDATE borrow 
    SET status_barang = 'dikembalikan',
        tgl_kembali = CURDATE(),
        kondisi_barang = p_kondisi,
        foto_pengembalian = p_foto
    WHERE id_peminjaman = p_id_peminjaman;
    
    -- Kembalikan stok
    UPDATE barang 
    SET jumlah_tersedia = jumlah_tersedia + v_jumlah
    WHERE kode_barang = v_kode_barang;
    
    SELECT 'SUCCESS' AS status, 'Pengembalian berhasil' AS message;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `id_admin` int NOT NULL,
  `id_user` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`id_admin`, `id_user`) VALUES
(1, 1),
(2, 14),
(3, 20);

-- --------------------------------------------------------

--
-- Table structure for table `barang`
--

CREATE TABLE `barang` (
  `id_barang` int NOT NULL,
  `id_instansi` int DEFAULT NULL,
  `kode_barang` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `nama_barang` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `lokasi_barang` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `jumlah_total` int DEFAULT '0',
  `jumlah_tersedia` int DEFAULT '0',
  `deskripsi` text COLLATE utf8mb4_general_ci,
  `kondisi_barang` enum('baik','rusak ringan','rusak berat') COLLATE utf8mb4_general_ci DEFAULT 'baik',
  `status` enum('tersedia','dipinjam','rusak','hilang') COLLATE utf8mb4_general_ci DEFAULT 'tersedia',
  `foto` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `foto_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `barang`
--

INSERT INTO `barang` (`id_barang`, `id_instansi`, `kode_barang`, `nama_barang`, `lokasi_barang`, `jumlah_total`, `jumlah_tersedia`, `deskripsi`, `kondisi_barang`, `status`, `foto`, `foto_url`, `created_at`, `updated_at`) VALUES
(1, 1, 'BRG-001', 'Proyektor Epson', 'Ruang Lab 1', 5, 5, 'Proyektor untuk presentasi', 'baik', 'tersedia', NULL, '/images/barang/ProyektorEPSON.jpg', '2025-11-15 22:36:36', '2026-01-03 20:40:46'),
(2, 1, 'BRG-002', 'Laptop Dell Latitude', 'Ruang Admin', 10, 2, 'Laptop untuk mahasiswa', 'baik', 'tersedia', NULL, '/images/barang/LaptopProgramming.jpg', '2025-11-15 22:36:36', '2026-01-03 20:41:07'),
(3, 1, 'BRG-003', 'Kamera Canon EOS', 'Ruang Media', 3, 2, 'Kamera DSLR untuk dokumentasi', 'baik', 'tersedia', NULL, '/images/barang/KameraCANON.jpg', '2025-11-15 22:36:36', '2026-01-04 13:20:26'),
(4, NULL, 'BRG-004', 'Sound System', 'Aula', 2, 2, 'Sound system untuk acara', 'baik', 'tersedia', NULL, '/images/barang/SoundSystem.jpg', '2025-11-15 22:36:36', '2026-01-03 20:41:39'),
(5, NULL, 'BRG-005', 'Meja Lipat', 'Gudang', 50, 45, 'Meja lipat untuk event', 'baik', 'tersedia', NULL, '/images/barang/MejaLipat.jpg', '2025-11-15 22:36:36', '2026-01-03 20:41:52'),
(6, 2, 'PGSD-001', 'KapiBaro', 'Ruang HIMA PGSD', 0, 5, 'Barang milik HIMA PGSD', 'baik', 'tersedia', NULL, 'https://via.placeholder.com/200x150?text=KapiBaro', '2025-11-17 22:09:22', '2026-01-03 20:43:43'),
(7, 2, 'PGSD-002', 'Capybara limited', 'Gudang PGSD', 0, 0, 'Limited edition', 'baik', 'tersedia', NULL, '/images/barang/Monyet.jpegtext=Capybara+limited', '2025-11-17 22:09:22', '2025-11-30 01:20:05'),
(8, 2, 'PGSD-003', 'Indomilk rasa Duren', 'Pantry PGSD', 0, 0, 'Stok habis', 'baik', 'tersedia', NULL, '/images/barang/Yasir.jpg', '2025-11-17 22:09:22', '2025-11-30 01:22:16'),
(9, 2, 'PGSD-004', 'Bendera', 'Ruang HIMA PGSD', 4, 4, 'Tersedia', 'baik', 'tersedia', NULL, '/images/barang/Bendera.jpg', '2025-11-17 22:09:22', '2026-01-03 20:45:09'),
(10, 4, 'PSTI-001', 'Laptop Programming', 'Lab PSTI', 10, 10, 'Untuk coding', 'baik', 'tersedia', NULL, '/images/barang/LaptopDELL.jpg', '2025-11-17 22:09:22', '2026-01-03 20:33:29'),
(11, 4, 'PSTI-002', 'Arduino Kit', 'Lab PSTI', 20, 20, 'Kit lengkap', 'baik', 'tersedia', NULL, '/images/barang/arduino.jpg', '2025-11-17 22:09:22', '2026-01-03 20:50:22'),
(12, 4, 'PSTI-003', 'bola', 'lapangan voli', 6, 6, 'basket', 'baik', 'tersedia', NULL, '/images/barang/basket.jpg', '2025-11-17 23:17:04', '2026-01-03 20:36:09'),
(14, 7, '0885885858', 'Pensil Warna', 'Gedung', 1, 1, 'Ya Pensil Warna', 'baik', 'tersedia', NULL, '/images/barang/pensilwarna.jpg', '2026-01-01 00:04:39', '2026-01-03 20:47:30'),
(15, 1, 'TES', 'tes', 'tes', 1, 1, 'tes', 'baik', 'tersedia', NULL, 'https://via.placeholder.com/200x150?text=No+Image', '2026-01-04 12:07:59', '2026-01-04 12:07:59');

-- --------------------------------------------------------

--
-- Table structure for table `berita`
--

CREATE TABLE `berita` (
  `id_berita` int NOT NULL,
  `judul` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `deskripsi` text COLLATE utf8mb4_general_ci NOT NULL,
  `warna_background` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '#D9696F',
  `created_by` int DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `id_barang` int DEFAULT NULL,
  `kode_barang` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `berita`
--

INSERT INTO `berita` (`id_berita`, `judul`, `deskripsi`, `warna_background`, `created_by`, `created_at`, `updated_at`, `id_barang`, `kode_barang`) VALUES
(34, 'Prosedur Barang Hilang', 'Peminjam wajib melapor max 1x24 jam. Penggantian unit baru dengan spesifikasi sama bersifat mutlak.', '#7BC96F', 1, '2026-01-03 20:09:43', '2026-01-03 20:09:43', NULL, NULL),
(35, 'Cek Kondisi Saat Ambil', 'Pastikan alat berfungsi saat serah terima. Komplain kerusakan setelah barang dibawa tidak dilayani.', '#C9C9C9', 1, '2026-01-03 20:10:04', '2026-01-03 20:10:04', NULL, NULL),
(36, 'Denda Keterlambatan', 'Telat kembali? Akun akan dibekukan (suspend) dan dikenakan denda harian sesuai jenis barang.', '#FFD166', 1, '2026-01-03 20:10:34', '2026-01-03 20:10:34', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `borrow`
--

CREATE TABLE `borrow` (
  `id_peminjaman` int NOT NULL,
  `id_peminjam` int NOT NULL,
  `id_admin` int DEFAULT NULL,
  `id_instansi` int DEFAULT NULL,
  `kode_barang` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `jumlah_pinjam` int NOT NULL,
  `kondisi_barang` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tgl_peminjaman` date NOT NULL,
  `tgl_pinjam` date NOT NULL,
  `tgl_kembali` date DEFAULT NULL,
  `dl_kembali` date NOT NULL,
  `foto_pengembalian` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status_barang` enum('dipinjam','dikembalikan','hilang','rusak','pending') COLLATE utf8mb4_general_ci DEFAULT 'pending',
  `status_approval` enum('pending_instansi','approved_instansi','rejected_instansi','pending_return','approved_return','rejected_return') COLLATE utf8mb4_general_ci DEFAULT 'pending_instansi',
  `catatan_penolakan` text COLLATE utf8mb4_general_ci,
  `status_pengembalian` enum('belum','diajukan','disetujui') COLLATE utf8mb4_general_ci DEFAULT 'belum',
  `jumlah_kembali_baik` int DEFAULT '0',
  `jumlah_kembali_rusak` int DEFAULT '0',
  `jumlah_hilang` int DEFAULT '0',
  `catatan_kerusakan` text COLLATE utf8mb4_general_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `jumlah_baik` int DEFAULT '0' COMMENT 'Jumlah barang kondisi baik saat pengembalian',
  `jumlah_rusak` int DEFAULT '0' COMMENT 'Jumlah barang rusak saat pengembalian',
  `catatan_pengembalian` text COLLATE utf8mb4_general_ci COMMENT 'Catatan dari peminjam saat mengembalikan',
  `alasan_penolakan` text COLLATE utf8mb4_general_ci COMMENT 'Alasan instansi menolak (pengajuan/pengembalian)',
  `tgl_approval_instansi` datetime DEFAULT NULL COMMENT 'Waktu instansi approve/reject pengajuan',
  `tgl_approval_pengembalian` datetime DEFAULT NULL COMMENT 'Waktu instansi approve/reject pengembalian',
  `id_instansi_approval` int DEFAULT NULL COMMENT 'ID instansi yang melakukan approval',
  `keperluan` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `borrow`
--

INSERT INTO `borrow` (`id_peminjaman`, `id_peminjam`, `id_admin`, `id_instansi`, `kode_barang`, `jumlah_pinjam`, `kondisi_barang`, `tgl_peminjaman`, `tgl_pinjam`, `tgl_kembali`, `dl_kembali`, `foto_pengembalian`, `status_barang`, `status_approval`, `catatan_penolakan`, `status_pengembalian`, `jumlah_kembali_baik`, `jumlah_kembali_rusak`, `jumlah_hilang`, `catatan_kerusakan`, `created_at`, `jumlah_baik`, `jumlah_rusak`, `catatan_pengembalian`, `alasan_penolakan`, `tgl_approval_instansi`, `tgl_approval_pengembalian`, `id_instansi_approval`, `keperluan`) VALUES
(1, 1, 1, 1, 'BRG-002', 1, 'rusak ringan', '2025-11-17', '2025-11-17', '2025-11-17', '2025-11-25', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 14:37:16', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 1, 1, 1, 'BRG-003', 3, 'baik', '2025-11-17', '2025-11-17', '2025-11-30', '2025-11-24', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 15:38:37', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 1, 1, 1, 'BRG-001', 5, 'baik', '2025-11-17', '2025-11-17', '2025-11-30', '2025-11-24', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 15:45:44', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 2, 1, 1, 'BRG-002', 8, 'baik', '2025-11-17', '2025-11-17', '2025-11-17', '2025-11-24', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 15:48:13', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 2, 1, 1, 'BRG-002', 3, 'baik', '2025-11-17', '2025-11-17', '2025-11-30', '2025-11-24', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 15:50:34', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(6, 2, 1, NULL, 'BRG-005', 40, 'baik', '2025-11-17', '2025-11-17', '2025-11-30', '2025-11-24', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 15:50:52', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(7, 3, 1, NULL, 'BRG-005', 5, 'baik', '2025-11-17', '2025-11-17', '2025-11-18', '2025-11-24', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 16:29:34', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(8, 3, 1, 2, 'PGSD-004', 2, 'baik', '2025-11-18', '2025-11-18', '2025-11-18', '2025-11-25', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 23:09:09', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(9, 3, 1, 4, 'PSTI-002', 10, 'baik', '2025-11-18', '2025-11-18', '2025-11-18', '2025-11-25', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 23:15:01', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(10, 3, 1, 4, 'PSTI-003', 4, 'baik', '2025-11-18', '2025-11-18', '2025-11-30', '2025-11-25', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-17 23:20:16', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(11, 3, 1, 4, 'PSTI-003', 1, 'hilang', '2025-11-18', '2025-11-18', '2025-11-18', '2025-11-25', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-18 02:45:06', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(12, 3, 1, 2, 'PGSD-001', 4, 'hilang', '2025-11-18', '2025-11-18', '2025-11-18', '2025-11-25', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-11-18 02:45:16', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(13, 3, NULL, 2, 'PGSD-001', 2, NULL, '2025-11-18', '2025-11-18', NULL, '2025-11-25', NULL, 'pending', 'pending_instansi', NULL, 'belum', 0, 0, 0, NULL, '2025-11-18 02:50:02', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(14, 3, 1, 1, 'BRG-002', 3, NULL, '2025-11-18', '2025-11-20', NULL, '2025-11-25', NULL, 'dipinjam', 'approved_instansi', NULL, 'belum', 0, 0, 0, NULL, '2025-11-18 03:59:10', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(15, 1, NULL, 4, 'PSTI-003', 1, NULL, '2025-12-11', '2025-12-11', NULL, '2025-12-18', NULL, 'pending', 'rejected_instansi', NULL, 'belum', 0, 0, 0, NULL, '2025-12-11 06:24:03', 0, 0, NULL, 'x', '2025-12-27 14:13:48', NULL, 4, NULL),
(16, 1, NULL, 4, 'PSTI-002', 1, NULL, '2025-12-11', '2025-12-11', NULL, '2025-12-12', NULL, 'pending', 'rejected_instansi', NULL, 'belum', 0, 0, 0, NULL, '2025-12-11 06:38:44', 0, 0, NULL, 'x', '2025-12-27 14:13:44', NULL, 4, NULL),
(17, 1, NULL, 4, 'PSTI-002', 1, NULL, '2025-12-15', '2025-12-15', '2025-12-27', '2025-12-22', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-12-15 12:58:04', 0, 1, 'wqrkqworq', NULL, '2025-12-27 07:36:42', '2025-12-27 07:52:39', 4, NULL),
(18, 1, NULL, 4, 'PSTI-002', 9, NULL, '2025-12-18', '2025-12-18', NULL, '2025-12-25', NULL, 'pending', 'rejected_instansi', NULL, 'belum', 0, 0, 0, NULL, '2025-12-18 01:24:49', 0, 0, NULL, 'x', '2025-12-27 14:13:35', NULL, 4, NULL),
(19, 1, NULL, NULL, 'PSTI-002', 3, NULL, '2025-12-27', '2025-12-27', '2025-12-27', '2026-01-03', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-12-27 00:53:51', 3, 0, 'Gachor', NULL, '2025-12-27 07:54:04', '2025-12-27 07:54:41', 4, NULL),
(20, 1, NULL, NULL, 'PSTI-002', 3, NULL, '2025-12-27', '2025-12-27', '2025-12-27', '2026-01-03', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-12-27 07:29:37', 3, 0, 'Cihuy', NULL, '2025-12-27 14:29:47', '2025-12-27 14:30:22', 4, NULL),
(21, 1, NULL, NULL, 'PSTI-002', 3, NULL, '2025-12-27', '2025-12-27', '2025-12-27', '2026-01-03', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-12-27 07:31:29', 2, 1, 'Kemalingan JIrr', NULL, '2025-12-27 14:31:45', '2025-12-27 14:32:32', 4, NULL),
(22, 1, NULL, NULL, 'PSTI-002', 5, NULL, '2025-12-27', '2025-12-27', '2025-12-30', '2026-01-03', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-12-27 08:12:31', 5, 0, 'Mantap', NULL, '2025-12-27 15:12:44', '2025-12-30 19:25:06', 4, NULL),
(23, 1, NULL, NULL, 'PSTI-002', 8, NULL, '2026-01-01', '2026-01-01', '2026-01-01', '2026-01-08', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-12-31 23:39:35', 8, 0, '', NULL, '2026-01-01 06:39:53', '2026-01-01 06:47:40', 4, NULL),
(24, 1, NULL, NULL, 'PSTI-002', 1, NULL, '2026-01-01', '2026-01-01', '2026-01-01', '2026-01-08', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 0, NULL, '2025-12-31 23:46:37', 1, 0, '', NULL, '2026-01-01 06:46:52', '2026-01-01 06:47:42', 4, NULL),
(25, 1, NULL, NULL, '0885885858', 1, NULL, '2026-01-01', '2026-01-01', NULL, '2026-01-08', NULL, 'pending', 'pending_instansi', NULL, 'belum', 0, 0, 0, NULL, '2026-01-01 00:05:46', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(26, 1, NULL, NULL, 'PSTI-003', 1, NULL, '2026-01-04', '2026-01-04', NULL, '2026-01-11', NULL, 'pending', 'pending_instansi', NULL, 'belum', 0, 0, 0, NULL, '2026-01-03 18:11:53', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL),
(27, 1, NULL, NULL, 'BRG-003', 1, NULL, '2026-01-04', '2026-01-04', NULL, '2026-01-11', NULL, 'pending', 'rejected_instansi', NULL, 'belum', 0, 0, 0, NULL, '2026-01-03 18:11:53', 0, 0, NULL, 'dsfs', '2026-01-04 01:23:11', NULL, 1, NULL),
(28, 1, NULL, NULL, 'BRG-003', 3, NULL, '2026-01-04', '2026-01-04', NULL, '2026-01-11', NULL, 'pending', 'rejected_instansi', NULL, 'belum', 0, 0, 0, NULL, '2026-01-03 18:17:21', 0, 0, NULL, 'hfjfh', '2026-01-04 01:23:05', NULL, 1, NULL),
(29, 1, NULL, NULL, 'BRG-002', 4, NULL, '2026-01-04', '2026-01-04', '2026-01-04', '2026-01-11', NULL, 'dikembalikan', 'approved_return', NULL, 'belum', 0, 0, 1, NULL, '2026-01-03 18:22:38', 1, 2, 'hilang di toilet', NULL, '2026-01-04 01:23:15', '2026-01-04 03:29:07', 1, 'sadassadas'),
(30, 1, NULL, NULL, 'BRG-003', 1, NULL, '2026-01-04', '2026-01-04', NULL, '2026-01-11', NULL, 'dipinjam', 'approved_instansi', NULL, 'belum', 0, 0, 0, NULL, '2026-01-04 13:17:05', 0, 0, NULL, NULL, '2026-01-04 20:20:26', NULL, 1, 'abcdefg');

-- --------------------------------------------------------

--
-- Table structure for table `instansi`
--

CREATE TABLE `instansi` (
  `id_instansi` int NOT NULL,
  `id_user` int NOT NULL,
  `nama_instansi` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `kategori` enum('LEMBAGA','BEM','HIMPUNAN','UKM') COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `instansi`
--

INSERT INTO `instansi` (`id_instansi`, `id_user`, `nama_instansi`, `kategori`) VALUES
(1, 4, 'Badan Eksekutif Mahasiswa FIK', 'BEM'),
(2, 8, 'HIMA PGSD', 'HIMPUNAN'),
(3, 9, 'HIMA UDI', 'HIMPUNAN'),
(4, 10, 'HIMA PSTI', 'HIMPUNAN'),
(5, 11, 'HMST', 'HIMPUNAN'),
(6, 12, 'HIMATRONIKA-AI', 'HIMPUNAN'),
(7, 21, 'HIMPUNAN FRSD', 'HIMPUNAN');

-- --------------------------------------------------------

--
-- Table structure for table `lapor`
--

CREATE TABLE `lapor` (
  `id_laporan` int NOT NULL,
  `no_laporan` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `id_peminjaman` int NOT NULL,
  `kode_barang` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `status` enum('diproses','selesai','ditolak') COLLATE utf8mb4_general_ci DEFAULT 'diproses',
  `tgl_laporan` date NOT NULL,
  `keterangan` text COLLATE utf8mb4_general_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `lapor`
--

INSERT INTO `lapor` (`id_laporan`, `no_laporan`, `id_peminjaman`, `kode_barang`, `status`, `tgl_laporan`, `keterangan`, `created_at`) VALUES
(1, 'LAP-00001', 7, 'BRG-005', 'selesai', '2025-11-18', NULL, '2025-11-17 17:20:16'),
(2, 'LAP-00002', 7, 'BRG-005', 'selesai', '2025-11-18', NULL, '2025-11-17 17:21:12'),
(3, 'LAP-00003', 11, 'PSTI-003', 'selesai', '2025-11-18', NULL, '2025-11-18 02:49:13'),
(4, 'LAP-00004', 12, 'PGSD-001', 'selesai', '2025-11-18', NULL, '2025-11-18 04:04:10'),
(5, 'LAP-00005', 11, 'PSTI-003', 'selesai', '2025-11-18', NULL, '2025-11-18 04:06:57'),
(6, 'LAP-00006', 30, 'BRG-003', 'diproses', '2026-01-04', 'nsaojnocjn', '2026-01-04 15:34:46'),
(7, 'LAP-00007', 30, 'BRG-003', 'diproses', '2026-01-04', '[INSTANSI: bem_fik]\nLAPORAN: 1234', '2026-01-04 16:09:39');

-- --------------------------------------------------------

--
-- Table structure for table `log_activity`
--

CREATE TABLE `log_activity` (
  `id_log` int NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `keterangan` text COLLATE utf8mb4_general_ci,
  `aktifitas` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `user_role` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `log_activity`
--

INSERT INTO `log_activity` (`id_log`, `username`, `keterangan`, `aktifitas`, `user_role`, `created_at`) VALUES
(1, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-15 22:36:37'),
(2, 'admin', 'Menambah barang baru: Proyektor Epson', 'CREATE_BARANG', 'admin', '2025-11-15 22:36:37'),
(3, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 14:31:01'),
(4, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 14:36:27'),
(5, 'budi123', 'Menambah peminjaman: Laptop Dell Latitude - 1 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 14:37:17'),
(6, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 14:41:46'),
(7, 'acil', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 14:41:52'),
(8, 'acil', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 14:43:16'),
(9, 'acil', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 14:44:47'),
(10, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 14:44:52'),
(11, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-11-17 14:48:16'),
(12, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 14:49:19'),
(13, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 14:54:41'),
(14, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 15:02:50'),
(15, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-11-17 15:03:36'),
(16, 'acil', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:03:44'),
(17, 'acil', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:13:20'),
(18, 'acil', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:13:53'),
(19, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 15:13:59'),
(20, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 15:19:58'),
(21, 'admin', 'Menyetujui peminjaman ID: 1', 'APPROVE_PEMINJAMAN', 'admin', '2025-11-17 15:30:28'),
(22, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-11-17 15:30:44'),
(23, 'acil', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:30:53'),
(24, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-11-17 15:33:44'),
(25, 'acil', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:34:03'),
(26, 'acil', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:36:40'),
(27, 'acil', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:37:02'),
(28, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:38:29'),
(29, 'budi123', 'Menambah peminjaman: Kamera Canon EOS - 3 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 15:38:40'),
(30, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:39:20'),
(31, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:39:26'),
(32, 'andi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:40:25'),
(33, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:42:51'),
(34, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:42:54'),
(35, 'siti456', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:43:01'),
(36, 'siti456', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:43:05'),
(37, 'siti456', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:43:22'),
(38, 'siti456', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:44:19'),
(39, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:44:24'),
(40, 'budi123', 'Mengembalikan barang: Laptop Dell Latitude', 'RETURN_BARANG', 'peminjam', '2025-11-17 15:44:51'),
(41, 'budi123', 'Menambah peminjaman: Proyektor Epson - 5 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 15:45:45'),
(42, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:45:47'),
(43, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 15:45:52'),
(44, 'admin', 'Menyetujui peminjaman ID: 3', 'APPROVE_PEMINJAMAN', 'admin', '2025-11-17 15:45:58'),
(45, 'admin', 'Menyetujui peminjaman ID: 2', 'APPROVE_PEMINJAMAN', 'admin', '2025-11-17 15:46:00'),
(46, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-11-17 15:46:24'),
(47, 'siti456', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:46:35'),
(48, 'siti456', 'Mengubah barang: BRG-003 - Kamera Canon EOS', 'UPDATE_BARANG', 'peminjam', '2025-11-17 15:47:27'),
(49, 'siti456', 'Menambah peminjaman: Laptop Dell Latitude - 8 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 15:48:14'),
(50, 'siti456', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 15:48:44'),
(51, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 15:48:50'),
(52, 'admin', 'Menyetujui peminjaman ID: 4', 'APPROVE_PEMINJAMAN', 'admin', '2025-11-17 15:49:20'),
(53, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-11-17 15:49:53'),
(54, 'siti456', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 15:49:59'),
(55, 'siti456', 'Mengembalikan barang: Laptop Dell Latitude', 'RETURN_BARANG', 'peminjam', '2025-11-17 15:50:18'),
(56, 'siti456', 'Menambah peminjaman: Laptop Dell Latitude - 3 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 15:50:35'),
(57, 'siti456', 'Menambah peminjaman: Meja Lipat - 40 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 15:50:52'),
(58, 'acil', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 16:19:51'),
(59, 'acil', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 16:29:20'),
(60, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 16:29:26'),
(61, 'andi123', 'Menambah peminjaman: Meja Lipat - 5 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 16:29:35'),
(62, 'siti456', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 16:29:40'),
(63, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 16:29:54'),
(64, 'admin', 'Menyetujui peminjaman ID: 6', 'APPROVE_PEMINJAMAN', 'admin', '2025-11-17 16:30:07'),
(65, 'admin', 'Menyetujui peminjaman ID: 7', 'APPROVE_PEMINJAMAN', 'admin', '2025-11-17 16:30:19'),
(66, 'andi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 16:30:58'),
(67, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 16:31:07'),
(68, 'andi123', 'Menambah laporan: LAP-00001 - Meja Lipat', 'CREATE_LAPORAN', 'peminjam', '2025-11-17 17:20:17'),
(69, 'admin', 'menyelesaikan laporan: LAP-00001', 'UPDATE_LAPORAN', 'admin', '2025-11-17 17:20:40'),
(70, 'admin', 'menyelesaikan laporan: LAP-00001', 'UPDATE_LAPORAN', 'admin', '2025-11-17 17:20:52'),
(71, 'admin', 'menyelesaikan laporan: LAP-00001', 'UPDATE_LAPORAN', 'admin', '2025-11-17 17:20:55'),
(72, 'admin', 'menolak laporan: LAP-00001', 'UPDATE_LAPORAN', 'admin', '2025-11-17 17:21:00'),
(73, 'admin', 'menyelesaikan laporan: LAP-00001', 'UPDATE_LAPORAN', 'admin', '2025-11-17 17:21:03'),
(74, 'andi123', 'Menambah laporan: LAP-00002 - Meja Lipat', 'CREATE_LAPORAN', 'peminjam', '2025-11-17 17:21:13'),
(75, 'andi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-17 17:21:18'),
(76, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 17:21:44'),
(77, 'andi123', 'Mengembalikan barang: Meja Lipat', 'RETURN_BARANG', 'peminjam', '2025-11-17 17:22:11'),
(78, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2025-11-17 18:19:29'),
(79, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-11-17 18:21:16'),
(80, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-17 23:07:51'),
(81, 'andi123', 'Menambah peminjaman: Curug Bidadari - 2 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 23:09:11'),
(82, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-11-17 23:11:28'),
(83, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 23:12:21'),
(84, 'admin', 'Menyetujui peminjaman ID: 8', 'APPROVE_BORROW', 'admin', '2025-11-17 23:13:09'),
(85, 'andi123', 'Menambah peminjaman: Arduino Kit - 10 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 23:15:03'),
(86, 'admin', 'Menyetujui peminjaman ID: 9', 'APPROVE_BORROW', 'admin', '2025-11-17 23:15:28'),
(87, 'hima_psti', 'Menambah barang: PSTI-003 - bola', 'CREATE_BARANG', 'instansi', '2025-11-17 23:17:05'),
(88, 'admin', 'Menyetujui peminjaman ID: 5', 'APPROVE_PEMINJAMAN', 'admin', '2025-11-17 23:18:19'),
(89, 'andi123', 'Menambah peminjaman: bola - 4 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-17 23:20:18'),
(90, 'andi123', 'Mengembalikan barang: Arduino Kit', 'RETURN_BARANG', 'peminjam', '2025-11-17 23:21:34'),
(91, 'andi123', 'Mengembalikan barang: Curug Bidadari', 'RETURN_BARANG', 'peminjam', '2025-11-17 23:21:37'),
(92, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-11-17 23:22:00'),
(93, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-11-17 23:22:09'),
(94, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 23:40:03'),
(95, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-11-17 23:40:27'),
(96, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-17 23:41:16'),
(97, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-11-18 00:09:04'),
(98, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-11-18 00:09:15'),
(99, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-11-18 00:09:30'),
(100, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-18 00:09:39'),
(101, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-18 02:44:56'),
(102, 'andi123', 'Menambah peminjaman: bola - 1 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-18 02:45:07'),
(103, 'andi123', 'Menambah peminjaman: KapiBaro - 4 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-18 02:45:17'),
(104, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-18 02:45:45'),
(105, 'admin', 'Menyetujui peminjaman ID: 12', 'APPROVE_BORROW', 'admin', '2025-11-18 02:45:54'),
(106, 'admin', 'Menyetujui peminjaman ID: 12', 'APPROVE_BORROW', 'admin', '2025-11-18 02:45:57'),
(107, 'admin', 'Menyetujui peminjaman ID: 12', 'APPROVE_BORROW', 'admin', '2025-11-18 02:45:59'),
(108, 'admin', 'Menyetujui peminjaman ID: 11', 'APPROVE_BORROW', 'admin', '2025-11-18 02:46:01'),
(109, 'admin', 'Menyetujui peminjaman ID: 10', 'APPROVE_BORROW', 'admin', '2025-11-18 02:46:04'),
(110, 'andi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-18 02:46:34'),
(111, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-18 02:46:43'),
(112, 'andi123', 'Menambah laporan: LAP-00003 - bola', 'CREATE_LAPORAN', 'peminjam', '2025-11-18 02:49:14'),
(113, 'admin', 'menyelesaikan laporan: LAP-00003', 'PROCESS_LAPORAN', 'admin', '2025-11-18 02:50:33'),
(114, 'andi123', 'Menambah peminjaman: KapiBaro - 2 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-18 02:51:51'),
(115, 'andi123', 'Menambah peminjaman: Laptop Dell Latitude - 3 unit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-11-18 03:59:26'),
(116, 'andi123', 'Menambah laporan: LAP-00004 - KapiBaro', 'CREATE_LAPORAN', 'peminjam', '2025-11-18 04:04:14'),
(117, 'andi123', 'Mengembalikan barang: KapiBaro', 'RETURN_BARANG', 'peminjam', '2025-11-18 04:05:02'),
(118, 'andi123', 'Menambah laporan: LAP-00005 - bola', 'CREATE_LAPORAN', 'peminjam', '2025-11-18 04:06:59'),
(119, 'andi123', 'Mengembalikan barang: bola', 'RETURN_BARANG', 'peminjam', '2025-11-18 04:07:30'),
(120, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-27 21:55:08'),
(121, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-27 22:52:00'),
(122, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-27 22:55:20'),
(123, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-29 22:15:29'),
(124, 'admin', 'Mengembalikan barang: Kamera Canon EOS', 'RETURN_BARANG', 'admin', '2025-11-29 22:15:47'),
(125, 'admin', 'Mengembalikan barang: Laptop Dell Latitude', 'RETURN_BARANG', 'admin', '2025-11-29 22:15:49'),
(126, 'admin', 'Mengembalikan barang: Proyektor Epson', 'RETURN_BARANG', 'admin', '2025-11-29 22:15:52'),
(127, 'admin', 'Mengembalikan barang: Meja Lipat', 'RETURN_BARANG', 'admin', '2025-11-29 22:15:54'),
(128, 'admin', 'Mengembalikan barang: bola', 'RETURN_BARANG', 'admin', '2025-11-29 22:15:55'),
(129, 'admin', 'Menyetujui peminjaman ID: 14', 'APPROVE_PEMINJAMAN', 'admin', '2025-11-29 22:16:06'),
(130, 'andi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-29 22:30:48'),
(131, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-29 22:31:43'),
(132, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-29 22:33:32'),
(133, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-29 22:41:49'),
(134, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-11-29 23:11:50'),
(135, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-29 23:12:20'),
(136, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-29 23:16:30'),
(137, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-29 23:42:59'),
(138, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-29 23:46:24'),
(139, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-11-29 23:57:24'),
(140, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:00:37'),
(141, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:02:37'),
(142, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:06:03'),
(143, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:07:18'),
(144, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:12:27'),
(145, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:14:12'),
(146, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:23:47'),
(147, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:27:35'),
(148, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:33:12'),
(149, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:39:43'),
(150, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:44:19'),
(151, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:46:28'),
(152, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:48:57'),
(153, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:51:02'),
(154, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:58:44'),
(155, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 00:59:32'),
(156, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:03:54'),
(157, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:09:16'),
(158, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:13:06'),
(159, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:14:38'),
(160, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:20:26'),
(161, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:22:30'),
(162, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:26:59'),
(163, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:32:27'),
(164, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-11-30 01:33:49'),
(165, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-01 12:12:41'),
(166, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 17:02:30'),
(167, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 17:45:29'),
(168, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 17:50:26'),
(169, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 17:54:41'),
(170, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 18:05:49'),
(171, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 18:07:55'),
(172, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 18:11:17'),
(173, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 18:18:35'),
(174, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 18:19:07'),
(175, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 18:20:41'),
(176, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 18:26:58'),
(177, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 18:37:39'),
(178, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-03 18:38:34'),
(179, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:10:19'),
(180, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:29:56'),
(181, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:34:30'),
(182, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-03 19:36:53'),
(183, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:37:57'),
(184, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:39:22'),
(185, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:44:04'),
(186, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:46:41'),
(187, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:55:48'),
(188, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 19:58:28'),
(189, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-03 20:06:32'),
(190, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 06:22:39'),
(191, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-11 06:23:07'),
(192, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-11 06:23:15'),
(193, 'budi123', 'Menambah peminjaman: bola', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-11 06:24:03'),
(194, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-11 06:24:10'),
(195, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-11 06:24:24'),
(196, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-11 06:24:43'),
(197, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-11 06:26:06'),
(198, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-11 06:38:44'),
(199, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-11 06:39:10'),
(200, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-11 06:39:16'),
(201, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-11 06:44:52'),
(202, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-11 06:46:50'),
(203, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 06:47:30'),
(204, 'admin', 'Menyelesaikan Laporan: LAP-00005', 'PROCESS_LAPORAN', 'admin', '2025-12-11 06:47:51'),
(205, 'admin', 'Menyelesaikan Laporan: LAP-00005', 'PROCESS_LAPORAN', 'admin', '2025-12-11 06:47:55'),
(206, 'admin', 'Menyelesaikan Laporan: LAP-00002', 'PROCESS_LAPORAN', 'admin', '2025-12-11 06:48:01'),
(207, 'admin', 'Reset password user: hima_pgsd', 'RESET_PASSWORD', 'admin', '2025-12-11 06:50:20'),
(208, 'admin', 'menonaktifkan user: hima_pgsd', 'UPDATE_USER_STATUS', 'admin', '2025-12-11 07:03:15'),
(209, 'admin', 'mengaktifkan user: hima_pgsd', 'UPDATE_USER_STATUS', 'admin', '2025-12-11 07:03:18'),
(210, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 07:13:29'),
(211, 'admin', 'Tambah user: sea', 'CREATE_USER', 'admin', '2025-12-11 07:16:08'),
(212, 'admin', 'Reset password user: sea', 'RESET_PASSWORD', 'admin', '2025-12-11 07:16:17'),
(213, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-11 07:16:20'),
(214, 'sea', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-11 07:16:27'),
(215, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 07:16:42'),
(216, 'admin', 'menonaktifkan user: sea', 'UPDATE_USER_STATUS', 'admin', '2025-12-11 07:16:49'),
(217, 'admin', 'Menyelesaikan Laporan: LAP-00004', 'PROCESS_LAPORAN', 'admin', '2025-12-11 07:17:00'),
(218, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-11 07:17:12'),
(219, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 07:17:26'),
(220, 'admin', 'mengaktifkan user: sea', 'UPDATE_USER_STATUS', 'admin', '2025-12-11 07:17:31'),
(221, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-11 07:17:35'),
(222, 'sea', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-11 07:17:40'),
(223, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 07:18:51'),
(224, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-11 07:19:31'),
(225, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 07:19:49'),
(226, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 07:21:29'),
(227, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-11 07:26:19'),
(228, 'admin', 'Tambah user: wq (role: admin)', 'CREATE_USER', 'admin', '2025-12-11 07:26:34'),
(229, 'admin', 'menonaktifkan user: wq', 'UPDATE_USER_STATUS', 'admin', '2025-12-11 07:26:39'),
(230, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 02:56:27'),
(231, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-15 02:56:52'),
(232, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-15 02:56:56'),
(233, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-15 02:57:10'),
(234, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 02:57:14'),
(235, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 03:02:14'),
(236, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 03:07:39'),
(237, 'admin', 'Tambah berita: wrkoqw', 'CREATE_BERITA', 'admin', '2025-12-15 03:08:01'),
(238, 'admin', 'Tambah berita: qwrkqow', 'CREATE_BERITA', 'admin', '2025-12-15 03:08:18'),
(239, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 03:19:26'),
(240, 'admin', 'Tambah berita: xx', 'CREATE_BERITA', 'admin', '2025-12-15 03:19:58'),
(241, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 03:33:37'),
(242, 'admin', 'Tambah berita: wqrq', 'CREATE_BERITA', 'admin', '2025-12-15 03:33:45'),
(243, 'admin', 'Tambah berita: Kachiw', 'CREATE_BERITA', 'admin', '2025-12-15 03:34:03'),
(244, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 03:37:43'),
(245, 'admin', 'Tambah berita: x', 'CREATE_BERITA', 'admin', '2025-12-15 03:37:50'),
(246, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 03:42:30'),
(247, 'admin', 'Hapus berita: x', 'DELETE_BERITA', 'admin', '2025-12-15 03:42:46'),
(248, 'admin', 'Hapus berita: Kachiw', 'DELETE_BERITA', 'admin', '2025-12-15 03:42:50'),
(249, 'admin', 'Hapus berita: wqrq', 'DELETE_BERITA', 'admin', '2025-12-15 03:42:56'),
(250, 'admin', 'Tambah berita: Arya Adi Manggala', 'CREATE_BERITA', 'admin', '2025-12-15 03:43:19'),
(251, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-15 03:44:51'),
(252, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-15 03:44:55'),
(253, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-15 03:45:27'),
(254, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-15 03:45:30'),
(255, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-15 03:45:43'),
(256, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 03:45:46'),
(257, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-15 03:51:38'),
(258, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 12:54:26'),
(259, 'admin', 'mengaktifkan user: wq', 'UPDATE_USER_STATUS', 'admin', '2025-12-15 12:54:41'),
(260, 'admin', 'Tambah user: SISTEL (role: peminjam)', 'CREATE_USER', 'admin', '2025-12-15 12:55:16'),
(261, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-15 12:55:24'),
(262, 'SISTEL', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-15 12:55:30'),
(263, 'SISTEL', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-15 12:55:40'),
(264, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 12:55:46'),
(265, 'admin', 'Tambah berita: XXX', 'CREATE_BERITA', 'admin', '2025-12-15 12:56:04'),
(266, 'admin', 'Hapus berita: Arya Adi Manggala', 'DELETE_BERITA', 'admin', '2025-12-15 12:56:22'),
(267, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-15 12:56:33'),
(268, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-15 12:56:39'),
(269, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-15 12:58:04'),
(270, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-15 12:58:18'),
(271, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-15 12:58:57'),
(272, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-15 12:59:11'),
(273, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 22:41:03'),
(274, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 22:43:24'),
(275, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-15 22:43:54'),
(276, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-15 22:44:00'),
(277, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-15 22:45:37'),
(278, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 22:45:41'),
(279, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-15 22:50:29'),
(280, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 22:51:40'),
(281, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-15 22:51:53'),
(282, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-15 22:52:22'),
(283, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-15 22:52:30'),
(284, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-15 22:53:04'),
(285, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 02:03:24'),
(286, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 02:51:37'),
(287, 'admin', 'Tambah user: Zaldi (role: peminjam)', 'CREATE_USER', 'admin', '2025-12-16 02:52:28'),
(288, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 02:52:30'),
(289, 'Zaldi', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-16 02:52:34'),
(290, 'Zaldi', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-16 02:52:52'),
(291, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 03:50:04'),
(292, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 03:50:20'),
(293, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-16 03:50:24'),
(294, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 03:54:05'),
(295, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:01:47'),
(296, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:07:05'),
(297, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 04:07:32'),
(298, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-16 04:07:39'),
(299, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-16 04:08:12'),
(300, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:08:17'),
(301, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:09:03'),
(302, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 04:09:42'),
(303, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:11:14'),
(304, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 04:11:30'),
(305, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:11:36'),
(306, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:12:19'),
(307, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 04:12:55'),
(308, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-16 04:13:06'),
(309, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:13:50'),
(310, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:15:25'),
(311, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:20:45'),
(312, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:28:41'),
(313, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 04:30:28'),
(314, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-16 04:30:31'),
(315, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:32:44'),
(316, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:34:37'),
(317, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 04:34:57'),
(318, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-16 04:35:02'),
(319, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:38:52'),
(320, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 04:39:03'),
(321, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-16 04:39:07'),
(322, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-16 04:45:48'),
(323, 'admin', 'Hapus berita: TELAH HILANG KEPERCAYAAN', 'DELETE_BERITA', 'admin', '2025-12-16 04:47:00'),
(324, 'admin', 'Hapus berita: Jadwal Maintenance', 'DELETE_BERITA', 'admin', '2025-12-16 04:47:14'),
(325, 'admin', 'Hapus berita: XXX', 'DELETE_BERITA', 'admin', '2025-12-16 04:47:25'),
(326, 'admin', 'Hapus berita: xx', 'DELETE_BERITA', 'admin', '2025-12-16 04:47:35'),
(327, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-16 04:48:08'),
(328, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-16 04:48:17'),
(329, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-16 04:51:15'),
(330, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-16 04:52:37'),
(331, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-16 04:53:45'),
(332, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-16 04:55:49'),
(333, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-17 01:16:27'),
(334, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-17 01:17:25'),
(335, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-17 01:17:53'),
(336, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-18 01:23:44'),
(337, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-18 01:24:16'),
(338, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-18 01:24:23'),
(339, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-18 01:24:49'),
(340, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-18 02:14:07'),
(341, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-18 02:14:16'),
(342, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-18 14:55:36'),
(343, 'admin', 'Tambah berita: DICARI BARANG HILANG', 'CREATE_BERITA', 'admin', '2025-12-18 14:58:35'),
(344, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-18 14:59:57'),
(345, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-18 15:00:05'),
(346, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-18 15:00:11'),
(347, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-18 15:00:22'),
(348, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-18 15:00:28'),
(349, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-20 12:03:41'),
(350, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-20 12:06:04'),
(351, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-23 13:07:25'),
(352, 'admin', 'Tambah berita: Sat', 'CREATE_BERITA', 'admin', '2025-12-23 13:07:35'),
(353, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-23 13:08:17'),
(354, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-25 09:18:02'),
(355, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-25 09:18:11'),
(356, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 00:36:28'),
(357, 'hima_psti', 'Menyetujui pengajuan peminjaman ID: 17', 'APPROVE_PENGAJUAN', 'instansi', '2025-12-27 00:36:44'),
(358, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 00:37:09'),
(359, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 00:43:51'),
(360, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 00:44:03'),
(361, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 00:44:07'),
(362, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 00:44:26'),
(363, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 00:51:54'),
(364, 'budi123', 'Mengajukan pengembalian: Arduino Kit', 'RETURN_REQUEST', 'peminjam', '2025-12-27 00:52:24'),
(365, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 00:52:28'),
(366, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 00:52:33'),
(367, 'hima_psti', 'Menerima pengembalian ID: 17', 'APPROVE_PENGEMBALIAN', 'instansi', '2025-12-27 00:52:41'),
(368, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 00:53:24'),
(369, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-27 00:53:51'),
(370, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 00:53:55'),
(371, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 00:53:59'),
(372, 'hima_psti', 'Menyetujui pengajuan peminjaman ID: 19', 'APPROVE_PENGAJUAN', 'instansi', '2025-12-27 00:54:05'),
(373, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 00:54:11'),
(374, 'budi123', 'Mengajukan pengembalian: Arduino Kit', 'RETURN_REQUEST', 'peminjam', '2025-12-27 00:54:30'),
(375, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 00:54:32'),
(376, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 00:54:37'),
(377, 'hima_psti', 'Menerima pengembalian ID: 19', 'APPROVE_PENGEMBALIAN', 'instansi', '2025-12-27 00:54:42'),
(378, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 00:55:39'),
(379, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 01:00:14'),
(380, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 07:04:34'),
(381, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 07:04:56'),
(382, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:05:00'),
(383, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:08:06'),
(384, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:10:32'),
(385, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:13:30'),
(386, 'hima_psti', 'Menolak pengajuan ID: 18 - Alasan: x', 'REJECT_PENGAJUAN', 'instansi', '2025-12-27 07:13:36'),
(387, 'hima_psti', 'Menolak pengajuan ID: 16 - Alasan: x', 'REJECT_PENGAJUAN', 'instansi', '2025-12-27 07:13:45'),
(388, 'hima_psti', 'Menolak pengajuan ID: 15 - Alasan: x', 'REJECT_PENGAJUAN', 'instansi', '2025-12-27 07:13:49'),
(389, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-27 07:13:55'),
(390, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 07:13:59'),
(391, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-27 07:29:37'),
(392, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 07:29:41'),
(393, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:29:44'),
(394, 'hima_psti', 'Menyetujui pengajuan peminjaman ID: 20', 'APPROVE_PENGAJUAN', 'instansi', '2025-12-27 07:29:48'),
(395, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 07:29:56'),
(396, 'budi123', 'Mengajukan pengembalian: Arduino Kit', 'RETURN_REQUEST', 'peminjam', '2025-12-27 07:30:06'),
(397, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 07:30:10'),
(398, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:30:16'),
(399, 'hima_psti', 'Menerima pengembalian ID: 20', 'APPROVE_PENGEMBALIAN', 'instansi', '2025-12-27 07:30:23'),
(400, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-27 07:31:15'),
(401, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 07:31:19'),
(402, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-27 07:31:29'),
(403, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 07:31:32'),
(404, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:31:36'),
(405, 'hima_psti', 'Menyetujui pengajuan peminjaman ID: 21', 'APPROVE_PENGAJUAN', 'instansi', '2025-12-27 07:31:46'),
(406, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-27 07:31:49'),
(407, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 07:31:52'),
(408, 'budi123', 'Mengajukan pengembalian: Arduino Kit', 'RETURN_REQUEST', 'peminjam', '2025-12-27 07:32:21'),
(409, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 07:32:24'),
(410, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:32:27'),
(411, 'hima_psti', 'Menerima pengembalian ID: 21', 'APPROVE_PENGEMBALIAN', 'instansi', '2025-12-27 07:32:33'),
(412, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:50:37'),
(413, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 07:59:10'),
(414, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 08:00:01'),
(415, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 08:11:36'),
(416, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-27 08:12:05'),
(417, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-27 08:12:08'),
(418, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-27 08:12:31'),
(419, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-27 08:12:35'),
(420, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-27 08:12:39'),
(421, 'hima_psti', 'Menyetujui pengajuan peminjaman ID: 22', 'APPROVE_PENGAJUAN', 'instansi', '2025-12-27 08:12:45'),
(422, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-27 08:13:57'),
(423, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-28 05:46:20'),
(424, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-30 07:56:30'),
(425, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-30 07:57:13'),
(426, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-30 07:57:20'),
(427, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-30 08:07:42'),
(428, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-30 08:12:52'),
(429, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-30 08:13:21'),
(430, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-30 08:13:27'),
(431, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-30 08:17:50'),
(432, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-30 08:20:11'),
(433, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-30 12:14:31'),
(434, 'admin', 'Tambah user: Afdy (role: peminjam)', 'CREATE_USER', 'admin', '2025-12-30 12:16:31'),
(435, 'admin', 'Tambah user: Abah (role: admin)', 'CREATE_USER', 'admin', '2025-12-30 12:20:19'),
(436, 'admin', 'menonaktifkan user: Abah', 'UPDATE_USER_STATUS', 'admin', '2025-12-30 12:20:23'),
(437, 'admin', 'menonaktifkan user: Afdy', 'UPDATE_USER_STATUS', 'admin', '2025-12-30 12:20:29'),
(438, 'admin', 'menonaktifkan user: hima_udi', 'UPDATE_USER_STATUS', 'admin', '2025-12-30 12:20:35'),
(439, 'admin', 'Hapus berita: DICARI BARANG HILANG', 'DELETE_BERITA', 'admin', '2025-12-30 12:21:38'),
(440, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2025-12-30 12:21:58'),
(441, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-30 12:22:07'),
(442, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-30 12:23:09'),
(443, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-30 12:23:13'),
(444, 'budi123', 'Mengajukan pengembalian: Arduino Kit', 'RETURN_REQUEST', 'peminjam', '2025-12-30 12:23:37'),
(445, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-30 12:24:57'),
(446, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-30 12:25:01'),
(447, 'hima_psti', 'Menerima pengembalian ID: 22', 'APPROVE_PENGEMBALIAN', 'instansi', '2025-12-30 12:25:07'),
(448, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-30 12:25:16'),
(449, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-31 23:38:26'),
(450, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-31 23:39:00'),
(451, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-31 23:39:07'),
(452, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-31 23:39:36'),
(453, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-31 23:39:42'),
(454, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-31 23:39:46'),
(455, 'hima_psti', 'Menyetujui pengajuan peminjaman ID: 23', 'APPROVE_PENGAJUAN', 'instansi', '2025-12-31 23:39:54'),
(456, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-31 23:45:59'),
(457, 'budi123', 'Menambah peminjaman: Arduino Kit', 'CREATE_PEMINJAMAN', 'peminjam', '2025-12-31 23:46:37'),
(458, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-31 23:46:42'),
(459, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-31 23:46:46'),
(460, 'hima_psti', 'Menyetujui pengajuan peminjaman ID: 24', 'APPROVE_PENGAJUAN', 'instansi', '2025-12-31 23:46:53'),
(461, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-31 23:47:04'),
(462, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2025-12-31 23:47:08'),
(463, 'budi123', 'Mengajukan pengembalian: Arduino Kit', 'RETURN_REQUEST', 'peminjam', '2025-12-31 23:47:16'),
(464, 'budi123', 'Mengajukan pengembalian: Arduino Kit', 'RETURN_REQUEST', 'peminjam', '2025-12-31 23:47:23'),
(465, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2025-12-31 23:47:28'),
(466, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2025-12-31 23:47:32'),
(467, 'hima_psti', 'Menerima pengembalian ID: 23', 'APPROVE_PENGEMBALIAN', 'instansi', '2025-12-31 23:47:41'),
(468, 'hima_psti', 'Menerima pengembalian ID: 24', 'APPROVE_PENGEMBALIAN', 'instansi', '2025-12-31 23:47:43'),
(469, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2025-12-31 23:47:52'),
(470, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-31 23:48:49'),
(471, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2025-12-31 23:56:08'),
(472, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-01 00:00:36'),
(473, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-01 00:03:41'),
(474, 'admin', 'Tambah user: hima_frsd (role: instansi)', 'CREATE_USER', 'admin', '2026-01-01 00:04:04'),
(475, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-01 00:04:15'),
(476, 'hima_frsd', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-01 00:04:19'),
(477, 'hima_frsd', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-01 00:04:47'),
(478, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-01 00:04:50'),
(479, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-01 00:05:07'),
(480, 'hima_frsd', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-01 00:05:12'),
(481, 'hima_frsd', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-01 00:05:34'),
(482, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-01 00:05:41'),
(483, 'budi123', 'Menambah peminjaman: Pensil Warna', 'CREATE_PEMINJAMAN', 'peminjam', '2026-01-01 00:05:46'),
(484, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-01 00:06:52'),
(485, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-01 00:25:07'),
(486, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-01 00:25:15'),
(487, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-01 00:25:18'),
(488, 'admin', 'Tambah user: qwerty (role: instansi, kategori: Himpunan)', 'CREATE_USER', 'admin', '2026-01-01 00:25:42'),
(489, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-01 00:25:44'),
(490, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-01 00:25:47'),
(491, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-01 00:25:54'),
(492, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-01 00:25:57'),
(493, 'admin', 'menonaktifkan user: qwerty', 'UPDATE_USER_STATUS', 'admin', '2026-01-01 00:26:08'),
(494, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-01 00:26:13'),
(495, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-01 00:26:17'),
(496, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-01 00:48:51'),
(497, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 17:35:07'),
(498, 'budi123', 'Menambah peminjaman: bola', 'CREATE_PEMINJAMAN', 'peminjam', '2026-01-03 18:11:53'),
(499, 'budi123', 'Menambah peminjaman: Kamera Canon EOS', 'CREATE_PEMINJAMAN', 'peminjam', '2026-01-03 18:11:53'),
(500, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 18:11:56'),
(501, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 18:12:04'),
(502, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 18:16:53'),
(503, 'budi123', 'Menambah peminjaman: Kamera Canon EOS', 'CREATE_PEMINJAMAN', 'peminjam', '2026-01-03 18:17:21'),
(504, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 18:17:30'),
(505, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 18:17:50'),
(506, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 18:22:07'),
(507, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-03 18:22:16'),
(508, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 18:22:22'),
(509, 'budi123', 'Menambah peminjaman: Laptop Dell Latitude', 'CREATE_PEMINJAMAN', 'peminjam', '2026-01-03 18:22:38'),
(510, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 18:22:41'),
(511, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 18:22:48'),
(512, 'bem_fik', 'Menolak pengajuan ID: 28 - Alasan: hfjfh', 'REJECT_PENGAJUAN', 'instansi', '2026-01-03 18:23:07'),
(513, 'bem_fik', 'Menolak pengajuan ID: 27 - Alasan: dsfs', 'REJECT_PENGAJUAN', 'instansi', '2026-01-03 18:23:12'),
(514, 'bem_fik', 'Menyetujui pengajuan peminjaman ID: 29', 'APPROVE_PENGAJUAN', 'instansi', '2026-01-03 18:23:16'),
(515, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-03 18:23:18'),
(516, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 18:23:24'),
(517, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 18:40:48'),
(518, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 18:40:55'),
(519, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 18:41:02'),
(520, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 18:54:16'),
(521, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 18:54:19'),
(522, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 18:54:26'),
(523, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 18:57:46'),
(524, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:14:30'),
(525, 'admin', 'Tambah Berita: arduino', 'CREATE_BERITA', 'admin', '2026-01-03 19:14:50'),
(526, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:16:50'),
(527, 'admin', 'Tambah Berita: dsfsdf', 'CREATE_BERITA', 'admin', '2026-01-03 19:17:02'),
(528, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:19:16'),
(529, 'admin', 'Tambah Berita: faf', 'CREATE_BERITA', 'admin', '2026-01-03 19:19:32'),
(530, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-03 19:21:20'),
(531, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 19:21:26'),
(532, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:23:40'),
(533, 'admin', 'Tambah Berita: fdgdfg', 'CREATE_BERITA', 'admin', '2026-01-03 19:23:53'),
(534, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:25:00'),
(535, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:29:28'),
(536, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:30:02'),
(537, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:34:39'),
(538, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:42:37'),
(539, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-03 19:42:46'),
(540, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 19:42:56'),
(541, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-03 19:43:10'),
(542, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 19:50:45'),
(543, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 19:57:07'),
(544, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 19:57:36'),
(545, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 19:57:46'),
(546, 'admin', 'Tambah berita: asda', 'CREATE_BERITA', 'admin', '2026-01-03 19:57:52'),
(547, 'admin', 'Tambah berita: asdasd', 'CREATE_BERITA', 'admin', '2026-01-03 19:57:56'),
(548, 'admin', 'Tambah berita: asdasd', 'CREATE_BERITA', 'admin', '2026-01-03 19:58:00'),
(549, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:05:14'),
(550, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:06:37'),
(551, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 20:07:23'),
(552, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 20:07:30'),
(553, 'admin', 'Hapus berita: fsdfsdf', 'DELETE_BERITA', 'admin', '2026-01-03 20:07:37'),
(554, 'admin', 'Hapus berita: asdasd', 'DELETE_BERITA', 'admin', '2026-01-03 20:07:39'),
(555, 'admin', 'Hapus berita: asdasd', 'DELETE_BERITA', 'admin', '2026-01-03 20:07:42'),
(556, 'admin', 'Hapus berita: asda', 'DELETE_BERITA', 'admin', '2026-01-03 20:07:44'),
(557, 'admin', 'Tambah berita: jn', 'CREATE_BERITA', 'admin', '2026-01-03 20:08:18'),
(558, 'admin', 'Hapus berita: jn', 'DELETE_BERITA', 'admin', '2026-01-03 20:08:22'),
(559, 'admin', 'Tambah berita: Prosedur Barang Hilang', 'CREATE_BERITA', 'admin', '2026-01-03 20:09:44'),
(560, 'admin', 'Tambah berita: Cek Kondisi Saat Ambil', 'CREATE_BERITA', 'admin', '2026-01-03 20:10:04'),
(561, 'admin', 'Tambah berita: Denda Keterlambatan', 'CREATE_BERITA', 'admin', '2026-01-03 20:10:35'),
(562, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:14:08'),
(563, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:17:04'),
(564, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 20:17:08'),
(565, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 20:17:16'),
(566, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:19:49'),
(567, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:22:41'),
(568, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:26:57'),
(569, 'budi123', 'Mengajukan pengembalian: Laptop Dell Latitude', 'RETURN_REQUEST', 'peminjam', '2026-01-03 20:28:31'),
(570, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 20:28:37'),
(571, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 20:28:45'),
(572, 'bem_fik', 'Menerima pengembalian ID: 29', 'APPROVE_PENGEMBALIAN', 'instansi', '2026-01-03 20:29:08'),
(573, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-03 20:29:30'),
(574, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:29:38'),
(575, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 20:29:49'),
(576, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-03 20:31:27'),
(577, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:35:50'),
(578, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:37:48'),
(579, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:39:19'),
(580, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:42:56'),
(581, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-03 20:52:45'),
(582, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-03 20:53:17'),
(583, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 20:53:28'),
(584, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 21:01:01'),
(585, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 21:03:03'),
(586, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 21:05:20');
INSERT INTO `log_activity` (`id_log`, `username`, `keterangan`, `aktifitas`, `user_role`, `created_at`) VALUES
(587, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 21:11:14'),
(588, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-03 21:16:13'),
(589, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 11:57:20'),
(590, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 11:58:42'),
(591, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 11:59:01'),
(592, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 12:40:08'),
(593, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 12:48:15'),
(594, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 13:16:40'),
(595, 'budi123', 'Menambah peminjaman: Kamera Canon EOS', 'CREATE_PEMINJAMAN', 'peminjam', '2026-01-04 13:17:05'),
(596, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 13:17:15'),
(597, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 13:17:34'),
(598, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-04 13:19:35'),
(599, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 13:19:42'),
(600, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 13:19:53'),
(601, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 13:19:59'),
(602, 'bem_fik', 'Menyetujui pengajuan peminjaman ID: 30', 'APPROVE_PENGAJUAN', 'instansi', '2026-01-04 13:20:28'),
(603, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-04 13:20:35'),
(604, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 13:20:44'),
(605, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 13:28:53'),
(606, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 13:29:03'),
(607, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 13:46:03'),
(608, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 13:46:12'),
(609, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 13:46:32'),
(610, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-04 13:54:37'),
(611, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 13:54:45'),
(612, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-04 13:54:55'),
(613, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 13:55:03'),
(614, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-04 13:55:16'),
(615, 'hima_psti', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 13:55:50'),
(616, 'hima_psti', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-04 13:56:32'),
(617, 'hima_pgsd', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 13:56:55'),
(618, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 14:00:01'),
(619, 'admin', 'Tambah berita: adlalmfnq', 'CREATE_BERITA', 'admin', '2026-01-04 14:01:04'),
(620, 'admin', 'Hapus berita: adlalmfnq', 'DELETE_BERITA', 'admin', '2026-01-04 14:01:26'),
(621, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-04 14:03:50'),
(622, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 14:04:04'),
(623, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-04 14:04:33'),
(624, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 14:06:06'),
(625, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 14:06:11'),
(626, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 14:44:30'),
(627, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 14:44:40'),
(628, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 14:46:35'),
(629, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 14:46:46'),
(630, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 14:47:15'),
(631, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-04 14:54:46'),
(632, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 14:54:53'),
(633, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 15:14:30'),
(634, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 15:34:00'),
(635, 'budi123', 'Menambah Laporan: LAP-00006', 'CREATE_LAPORAN', 'peminjam', '2026-01-04 15:34:47'),
(636, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 15:35:14'),
(637, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 15:35:27'),
(638, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 15:35:45'),
(639, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 15:39:46'),
(640, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 15:41:23'),
(641, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 15:57:05'),
(642, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 15:58:19'),
(643, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 16:09:19'),
(644, 'budi123', 'Menambah Laporan: LAP-00007', 'CREATE_LAPORAN', 'peminjam', '2026-01-04 16:09:40'),
(645, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 16:09:44'),
(646, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 16:09:49'),
(647, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-04 16:10:10'),
(648, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 16:12:00'),
(649, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 16:15:32'),
(650, 'admin', 'Edit user (Username/Nama): yuvygv', 'UPDATE_USER', 'admin', '2026-01-04 16:15:45'),
(651, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 16:16:47'),
(652, 'admin', 'Reset password user: yuvygv', 'RESET_PASSWORD', 'admin', '2026-01-04 16:17:05'),
(653, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-04 16:17:46'),
(654, 'bem_fik', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 16:17:54'),
(655, 'bem_fik', 'Logout dari sistem', 'LOGOUT', 'instansi', '2026-01-04 16:18:10'),
(656, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 16:19:47'),
(657, 'admin', 'Logout dari sistem', 'LOGOUT', 'admin', '2026-01-04 16:19:53'),
(658, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 16:20:03'),
(659, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 16:22:13'),
(660, 'admin', 'Login ke sistem', 'LOGIN', 'admin', '2026-01-04 16:27:31'),
(661, 'budi123', 'Login ke sistem', 'LOGIN', 'peminjam', '2026-01-04 23:17:34'),
(662, 'budi123', 'Logout dari sistem', 'LOGOUT', 'peminjam', '2026-01-04 23:18:24'),
(663, 'hima_pgsd', 'Login ke sistem', 'LOGIN', 'instansi', '2026-01-04 23:18:42');

-- --------------------------------------------------------

--
-- Table structure for table `peminjam`
--

CREATE TABLE `peminjam` (
  `id_peminjam` int NOT NULL,
  `id_user` int NOT NULL,
  `no_telepon` varchar(15) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `peminjam`
--

INSERT INTO `peminjam` (`id_peminjam`, `id_user`, `no_telepon`) VALUES
(1, 2, '081234567890'),
(2, 3, '082345678901'),
(3, 7, '081298765432'),
(4, 13, '088879412794129'),
(5, 15, '0888'),
(6, 16, '0988'),
(7, 19, '08888');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id_user` int NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `nama` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `role` enum('admin','peminjam','instansi') COLLATE utf8mb4_general_ci NOT NULL,
  `status` enum('aktif','nonaktif') COLLATE utf8mb4_general_ci DEFAULT 'aktif',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id_user`, `username`, `password`, `nama`, `role`, `status`, `created_at`, `updated_at`) VALUES
(1, 'admin', '123456', 'Administrator', 'admin', 'aktif', '2025-11-15 22:36:36', '2025-11-17 14:44:40'),
(2, 'budi123', '123456', 'Budi Santoso', 'peminjam', 'aktif', '2025-11-15 22:36:36', '2025-11-17 15:38:12'),
(3, 'siti456', '123456', 'Siti Nurhaliza', 'peminjam', 'aktif', '2025-11-15 22:36:36', '2025-11-17 15:40:17'),
(4, 'bem_fik', '123456', 'BEM FIK', 'instansi', 'aktif', '2025-11-15 22:36:36', '2025-11-17 18:19:15'),
(7, 'andi123', '123456', 'Andi Nugraha', 'peminjam', 'aktif', '2025-11-17 16:28:54', '2025-11-17 16:28:54'),
(8, 'hima_pgsd', '123456', 'HIMA PGSD', 'instansi', 'aktif', '2025-11-17 22:09:21', '2026-01-04 13:56:47'),
(9, 'hima_udi', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'HIMA UDI', 'instansi', 'nonaktif', '2025-11-17 22:09:21', '2025-12-30 12:20:34'),
(10, 'hima_psti', '123456', 'HIMA PSTI', 'instansi', 'aktif', '2025-11-17 22:09:21', '2025-11-17 23:11:00'),
(11, 'hmst', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'HMST', 'instansi', 'aktif', '2025-11-17 22:09:21', '2025-11-17 22:09:21'),
(12, 'himatronika', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'HIMATRONIKA-AI', 'instansi', 'aktif', '2025-11-17 22:09:21', '2025-11-17 22:09:21'),
(13, 'sea', '$2a$10$OAhAE3GRk7Uc9PO881umFeYnscjVW4Gu9mIYB2STFFvVwUn2lVFpy', 'Kindi', 'peminjam', 'aktif', '2025-12-11 07:16:07', '2025-12-11 07:17:29'),
(14, 'wq', '$2a$10$ewIuY2v1sR7XRM8h3Ij/F.iL3rZYwRbXlifAL.6g9uWWv2Mrk6JW6', 'wwq', 'admin', 'aktif', '2025-12-11 07:26:33', '2025-12-15 12:54:39'),
(15, 'SISTEL', '$2a$10$Q/uEj/cQleoY6/.R2RWVDeEXSKt6gPg7L1rbKSH34Lo67H3cVNqhS', 'HIMA_SISTEL', 'peminjam', 'aktif', '2025-12-15 12:55:15', '2025-12-15 12:55:15'),
(16, 'Zaldi', '$2a$10$Fz6welVl1JTHdQrp8j.c5eW8VNxcNfBd9QkqI8xolttEq3SAS9eqO', 'Zaltot', 'peminjam', 'aktif', '2025-12-16 02:52:27', '2025-12-16 02:52:27'),
(19, 'Afdy', '$2a$10$waMKfIG67h7RUTNuFozZjeKWyLIatFIUPQMbGGVt8asFYXIGcgz4.', 'Abah', 'peminjam', 'nonaktif', '2025-12-30 12:16:29', '2025-12-30 12:20:28'),
(20, 'Abah', '$2a$10$zLieU.zQM/L2PxaTYJYUJeZ5.y7WjfDO2WgyU2rJxor84h3UY29dG', 'Arqwr', 'admin', 'nonaktif', '2025-12-30 12:20:18', '2025-12-30 12:20:22'),
(21, 'hima_frsd', '$2a$10$cbXFJXMa3JeH2sNrqIRlceKizAyG3D2bIw7ViXYWhmpJMfLh6d1/S', 'FRSD', 'instansi', 'aktif', '2026-01-01 00:04:03', '2026-01-01 00:04:03'),
(22, 'yuvygv', '111111', 'qwtpkqw', 'instansi', 'nonaktif', '2026-01-01 00:25:42', '2026-01-04 16:17:04');

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_barang_with_owner`
-- (See below for the actual view)
--
CREATE TABLE `v_barang_with_owner` (
`created_at` timestamp
,`deskripsi` text
,`foto` varchar(255)
,`id_barang` int
,`id_instansi` int
,`jumlah_tersedia` int
,`jumlah_total` int
,`kode_barang` varchar(50)
,`kondisi_barang` enum('baik','rusak ringan','rusak berat')
,`lokasi_barang` varchar(100)
,`nama_barang` varchar(100)
,`nama_lengkap` varchar(203)
,`nama_pemilik` varchar(100)
,`status` enum('tersedia','dipinjam','rusak','hilang')
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_borrow_status`
-- (See below for the actual view)
--
CREATE TABLE `v_borrow_status` (
`dl_kembali` date
,`id_instansi` int
,`id_peminjam` int
,`id_peminjaman` int
,`jumlah_baik` int
,`jumlah_hilang` int
,`jumlah_pinjam` int
,`jumlah_rusak` int
,`kode_barang` varchar(50)
,`nama_barang` varchar(100)
,`nama_instansi` varchar(100)
,`nama_peminjam` varchar(100)
,`sisa_hari` int
,`status_approval` enum('pending_instansi','approved_instansi','rejected_instansi','pending_return','approved_return','rejected_return')
,`status_barang` enum('dipinjam','dikembalikan','hilang','rusak','pending')
,`status_text` varchar(32)
,`tgl_kembali` date
,`tgl_peminjaman` date
,`tgl_pinjam` date
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_peminjaman_aktif`
-- (See below for the actual view)
--
CREATE TABLE `v_peminjaman_aktif` (
`dl_kembali` date
,`id_peminjaman` int
,`jumlah_pinjam` int
,`kode_barang` varchar(50)
,`nama_barang` varchar(100)
,`nama_peminjam` varchar(100)
,`no_telepon` varchar(15)
,`sisa_hari` int
,`status_barang` enum('dipinjam','dikembalikan','hilang','rusak','pending')
,`tgl_pinjam` date
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_statistik_barang`
-- (See below for the actual view)
--
CREATE TABLE `v_statistik_barang` (
`jumlah_dipinjam` bigint
,`jumlah_tersedia` int
,`jumlah_total` int
,`kode_barang` varchar(50)
,`kondisi_barang` enum('baik','rusak ringan','rusak berat')
,`nama_barang` varchar(100)
,`status` enum('tersedia','dipinjam','rusak','hilang')
);

-- --------------------------------------------------------

--
-- Structure for view `v_barang_with_owner`
--
DROP TABLE IF EXISTS `v_barang_with_owner`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_barang_with_owner`  AS SELECT `b`.`id_barang` AS `id_barang`, `b`.`id_instansi` AS `id_instansi`, `b`.`kode_barang` AS `kode_barang`, `b`.`nama_barang` AS `nama_barang`, `b`.`lokasi_barang` AS `lokasi_barang`, `b`.`jumlah_total` AS `jumlah_total`, `b`.`jumlah_tersedia` AS `jumlah_tersedia`, `b`.`deskripsi` AS `deskripsi`, `b`.`kondisi_barang` AS `kondisi_barang`, `b`.`status` AS `status`, `b`.`foto` AS `foto`, `b`.`created_at` AS `created_at`, `b`.`updated_at` AS `updated_at`, `i`.`nama_instansi` AS `nama_pemilik`, concat(`b`.`nama_barang`,' (',coalesce(`i`.`nama_instansi`,'Umum'),')') AS `nama_lengkap` FROM (`barang` `b` left join `instansi` `i` on((`b`.`id_instansi` = `i`.`id_instansi`)))  ;

-- --------------------------------------------------------

--
-- Structure for view `v_borrow_status`
--
DROP TABLE IF EXISTS `v_borrow_status`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_borrow_status`  AS SELECT `b`.`id_peminjaman` AS `id_peminjaman`, `b`.`kode_barang` AS `kode_barang`, `br`.`nama_barang` AS `nama_barang`, `p`.`id_peminjam` AS `id_peminjam`, `u`.`nama` AS `nama_peminjam`, `b`.`jumlah_pinjam` AS `jumlah_pinjam`, `b`.`status_barang` AS `status_barang`, `b`.`status_approval` AS `status_approval`, `b`.`tgl_peminjaman` AS `tgl_peminjaman`, `b`.`tgl_pinjam` AS `tgl_pinjam`, `b`.`dl_kembali` AS `dl_kembali`, `b`.`tgl_kembali` AS `tgl_kembali`, `b`.`jumlah_baik` AS `jumlah_baik`, `b`.`jumlah_rusak` AS `jumlah_rusak`, `b`.`jumlah_hilang` AS `jumlah_hilang`, `br`.`id_instansi` AS `id_instansi`, `i`.`nama_instansi` AS `nama_instansi`, (to_days(`b`.`dl_kembali`) - to_days(curdate())) AS `sisa_hari`, (case when (`b`.`status_approval` = 'pending_instansi') then 'Menunggu Persetujuan Instansi' when (`b`.`status_approval` = 'approved_instansi') then 'Sedang Dipinjam' when (`b`.`status_approval` = 'rejected_instansi') then 'Ditolak' when (`b`.`status_approval` = 'pending_return') then 'Menunggu Verifikasi Pengembalian' when (`b`.`status_approval` = 'approved_return') then 'Selesai' when (`b`.`status_approval` = 'rejected_return') then 'Pengembalian Ditolak' end) AS `status_text` FROM ((((`borrow` `b` join `barang` `br` on((`b`.`kode_barang` = `br`.`kode_barang`))) join `peminjam` `p` on((`b`.`id_peminjam` = `p`.`id_peminjam`))) join `user` `u` on((`p`.`id_user` = `u`.`id_user`))) left join `instansi` `i` on((`br`.`id_instansi` = `i`.`id_instansi`)))  ;

-- --------------------------------------------------------

--
-- Structure for view `v_peminjaman_aktif`
--
DROP TABLE IF EXISTS `v_peminjaman_aktif`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_peminjaman_aktif`  AS SELECT `b`.`id_peminjaman` AS `id_peminjaman`, `u`.`nama` AS `nama_peminjam`, `p`.`no_telepon` AS `no_telepon`, `br`.`kode_barang` AS `kode_barang`, `br`.`nama_barang` AS `nama_barang`, `b`.`jumlah_pinjam` AS `jumlah_pinjam`, `b`.`tgl_pinjam` AS `tgl_pinjam`, `b`.`dl_kembali` AS `dl_kembali`, (to_days(`b`.`dl_kembali`) - to_days(curdate())) AS `sisa_hari`, `b`.`status_barang` AS `status_barang` FROM (((`borrow` `b` join `peminjam` `p` on((`b`.`id_peminjam` = `p`.`id_peminjam`))) join `user` `u` on((`p`.`id_user` = `u`.`id_user`))) join `barang` `br` on((`b`.`kode_barang` = `br`.`kode_barang`))) WHERE (`b`.`status_barang` = 'dipinjam')  ;

-- --------------------------------------------------------

--
-- Structure for view `v_statistik_barang`
--
DROP TABLE IF EXISTS `v_statistik_barang`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_statistik_barang`  AS SELECT `barang`.`kode_barang` AS `kode_barang`, `barang`.`nama_barang` AS `nama_barang`, `barang`.`jumlah_total` AS `jumlah_total`, `barang`.`jumlah_tersedia` AS `jumlah_tersedia`, (`barang`.`jumlah_total` - `barang`.`jumlah_tersedia`) AS `jumlah_dipinjam`, `barang`.`status` AS `status`, `barang`.`kondisi_barang` AS `kondisi_barang` FROM `barang``barang`  ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`id_admin`),
  ADD UNIQUE KEY `id_user` (`id_user`);

--
-- Indexes for table `barang`
--
ALTER TABLE `barang`
  ADD PRIMARY KEY (`id_barang`),
  ADD UNIQUE KEY `kode_barang` (`kode_barang`),
  ADD KEY `idx_barang_status` (`status`),
  ADD KEY `idx_barang_instansi` (`id_instansi`);

--
-- Indexes for table `berita`
--
ALTER TABLE `berita`
  ADD PRIMARY KEY (`id_berita`),
  ADD KEY `created_by` (`created_by`);

--
-- Indexes for table `borrow`
--
ALTER TABLE `borrow`
  ADD PRIMARY KEY (`id_peminjaman`),
  ADD KEY `id_peminjam` (`id_peminjam`),
  ADD KEY `id_admin` (`id_admin`),
  ADD KEY `kode_barang` (`kode_barang`),
  ADD KEY `idx_borrow_status` (`status_barang`),
  ADD KEY `idx_borrow_instansi` (`id_instansi`),
  ADD KEY `idx_borrow_status_pengembalian` (`status_pengembalian`),
  ADD KEY `idx_status_approval` (`status_approval`),
  ADD KEY `idx_kode_barang` (`kode_barang`),
  ADD KEY `idx_instansi_approval` (`id_instansi_approval`),
  ADD KEY `idx_combined_status` (`status_approval`,`status_barang`);

--
-- Indexes for table `instansi`
--
ALTER TABLE `instansi`
  ADD PRIMARY KEY (`id_instansi`),
  ADD UNIQUE KEY `id_user` (`id_user`);

--
-- Indexes for table `lapor`
--
ALTER TABLE `lapor`
  ADD PRIMARY KEY (`id_laporan`),
  ADD UNIQUE KEY `no_laporan` (`no_laporan`),
  ADD KEY `id_peminjaman` (`id_peminjaman`),
  ADD KEY `kode_barang` (`kode_barang`);

--
-- Indexes for table `log_activity`
--
ALTER TABLE `log_activity`
  ADD PRIMARY KEY (`id_log`),
  ADD KEY `idx_log_username` (`username`);

--
-- Indexes for table `peminjam`
--
ALTER TABLE `peminjam`
  ADD PRIMARY KEY (`id_peminjam`),
  ADD UNIQUE KEY `id_user` (`id_user`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `idx_user_role` (`role`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin`
--
ALTER TABLE `admin`
  MODIFY `id_admin` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `barang`
--
ALTER TABLE `barang`
  MODIFY `id_barang` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `berita`
--
ALTER TABLE `berita`
  MODIFY `id_berita` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=38;

--
-- AUTO_INCREMENT for table `borrow`
--
ALTER TABLE `borrow`
  MODIFY `id_peminjaman` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `instansi`
--
ALTER TABLE `instansi`
  MODIFY `id_instansi` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `lapor`
--
ALTER TABLE `lapor`
  MODIFY `id_laporan` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `log_activity`
--
ALTER TABLE `log_activity`
  MODIFY `id_log` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=664;

--
-- AUTO_INCREMENT for table `peminjam`
--
ALTER TABLE `peminjam`
  MODIFY `id_peminjam` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id_user` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `admin`
--
ALTER TABLE `admin`
  ADD CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;

--
-- Constraints for table `barang`
--
ALTER TABLE `barang`
  ADD CONSTRAINT `fk_barang_instansi` FOREIGN KEY (`id_instansi`) REFERENCES `instansi` (`id_instansi`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `berita`
--
ALTER TABLE `berita`
  ADD CONSTRAINT `berita_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `user` (`id_user`);

--
-- Constraints for table `borrow`
--
ALTER TABLE `borrow`
  ADD CONSTRAINT `borrow_ibfk_1` FOREIGN KEY (`id_peminjam`) REFERENCES `peminjam` (`id_peminjam`),
  ADD CONSTRAINT `borrow_ibfk_2` FOREIGN KEY (`id_admin`) REFERENCES `admin` (`id_admin`),
  ADD CONSTRAINT `borrow_ibfk_3` FOREIGN KEY (`kode_barang`) REFERENCES `barang` (`kode_barang`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_borrow_instansi` FOREIGN KEY (`id_instansi`) REFERENCES `instansi` (`id_instansi`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_borrow_instansi_approval` FOREIGN KEY (`id_instansi_approval`) REFERENCES `instansi` (`id_instansi`) ON DELETE SET NULL;

--
-- Constraints for table `instansi`
--
ALTER TABLE `instansi`
  ADD CONSTRAINT `instansi_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;

--
-- Constraints for table `lapor`
--
ALTER TABLE `lapor`
  ADD CONSTRAINT `lapor_ibfk_1` FOREIGN KEY (`id_peminjaman`) REFERENCES `borrow` (`id_peminjaman`),
  ADD CONSTRAINT `lapor_ibfk_2` FOREIGN KEY (`kode_barang`) REFERENCES `barang` (`kode_barang`) ON UPDATE CASCADE;

--
-- Constraints for table `peminjam`
--
ALTER TABLE `peminjam`
  ADD CONSTRAINT `peminjam_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
