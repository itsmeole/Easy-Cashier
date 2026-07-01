# 🧾 Easy Cashier

Aplikasi **Point of Sale (POS)** Android modern yang dibangun dengan **Jetpack Compose** dan **Room Database**. Dirancang untuk memudahkan kasir dalam mengelola transaksi, produk, kategori, dan laporan penjualan — semua tersimpan secara lokal tanpa koneksi internet.

---

## ✨ Fitur Utama

| Fitur | Deskripsi |
|-------|-----------|
| 🛒 **Kasir** | Tambahkan produk ke keranjang, pilih modifier/varian, proses pembayaran tunai, dan cetak struk termal simulasi |
| 📦 **Kelola Produk** | Tambah, edit, dan hapus produk beserta harga, kategori, dan opsi modifier |
| 🏷️ **Kelola Kategori** | Buat dan ubah kategori; perubahan nama otomatis merambat ke semua produk terkait |
| 📊 **Laporan Transaksi** | Filter penjualan berdasarkan Hari Ini, 7 Hari, Bulan Ini, Semua Waktu, atau rentang tanggal kustom |
| 🏪 **Profil Toko** | Atur nama toko, alamat, dan nama kasir yang muncul di struk |
| 📄 **Cetak Ulang Struk** | Tampilkan kembali struk dari riwayat transaksi manapun dengan data kasir asli (snapshot) |
| 📱 **Adaptif Ponsel & Tablet** | Bottom Navigation (ponsel) dan Navigation Rail (tablet ≥600dp) |

---

## 🛠️ Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| Bahasa | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Arsitektur | MVVM + Repository Pattern |
| Database | Room (SQLite) |
| Reaktivitas | Kotlin Coroutines + Flow |
| Navigasi | AnimatedContent (tab-based) |
| Build System | Gradle (Kotlin DSL) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

---

## 📁 Struktur Project

```
Easy-Cashier/
├── app/
│   └── src/main/java/com/example/
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── dao/              # CategoryDao, ProductDao, TransactionDao, UserProfileDao
│       │   ├── database/         # AppDatabase (Room, versi 4)
│       │   ├── entity/           # Category, Product, Transaction, TransactionItem, UserProfile
│       │   └── repository/       # CashierRepository, CartItemModel
│       └── ui/
│           ├── screen/           # CashierScreen, ProductScreen, ReportScreen, ProfileScreen, MainScreen
│           ├── theme/            # Color, Theme, Type
│           └── viewmodel/        # CashierViewModel, CashierViewModelFactory
├── DOKUMENTASI_KODE.md
├── DOKUMENTASI_SISTEM (1).md
└── README.md
```

---

## 🚀 Cara Menjalankan

### Prasyarat
- Android Studio Iguana / Ladybug atau lebih baru
- JDK 11
- Android SDK 36

### Langkah

1. **Clone repository**
   ```bash
   git clone <url-repo>
   cd Easy-Cashier
   ```

2. **Buka di Android Studio**
   - Pilih `File > Open` dan arahkan ke folder project.

3. **Sync Gradle**
   - Android Studio akan otomatis melakukan sync dependensi.

4. **Jalankan di emulator atau perangkat fisik**
   - Klik tombol ▶ **Run** atau tekan `Shift + F10`.

> **Catatan:** Aplikasi tidak memerlukan koneksi internet. Semua data tersimpan lokal di database SQLite perangkat.

---

## 🏗️ Arsitektur

Project menggunakan pola **MVVM** dengan **Repository Pattern** dan **Reactive Programming**:

```
UI Layer (Compose Screens)
        │ collectAsState() / StateFlow
        ▼
ViewModel Layer (CashierViewModel)
        │ suspend fun / Flow
        ▼
Repository Layer (CashierRepository)
        │
  ┌─────┴──────────────────────┐
  ▼          ▼          ▼      ▼
ProductDao  TransactionDao  CategoryDao  UserProfileDao
                     │
                     ▼
           AppDatabase (Room / SQLite)
           File: "easy_cashier_db" — Versi 4
```

### Highlights Desain

- **Snapshot Data** — Nama kasir, nama toko, dan alamat toko disimpan langsung ke setiap baris transaksi saat checkout. Ini memastikan struk lama tidak berubah meskipun profil kasir diperbarui kemudian.
- **Draft State** — Perubahan profil disimpan sementara di state draft dan baru ditulis ke database saat tombol "Simpan" diklik, sehingga perubahan tidak sengaja bisa dibatalkan.
- **flatMapLatest** — Pencarian produk menggunakan `flatMapLatest` untuk membatalkan query lama secara otomatis ketika teks pencarian berubah.

---

## 🗄️ Skema Database (versi 4)

| Tabel | Keterangan |
|-------|------------|
| `products` | Daftar produk/menu |
| `categories` | Kategori produk |
| `transactions` | Header setiap transaksi (termasuk snapshot profil) |
| `transaction_items` | Detail item per transaksi (relasi ke `transactions`) |
| `user_profile` | Profil toko & kasir (singleton, id = 1) |

> ⚠️ Database menggunakan `fallbackToDestructiveMigration()` — ketika versi database diupgrade, data lama akan dihapus dan tabel dibuat ulang.

---

## 📖 Dokumentasi Tambahan

- [**DOKUMENTASI_KODE.md**](./DOKUMENTASI_KODE.md) — Referensi lengkap setiap class, interface, dan fungsi dalam project.
- [**DOKUMENTASI_SISTEM (1).md**](<./DOKUMENTASI_SISTEM (1).md>) — Penjelasan arsitektur data, alur bisnis, dan implementasi fitur utama beserta kode snippet.

---

## 👥 Tim Pengembang

Dikembangkan sebagai project kuliah. Detail tim pengembang dapat dilihat di halaman **Profil** di dalam aplikasi.
