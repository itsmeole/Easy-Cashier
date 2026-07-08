# Dokumentasi Sistem Aplikasi Easy Cashier (POS)

Dokumen ini menjelaskan secara detail arsitektur data, model entitas yang digunakan, fitur-fitur utama, letak baris kode penting, serta penjelasan bagaimana data tersebut dipakai dan ditampilkan di dalam aplikasi **Easy Cashier (Point of Sale)** berbasis Jetpack Compose dan Room Database.

---

## Daftar Isi
1. [Arsitektur Data & Model Entitas (Database Schema)](#1-arsitektur-data--model-entitas-database-schema)
2. [Aliran Penggunaan & Penyajian Data (Data Flow & UI Display)](#2-aliran-penggunaan--penyajian-data-data-flow--ui-display)
3. [Fitur Kelola Profil Toko & Kasir (Profile Screen)](#3-fitur-kelola-profil-toko-kasir-profile-screen)
4. [Fitur Transaksi & Kasir (Cashier Screen)](#4-fitur-transaksi-kasir-cashier-screen)
5. [Fitur Kelola Produk & Kategori (Product Screen)](#5-fitur-kelola-produk-kategori-product-screen)
6. [Fitur Laporan Transaksi (Report Screen)](#6-fitur-laporan-transaksi-report-screen)
7. [Arsitektur Room Database & Migrasi](#7-arsitektur-room-database--migrasi)

---

## 1. Arsitektur Data & Model Entitas (Database Schema)

Aplikasi Easy Cashier menggunakan database relasional SQLite lokal melalui library **Jetpack Room**. Berikut adalah jenis-jenis data yang disimpan dan digunakan oleh sistem:

### A. Data Profil Pengguna/Toko (`UserProfile`)
Menyimpan identitas aktif toko dan kasir yang bertugas saat ini.
*   **Kelas Model:** `com.example.data.entity.UserProfile`
*   **Struktur Kolom:**
    *   `id` (Int, Primary Key): ID tunggal untuk baris profil.
    *   `storeName` (String): Nama toko POS aktif.
    *   `storeAddress` (String): Alamat fisik toko.
    *   `cashierName` (String): Nama kasir aktif yang bertugas.

### B. Data Kategori (`Category`)
Menyimpan klasifikasi menu atau barang untuk mempermudah penyaringan produk.
*   **Kelas Model:** `com.example.data.entity.Category`
*   **Struktur Kolom:**
    *   `id` (Int, Primary Key, Auto-Increment): ID unik kategori.
    *   `name` (String): Nama kategori (misal: "Makanan", "Minuman", "Cemilan").

### C. Data Produk (`Product`)
Menyimpan inventaris barang dagangan yang dapat ditambahkan ke keranjang belanja.
*   **Kelas Model:** `com.example.data.entity.Product`
*   **Struktur Kolom:**
    *   `id` (Int, Primary Key, Auto-Increment): ID unik produk.
    *   `name` (String): Nama produk dagangan.
    *   `price` (Double): Harga satuan produk.
    *   `categoryId` (Int): ID Kategori relasional (menghubungkan produk ke tabel `Category`).
    *   `modifierMenu` (String): String mentah berisi daftar varian yang dipisahkan koma (misal: "Pedas, Sedang, Manis").
    *   `imagePath` (String): Path lokal atau URL gambar produk (jika ada).

### D. Data Transaksi (`Transaction`)
Menyimpan ringkasan informasi keuangan setiap transaksi penjualan yang sukses (Order Header). Kolom identitas toko dan kasir di-*snapshot* langsung ke sini demi alasan akurasi riwayat (History Isolation).
*   **Kelas Model:** `com.example.data.entity.Transaction`
*   **Struktur Kolom:**
    *   `id` (Int, Primary Key, Auto-Increment): ID unik struk transaksi.
    *   `timestamp` (Long): Waktu transaksi dalam format UNIX Epoch Millisecond.
    *   `totalAmount` (Double): Total nilai belanja yang harus dibayar.
    *   `cashPaid` (Double): Jumlah uang tunai yang diserahkan pembeli.
    *   `changeAmount` (Double): Jumlah uang kembalian.
    *   `storeName` (String): **[Snapshot]** Nama toko saat transaksi dilakukan.
    *   `storeAddress` (String): **[Snapshot]** Alamat toko saat transaksi dilakukan.
    *   `cashierName` (String): **[Snapshot]** Nama kasir yang bertugas saat transaksi dilakukan.

### E. Data Item Detail Transaksi (`TransactionItem`)
Menyimpan rincian produk yang dibeli di dalam satu nomor transaksi tertentu (Order Line / Detail).
*   **Kelas Model:** `com.example.data.entity.TransactionItem`
*   **Struktur Kolom:**
    *   `id` (Int, Primary Key, Auto-Increment): ID unik baris item belanja.
    *   `transactionId` (Int): ID Transaksi induk (Relasi Foreign Key ke `Transaction`).
    *   `productId` (Int): ID Produk yang dibeli.
    *   `productName` (String): Nama produk saat transaksi terjadi (mencegah nama berubah di struk lama jika produk diedit di masa depan).
    *   `productPrice` (Double): Harga produk saat transaksi terjadi (mencegah perubahan harga lama di laporan keuangan).
    *   `quantity` (Int): Jumlah pcs item yang dibeli.
    *   `modifierSelected` (String): Varian modifier terpilih (misal: "Pedas").

---

## 2. Aliran Penggunaan & Penyajian Data (Data Flow & UI Display)

Bagaimana kelima entitas di atas saling berkomunikasi, digunakan dalam proses bisnis, dan ditampilkan di layar UI? Berikut diagram penjelasannya:

```
[UserProfile]  ──(di-snapshot saat Checkout)──► [Transaction] (Laporan & Cetak Struk)
                                                      │
[Product]     ──(ditambahkan ke keranjang)──► [TransactionItem] (Detail Item Struk)
    │
[Category] (Mengelompokkan Produk di Katalog)
```

### A. Bagaimana Data Dipakai (Business Logic)
1.  **Penyaringan Katalog:** Di layar kasir, database memuat semua `Product` dan `Category`. Saat salah satu tab kategori diklik, aplikasi menyaring daftar produk berdasarkan `categoryId`.
2.  **Penyusunan Struk Belanja:** Kasir memilih `Product`, memilih modifier (dari kolom `modifierMenu`), memasukkan kuantitas, lalu menambahkannya ke keranjang belanja virtual.
3.  **Proses Checkout:**
    *   Aplikasi membaca nilai profil aktif (`UserProfile`) seperti Nama Kasir, Nama Toko, dan Alamat Toko.
    *   Disimpan entitas `Transaction` baru yang merekam total uang, kembalian, tanggal, beserta **Snapshot identitas profil aktif**.
    *   Disimpan daftar entitas `TransactionItem` yang merekam produk-produk dari keranjang belanja dan dihubungkan ke `transactionId` yang baru saja dibuat.

### B. Bagaimana Data Ditampilkan (UI Presentation)
1.  **Informasi Header Kasir:** Nama Toko dan Nama Kasir dari `UserProfile` ditampilkan di bagian atas layar kasir agar kasir tahu akun siapa yang sedang bertugas.
2.  **Kartu Produk:** Nama, harga, dan gambar dari `Product` disajikan dalam bentuk Grid/List card yang menarik di tab katalog produk.
3.  **Dialog Simulasi Cetak Struk:**
    *   Menampilkan data `storeName`, `storeAddress`, dan `cashierName` **langsung dari data snapshot** yang ada di tabel `Transaction` (bukan dari profil aktif saat ini).
    *   Menampilkan daftar barang belanjaan dari `TransactionItem` yang direlasikan berdasarkan ID transaksi tersebut.
4.  **Halaman Laporan Penjualan:**
    *   Menghitung total uang masuk harian dengan menjumlahkan `totalAmount` dari seluruh baris di tabel `Transaction`.
    *   Menyajikan grafik penjualan berbasis waktu transaksi (`timestamp`).

---

## 3. Fitur Kelola Profil Toko & Kasir (Profile Screen)

Fitur ini digunakan untuk mengatur identitas toko (Nama Toko, Alamat Toko) serta Nama Kasir yang bertugas saat ini.

### A. Mekanisme Draft & Simpan Perubahan
Agar pengguna bisa melakukan pembatalan atau melihat perbandingan sebelum menyimpan, sistem menggunakan konsep *state draft* terpisah dari data profil yang tersimpan aktif di database.

*   **Berkas:** `/app/src/main/java/com/example/ui/viewmodel/CashierViewModel.kt`
*   **Baris Kode Penting:**
    ```kotlin
    // Menghitung apakah ada perubahan pada input profil dibanding data asli di database
    val isProfileChanged: StateFlow<Boolean> = combine(
        userProfile,
        _draftStoreName,
        _draftStoreAddress,
        _draftCashierName
    ) { profile, draftName, draftAddress, draftCashier ->
        profile.storeName != draftName.trim() ||
        profile.storeAddress != draftAddress.trim() ||
        profile.cashierName != draftCashier.trim()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    ```
*   **Penjelasan Fungsi:**
    Kode di atas menggunakan operator `combine` dari Kotlin Flow untuk membandingkan secara real-time isi *text field* sementara (draft) dengan data profil aktual di database (`userProfile`). Tombol "Simpan Perubahan" di UI hanya akan aktif (`enabled`) jika nilai `isProfileChanged` bernilai `true`.

---

### B. Menghilangkan Fokus Keyboard (Clear Focus) Saat Ketuk di Luar Input
Untuk meningkatkan pengalaman pengguna (UX), saat pengguna mengetuk area kosong di luar text field, fokus input keyboard akan otomatis dilepas.

*   **Berkas:** `/app/src/main/java/com/example/ui/screen/ProfileScreen.kt`
*   **Baris Kode Penting:**
    ```kotlin
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    )
    ```
*   **Penjelasan Fungsi:**
    Menggunakan `Modifier.clickable` tanpa efek visual riak air (`indication = null`) dengan `MutableInteractionSource` agar area kosong pada layar bisa mendeteksi ketukan. Ketika diketuk, `focusManager.clearFocus()` dipanggil untuk menutup keyboard dan melepaskan kursor dari text field yang aktif.

---

## 4. Fitur Transaksi & Kasir (Cashier Screen)

Ini adalah pusat operasi kasir tempat memilih produk, memilih modifier varian, mengelola keranjang belanja, serta mencetak struk belanja thermal simulasi.

### A. Fitur Pemisahan Modifier Menggunakan Koma `,` Menjadi Selectable Item (Chips)
Saat produk dikonfigurasi dengan modifier (misal: "Pedas, Sedang, Manis"), sistem secara cerdas memisahkannya menjadi tombol pilihan (*chips*) tersendiri di dialog kasir.

*   **Berkas:** `/app/src/main/java/com/example/ui/screen/CashierScreen.kt`
*   **Baris Kode Penting:**
    ```kotlin
    val modifiersList = product.modifierMenu.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    ```
*   **Penjelasan Fungsi:**
    *   `split(",")`: Memecah satu string teks modifier yang dipisahkan oleh tanda koma menjadi daftar (`List<String>`).
    *   `map { it.trim() }`: Menghapus spasi berlebih di awal atau akhir kata hasil pemisahan.
    *   `filter { it.isNotEmpty() }`: Memastikan tidak ada item kosong yang ikut masuk ke daftar tombol jika pengguna tidak sengaja menulis koma ganda (misal: "Keju,,Cokelat").
    *   Hasilnya dipetakan langsung ke komponen Row berisi `FilterChip` interaktif sehingga kasir tinggal mengeklik salah satu varian sebelum memasukkan item ke keranjang belanja.

---

### B. Snapshot Data Profil Pada Transaksi Sebelumnya (Kasir Tidak Berubah Saat Profil Diganti)
Masalah umum sistem POS adalah jika nama kasir diubah di profil, semua riwayat struk lama ikut berganti nama kasir. Aplikasi ini menyelesaikan masalah tersebut dengan melakukan **Snapshot Data** (menyimpan data nama kasir, nama toko, dan alamat toko secara langsung ke dalam baris transaksi saat transaksi sukses dibuat).

#### 1. Struktur Entitas Transaksi Berubah
*   **Berkas:** `/app/src/main/java/com/example/data/entity/Transaction.kt`
*   **Baris Kode Penting:**
    ```kotlin
    @Entity(tableName = "transactions")
    data class Transaction(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val timestamp: Long,
        val totalAmount: Double,
        val cashPaid: Double,
        val changeAmount: Double,
        val storeName: String = "",
        val storeAddress: String = "",
        val cashierName: String = ""
    )
    ```
*   **Penjelasan Fungsi:**
    Kolom `storeName`, `storeAddress`, dan `cashierName` disimpan langsung pada baris tabel `transactions`. Ini bertindak sebagai catatan sejarah (*historical snapshot*) yang tidak terikat langsung pada tabel profil aktif.

#### 2. Proses Menyimpan Snapshot Saat Checkout
*   **Berkas:** `/app/src/main/java/com/example/ui/viewmodel/CashierViewModel.kt`
*   **Baris Kode Penting:**
    ```kotlin
    val activeProfile = userProfile.value

    viewModelScope.launch {
        val transaction = repository.executeCheckout(
            totalAmount = total,
            cashPaid = paidAmount,
            changeAmount = change,
            cartItems = itemsInCart,
            storeName = activeProfile.storeName,
            storeAddress = activeProfile.storeAddress,
            cashierName = activeProfile.cashierName
        )
        // ...
    }
    ```
*   **Penjelasan Fungsi:**
    Sebelum fungsi checkout dipanggil, aplikasi mengambil nilai profil aktif saat itu (`activeProfile`). Nilai tersebut dikirimkan ke repositori untuk disimpan bersama data angka transaksi lainnya.

#### 3. Menampilkan Snapshot di Struk thermal
*   **Berkas:** `/app/src/main/java/com/example/ui/screen/CashierScreen.kt`
*   **Baris Kode Penting:**
    ```kotlin
    val currentStoreName by viewModel.storeName.collectAsState()
    val currentStoreAddress by viewModel.storeAddress.collectAsState()
    val currentCashierName by viewModel.cashierName.collectAsState()

    val storeName = transaction.storeName.ifBlank { currentStoreName }
    val storeAddress = transaction.storeAddress.ifBlank { currentStoreAddress }
    val cashierName = transaction.cashierName.ifBlank { currentCashierName }
    ```
*   **Penjelasan Fungsi:**
    Komponen dialog cetak struk (`ReceiptDialog`) mengecek apakah transaksi memiliki data snapshot (`transaction.cashierName`). Jika ya, struk akan mencetak nama kasir lama yang tersimpan dalam snapshot tersebut. Jika kosong (misal untuk data lama sebelum migrasi), sistem akan menggunakan data profil aktif sebagai cadangan (*fallback*).

---

### C. Menghilangkan Fokus Keyboard di Layar Kasir
*   **Berkas:** `/app/src/main/java/com/example/ui/screen/CashierScreen.kt`
*   **Baris Kode Penting:**
    ```kotlin
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    )
    ```
*   **Penjelasan Fungsi:**
    Sama seperti pada profil, membungkus seluruh konten layar kasir dengan Box yang interaktif melepaskan fokus input saat kasir mengetuk bagian luar kolom input jumlah bayar atau kolom pencarian produk.

---

## 5. Fitur Kelola Produk & Kategori (Product Screen)

Digunakan oleh pemilik toko untuk menambahkan, mengubah, atau menghapus produk dagangan beserta kategori yang dikelompokkan kustom.

### A. Menghilangkan Fokus Keyboard di Layar Produk & Kategori
*   **Berkas:** `/app/src/main/java/com/example/ui/screen/ProductScreen.kt`
*   **Baris Kode Penting:**
    ```kotlin
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .padding(paddingValues)
            .padding(16.dp)
    )
    ```
*   **Penjelasan Fungsi:**
    Saat mengelola atau menambahkan produk baru, mengetuk area kosong di layar daftar produk akan menutup keyboard virtual secara instan tanpa mengganggu proses navigasi kasir.

---

## 6. Fitur Laporan Transaksi (Report Screen)

Fitur ini menyajikan performa penjualan harian, ringkasan pendapatan, jumlah total struk belanja, serta fitur cetak ulang struk (*reprint receipt*) untuk transaksi yang telah lampau.

*   **Berkas:** `/app/src/main/java/com/example/ui/screen/ReportScreen.kt`
*   **Fungsi Utama:**
    Menampilkan visualisasi ringkas penjualan, dan meluncurkan dialog struk termal (`ReceiptDialog`) menggunakan data transaksi lama. Berkat adanya fitur snapshot di bagian sebelumnya, struk lama yang dicetak ulang akan tetap menampilkan nama kasir asli yang melayani transaksi tersebut di masa lalu, meskipun nama profil kasir aktif sekarang sudah berubah.

---

## 7. Arsitektur Room Database & Migrasi

### A. Skema Database
*   **Berkas:** `/app/src/main/java/com/example/data/database/AppDatabase.kt`
*   **Kode Konfigurasi Migrasi Versi 4:**
    ```kotlin
    @Database(
        entities = [Product::class, Transaction::class, TransactionItem::class, Category::class, UserProfile::class],
        version = 4,
        exportSchema = false
    )
    abstract class AppDatabase : RoomDatabase() {
        // ...
    }
    ```
*   **Penjelasan Fungsi:**
    Versi database dinaikkan ke **Versi 4** untuk mengakomodasi penambahan kolom snapshot (`storeName`, `storeAddress`, dan `cashierName`) pada tabel `transactions`. Room akan memperbarui struktur skema tabel secara otomatis saat aplikasi berjalan.

---
*Dokumentasi ini disusun untuk mempermudah pemeliharaan kode dan penambahan fitur-fitur baru di masa yang akan datang.*
