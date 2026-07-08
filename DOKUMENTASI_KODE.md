# 📚 Dokumentasi Kode — Easy Cashier

> Dokumentasi ini menjelaskan setiap **class**, **interface**, **data class**, dan **fungsi** yang ada di dalam project **Easy Cashier** (Aplikasi Point of Sales Android berbasis Jetpack Compose + Room Database).

---

## 🗂️ Daftar Isi

1. [Struktur Package](#-struktur-package)
2. [Layer Data — Entity](#-layer-data--entity)
3. [Layer Data — DAO](#-layer-data--dao)
4. [Layer Data — Database](#-layer-data--database)
5. [Layer Data — Repository](#-layer-data--repository)
6. [Layer UI — ViewModel](#-layer-ui--viewmodel)
7. [Layer UI — Screen](#-layer-ui--screen)
8. [Arsitektur Keseluruhan](#-arsitektur-keseluruhan)

---

## 📁 Struktur Package

```
com.example
├── MainActivity.kt
├── data
│   ├── dao
│   │   ├── CategoryDao.kt
│   │   ├── ProductDao.kt
│   │   ├── TransactionDao.kt
│   │   └── UserProfileDao.kt
│   ├── database
│   │   └── AppDatabase.kt
│   ├── entity
│   │   ├── Category.kt
│   │   ├── Product.kt
│   │   ├── Transaction.kt
│   │   ├── TransactionItem.kt
│   │   └── UserProfile.kt
│   └── repository
│       └── CashierRepository.kt
└── ui
    ├── screen
    │   ├── CashierScreen.kt
    │   ├── MainScreen.kt
    │   ├── ProductScreen.kt
    │   ├── ProfileScreen.kt
    │   └── ReportScreen.kt
    ├── theme
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    └── viewmodel
        ├── CashierViewModel.kt
        └── CashierViewModelFactory.kt
```

---

## 🗃️ Layer Data — Entity

Entity adalah representasi tabel di dalam SQLite Room Database. Setiap `data class` yang diannotasi `@Entity` akan dipetakan menjadi sebuah tabel.

---

### `Category`

**File:** `data/entity/Category.kt`  
**Tabel Room:** `categories`

Merepresentasikan sebuah kategori produk. Digunakan untuk mengelompokkan produk (misalnya: Minuman, Makanan).

| Field | Tipe | Keterangan |
|-------|------|------------|
| `name` | `String` | Primary Key — Nama kategori (unik) |

---

### `Product`

**File:** `data/entity/Product.kt`  
**Tabel Room:** `products`

Merepresentasikan sebuah produk/menu yang dijual.

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | `Int` | Primary Key auto-generate |
| `name` | `String` | Nama produk |
| `price` | `Double` | Harga produk dalam Rupiah |
| `category` | `String` | Nama kategori produk |
| `modifierMenu` | `String` | Opsi pilihan produk dipisah koma (cth: `"Manis, Less Sugar, Tawar"`). Kosong jika tidak ada opsi. |

---

### `Transaction`

**File:** `data/entity/Transaction.kt`  
**Tabel Room:** `transactions`

Merepresentasikan sebuah header transaksi/struk penjualan.

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | `Int` | Primary Key auto-generate |
| `timestamp` | `Long` | Waktu transaksi dalam Unix milliseconds |
| `totalAmount` | `Double` | Total harga belanja |
| `cashPaid` | `Double` | Jumlah uang yang dibayarkan pelanggan |
| `changeAmount` | `Double` | Kembalian yang diberikan |
| `storeName` | `String` | Nama toko saat transaksi terjadi (snapshot) |
| `storeAddress` | `String` | Alamat toko saat transaksi terjadi (snapshot) |
| `cashierName` | `String` | Nama kasir saat transaksi terjadi (snapshot) |

---

### `TransactionItem`

**File:** `data/entity/TransactionItem.kt`  
**Tabel Room:** `transaction_items`

Merepresentasikan satu baris item di dalam sebuah transaksi (relasi one-to-many dengan `Transaction`).

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | `Int` | Primary Key auto-generate |
| `transactionId` | `Int` | Foreign key ke `Transaction.id` |
| `productId` | `Int` | ID produk pada saat pembelian |
| `productName` | `String` | Nama produk (snapshot saat beli) |
| `productPrice` | `Double` | Harga satuan produk (snapshot) |
| `quantity` | `Int` | Jumlah item yang dibeli |
| `selectedModifier` | `String` | Pilihan modifier yang dipilih pelanggan (cth: `"Less Sugar"`) |

---

### `UserProfile`

**File:** `data/entity/UserProfile.kt`  
**Tabel Room:** `user_profile`

Menyimpan informasi profil toko dan kasir. Hanya ada satu baris data dengan `id = 1` (Singleton pattern di database).

| Field | Tipe | Keterangan |
|-------|------|------------|
| `id` | `Int` | Selalu bernilai `1` (singleton) |
| `storeName` | `String` | Nama toko yang muncul di struk |
| `storeAddress` | `String` | Alamat toko yang muncul di struk |
| `cashierName` | `String` | Nama kasir yang muncul di struk |

---

## 🔌 Layer Data — DAO

DAO (Data Access Object) adalah interface yang mendefinisikan operasi-operasi database (SQL query). Room secara otomatis menghasilkan implementasinya pada saat compile time.

---

### `CategoryDao`

**File:** `data/dao/CategoryDao.kt`

Interface DAO untuk operasi CRUD tabel `categories`.

| Fungsi | Return | Keterangan |
|--------|--------|------------|
| `getAllCategories()` | `Flow<List<Category>>` | Mengambil semua kategori diurutkan A-Z. Reaktif (otomatis update UI jika data berubah). |
| `insertCategory(category)` | `Unit` | Menyisipkan kategori baru. Jika nama sudah ada, data lama diganti (`REPLACE`). |
| `deleteCategory(category)` | `Unit` | Menghapus kategori berdasarkan objek yang diberikan. |
| `updateCategoryName(oldName, newName)` | `Unit` | Mengupdate nama kategori dari `oldName` menjadi `newName`. |

---

### `ProductDao`

**File:** `data/dao/ProductDao.kt`

Interface DAO untuk operasi CRUD tabel `products`.

| Fungsi | Return | Keterangan |
|--------|--------|------------|
| `getAllProducts()` | `Flow<List<Product>>` | Mengambil semua produk diurutkan A-Z berdasarkan nama. |
| `searchProducts(query)` | `Flow<List<Product>>` | Mencari produk berdasarkan `query` yang cocok pada nama atau kategori (SQL LIKE). |
| `insertProduct(product)` | `Unit` | Menyisipkan produk baru ke database. |
| `updateProduct(product)` | `Unit` | Mengupdate data produk berdasarkan primary key (`id`). |
| `deleteProduct(product)` | `Unit` | Menghapus produk dari database. |

---

### `TransactionDao`

**File:** `data/dao/TransactionDao.kt`

Interface DAO untuk operasi transaksi dan item transaksi.

| Fungsi | Return | Keterangan |
|--------|--------|------------|
| `insertTransaction(transaction)` | `Long` | Menyimpan header transaksi. Mengembalikan ID baris yang baru dibuat. |
| `insertTransactionItems(items)` | `Unit` | Menyimpan daftar item transaksi sekaligus (bulk insert). |
| `getAllTransactions()` | `Flow<List<Transaction>>` | Mengambil semua transaksi diurutkan dari terbaru (DESC). |
| `getTransactionsByDateRange(startDate, endDate)` | `Flow<List<Transaction>>` | Memfilter transaksi berdasarkan rentang waktu (timestamp milliseconds). |
| `getTransactionItems(transactionId)` | `List<TransactionItem>` | Mengambil semua item untuk satu ID transaksi tertentu (suspend/one-shot). |

---

### `UserProfileDao`

**File:** `data/dao/UserProfileDao.kt`

Interface DAO untuk operasi profil toko (singleton row).

| Fungsi | Return | Keterangan |
|--------|--------|------------|
| `getUserProfile()` | `Flow<UserProfile?>` | Mengambil profil dengan `id = 1`. Mengembalikan `null` jika belum ada profil tersimpan. |
| `insertOrUpdateProfile(profile)` | `Unit` | Menyimpan atau memperbarui profil. Karena `id` selalu `1`, operasi ini selalu meng-overwrite data yang ada. |

---

## 🗄️ Layer Data — Database

---

### `AppDatabase`

**File:** `data/database/AppDatabase.kt`

Kelas utama Room Database. Menghubungkan semua entity dan DAO menjadi satu database SQLite bernama `"easy_cashier_db"` (versi 4).

**Anotasi:** `@Database(entities = [Product, Transaction, TransactionItem, Category, UserProfile], version = 4)`

#### Abstract Methods (DAO Accessor)

| Fungsi | Return | Keterangan |
|--------|--------|------------|
| `productDao()` | `ProductDao` | Menyediakan akses ke DAO produk |
| `transactionDao()` | `TransactionDao` | Menyediakan akses ke DAO transaksi |
| `categoryDao()` | `CategoryDao` | Menyediakan akses ke DAO kategori |
| `userProfileDao()` | `UserProfileDao` | Menyediakan akses ke DAO profil |

#### Companion Object

| Fungsi | Return | Keterangan |
|--------|--------|------------|
| `getDatabase(context)` | `AppDatabase` | **Singleton Pattern.** Mengembalikan satu instance database. Menggunakan `@Volatile` dan blok `synchronized` untuk thread safety. Jika instance belum ada, database dibuat dengan `Room.databaseBuilder`. `fallbackToDestructiveMigration()` digunakan sehingga ketika versi database berubah, data lama akan dihapus dan tabel dibuat ulang. |

---

## 🏭 Layer Data — Repository

---

### `CartItemModel`

**File:** `data/repository/CashierRepository.kt`

`data class` pembantu yang merepresentasikan satu item di keranjang belanja **di memori** (tidak disimpan ke database). Digunakan untuk menyimpan pilihan sementara selama proses pembelian berlangsung.

| Field | Tipe | Keterangan |
|-------|------|------------|
| `product` | `Product` | Referensi ke objek produk yang dipilih |
| `quantity` | `Int` | Jumlah item dalam keranjang |
| `selectedModifier` | `String` | Pilihan modifier yang dipilih user (bisa kosong `""`) |

---

### `CashierRepository`

**File:** `data/repository/CashierRepository.kt`

Repository bertindak sebagai perantara (mediator) antara ViewModel dan DAO. Menyatukan semua sumber data dan menyediakan API yang bersih kepada ViewModel.

**Constructor Parameters:** `productDao`, `transactionDao`, `categoryDao`, `userProfileDao`

#### Properties (Reactive Flows)

| Property | Tipe | Keterangan |
|----------|------|------------|
| `allProducts` | `Flow<List<Product>>` | Stream semua produk dari database |
| `allTransactions` | `Flow<List<Transaction>>` | Stream semua transaksi dari database |
| `allCategories` | `Flow<List<Category>>` | Stream semua kategori dari database |
| `userProfile` | `Flow<UserProfile?>` | Stream profil toko (nullable) |

#### Fungsi-fungsi

| Fungsi | Suspend | Return | Keterangan |
|--------|:-------:|--------|------------|
| `insertOrUpdateProfile(profile)` | ✅ | `Unit` | Menyimpan atau memperbarui profil toko |
| `insertCategory(category)` | ✅ | `Unit` | Menambahkan kategori baru |
| `deleteCategory(category)` | ✅ | `Unit` | Menghapus kategori |
| `updateCategoryName(oldName, newName)` | ✅ | `Unit` | Mengubah nama kategori |
| `searchProducts(query)` | ❌ | `Flow<List<Product>>` | Mencari produk berdasarkan teks |
| `insertProduct(product)` | ✅ | `Unit` | Menambahkan produk baru |
| `updateProduct(product)` | ✅ | `Unit` | Memperbarui data produk |
| `deleteProduct(product)` | ✅ | `Unit` | Menghapus produk |
| `getTransactionsByDateRange(start, end)` | ❌ | `Flow<List<Transaction>>` | Mengambil transaksi dalam rentang waktu tertentu |
| `getTransactionItems(transactionId)` | ✅ | `List<TransactionItem>` | Mengambil item-item dari satu transaksi |
| `executeCheckout(...)` | ✅ | `Transaction` | **Fungsi inti checkout** — lihat detail di bawah |

#### Detail `executeCheckout(...)`

```
Parameter: totalAmount, cashPaid, changeAmount, cartItems, storeName, storeAddress, cashierName
```

Alur kerja:
1. Membuat objek `Transaction` dengan timestamp saat ini.
2. Menyimpan ke database → mendapatkan generated `id`.
3. Memetakan setiap `CartItemModel` menjadi `TransactionItem` yang terhubung ke `transactionId`.
4. Menyimpan semua item sekaligus (bulk insert).
5. Mengembalikan `Transaction` dengan `id` yang sudah terisi benar.

---

## 🧠 Layer UI — ViewModel

---

### `CashierViewModelFactory`

**File:** `ui/viewmodel/CashierViewModelFactory.kt`

Factory class untuk membuat instance `CashierViewModel` dengan dependency injection manual. Diperlukan karena `CashierViewModel` membutuhkan parameter `CashierRepository` di constructor-nya (tidak bisa dibuat langsung oleh Android).

**Implements:** `ViewModelProvider.Factory`

| Fungsi | Keterangan |
|--------|------------|
| `create(modelClass)` | Membuat `AppDatabase` → membuat `CashierRepository` dengan semua DAO → membuat dan mengembalikan `CashierViewModel`. |

---

### `CashierViewModel`

**File:** `ui/viewmodel/CashierViewModel.kt`

ViewModel pusat yang menyimpan dan mengelola seluruh **state aplikasi** menggunakan `StateFlow`. Digunakan bersama oleh semua screen.

**Extends:** `ViewModel()`

---

#### Grup 1: Profil & Pengaturan Toko

| State / Fungsi | Tipe | Keterangan |
|----------------|------|------------|
| `userProfile` | `StateFlow<UserProfile>` | Profil toko aktif dari database. Default ke nilai bawaan jika belum ada. |
| `storeName` | `StateFlow<String>` | Nama toko yang diambil dari `userProfile`. |
| `storeAddress` | `StateFlow<String>` | Alamat toko dari `userProfile`. |
| `cashierName` | `StateFlow<String>` | Nama kasir dari `userProfile`. |
| `draftStoreName` | `StateFlow<String>` | Draft sementara nama toko (digunakan di form edit profil). |
| `draftStoreAddress` | `StateFlow<String>` | Draft sementara alamat toko. |
| `draftCashierName` | `StateFlow<String>` | Draft sementara nama kasir. |
| `isProfileChanged` | `StateFlow<Boolean>` | `true` jika nilai draft berbeda dengan data tersimpan di database. Digunakan untuk mengaktifkan tombol "Simpan". |
| `updateDraftStoreName(name)` | Fungsi | Memperbarui nilai draft nama toko. |
| `updateDraftStoreAddress(address)` | Fungsi | Memperbarui nilai draft alamat toko. |
| `updateDraftCashierName(name)` | Fungsi | Memperbarui nilai draft nama kasir. |
| `saveProfile()` | Fungsi (coroutine) | Menyimpan nilai-nilai draft yang sudah di-trim ke database melalui repository. |

---

#### Grup 2: Kategori

| State / Fungsi | Tipe | Keterangan |
|----------------|------|------------|
| `customCategories` | `StateFlow<List<String>>` | Daftar nama kategori dari database. |
| `allCategories` | `StateFlow<List<String>>` | Gabungan `customCategories` + kategori dari field produk (distinct). Digunakan sebagai filter chip di CashierScreen. |
| `addCategory(categoryName)` | Fungsi (coroutine) | Menambah kategori baru jika nama tidak kosong dan belum ada di database. |
| `editCategory(oldName, newName)` | Fungsi (coroutine) | Mengubah nama kategori dan secara otomatis mengupdate semua produk yang menggunakan kategori tersebut. |
| `deleteCategory(categoryName)` | Fungsi (coroutine) | Menghapus kategori. Semua produk di kategori tersebut akan dipindahkan ke kategori lain yang masih ada (atau `"Lainnya"` jika tidak ada). |

---

#### Grup 3: Produk & Pencarian

| State / Fungsi | Tipe | Keterangan |
|----------------|------|------------|
| `searchQuery` | `StateFlow<String>` | Teks query pencarian produk aktif. |
| `products` | `StateFlow<List<Product>>` | Daftar produk yang reaktif terhadap `searchQuery`. Kosong query → semua produk; ada query → hasil pencarian. Menggunakan `flatMapLatest`. |
| `updateSearchQuery(query)` | Fungsi | Memperbarui teks pencarian. |
| `addProduct(name, price, category, modifierMenu)` | Fungsi (coroutine) | Membuat dan menyimpan produk baru ke database. |
| `editProduct(id, name, price, category, modifierMenu)` | Fungsi (coroutine) | Memperbarui data produk yang sudah ada berdasarkan `id`. |
| `deleteProduct(product)` | Fungsi (coroutine) | Menghapus produk dari database. |

---

#### Grup 4: Keranjang Belanja (Cart)

| State / Fungsi | Tipe | Keterangan |
|----------------|------|------------|
| `cartItems` | `StateFlow<List<CartItemModel>>` | Daftar item dalam keranjang belanja saat ini (in-memory, bukan database). |
| `cartTotal` | `StateFlow<Double>` | Total harga semua item di keranjang (harga × qty). |
| `addToCart(product, selectedModifier)` | Fungsi | Menambah produk ke keranjang. Jika produk dengan modifier yang sama sudah ada, kuantitasnya ditambah `+1`. |
| `removeFromCart(cartItem)` | Fungsi | Menghapus satu baris item dari keranjang sepenuhnya. |
| `updateCartItemQuantity(cartItem, quantity)` | Fungsi | Mengubah jumlah item. Jika `quantity ≤ 0`, item otomatis dihapus. |
| `clearCart()` | Fungsi | Mengosongkan keranjang, mereset input bayar, dan menghapus state struk sukses. |

---

#### Grup 5: Checkout & Struk

| State / Fungsi | Tipe | Keterangan |
|----------------|------|------------|
| `cashPaid` | `StateFlow<String>` | Teks jumlah uang yang dibayarkan pelanggan (input angka). |
| `checkoutSuccessState` | `StateFlow<Pair<Transaction, List<TransactionItem>>?>` | `null` jika tidak ada struk aktif. Berisi data transaksi + item-itemnya ketika checkout berhasil → memicu `ReceiptDialog`. |
| `updateCashPaid(paid)` | Fungsi | Memperbarui input nominal bayar. |
| `dismissCheckoutSuccess()` | Fungsi | Menutup struk dengan mengeset state menjadi `null`. |
| `executeCheckout()` | Fungsi (coroutine) | Memproses pembayaran: validasi uang cukup → panggil repository → simpan ke database → ambil item dari DB → trigger tampil struk → kosongkan keranjang. |

---

#### Grup 6: Laporan & Filter Tanggal

| State / Fungsi | Tipe | Keterangan |
|----------------|------|------------|
| `reportFilterType` | `StateFlow<Int>` | Tipe filter aktif: `0`=Hari Ini, `1`=7 Hari, `2`=Bulan Ini, `3`=Semua Waktu, `4`=Kustom. |
| `customStartDate` | `StateFlow<Long>` | Timestamp awal untuk filter kustom (milliseconds). |
| `customEndDate` | `StateFlow<Long>` | Timestamp akhir untuk filter kustom (milliseconds). |
| `filteredTransactions` | `StateFlow<List<Transaction>>` | Daftar transaksi yang sudah difilter berdasarkan `reportFilterType`. Reaktif terhadap perubahan filter menggunakan `combine + flatMapLatest`. |
| `setFilterType(type)` | Fungsi | Mengubah jenis filter laporan. |
| `setCustomDateRange(start, end)` | Fungsi | Menetapkan rentang tanggal kustom dan mengubah `reportFilterType` ke `4`. |
| `loadTransactionItems(transactionId)` | Fungsi suspend | Mengambil item-item dari satu transaksi tertentu (digunakan di ReportScreen saat user expand baris transaksi). |

---

#### Grup 7: Utility Formatter

| Fungsi | Return | Keterangan |
|--------|--------|------------|
| `formatRupiah(amount)` | `String` | Memformat angka `Double` menjadi format Rupiah Indonesia (cth: `"Rp 15.000"`). Menggunakan `Locale("id", "ID")`. |
| `formatDateTime(timestamp)` | `String` | Memformat Unix timestamp menjadi string tanggal dan waktu (cth: `"01 Jun 2026, 14:30"`). |
| `formatDateOnly(timestamp)` | `String` | Memformat Unix timestamp menjadi string tanggal saja (cth: `"01 Jun 2026"`). |

---

#### `init` block

Pada saat ViewModel dibuat, blok `init` menjalankan coroutine untuk mengamati `userProfile` dan secara otomatis mengisi nilai-nilai draft (`_draftStoreName`, `_draftStoreAddress`, `_draftCashierName`) dari data database.

---

## 🎨 Layer UI — Screen

---

### `MainActivity`

**File:** `MainActivity.kt`

Entry point aplikasi Android. Meng-extend `ComponentActivity`.

| Fungsi | Keterangan |
|--------|------------|
| `onCreate(savedInstanceState)` | Inisialisasi activity: mengaktifkan edge-to-edge display dengan `enableEdgeToEdge()`, lalu memanggil `setContent` untuk menampilkan `MainScreen()` di dalam tema `MyApplicationTheme`. |

---

### `Screen` (sealed class)

**File:** `ui/screen/MainScreen.kt`

Sealed class yang merepresentasikan setiap halaman/tab navigasi di aplikasi. Setiap objek menyimpan `id` (untuk identifikasi), `title` (label navigasi), ikon aktif, dan ikon tidak aktif.

| Object | `id` | `title` | Keterangan |
|--------|------|---------|------------|
| `Screen.Cashier` | `"kasir"` | `"Kasir"` | Halaman kasir utama |
| `Screen.Products` | `"produk"` | `"Kelola Produk"` | Halaman manajemen produk |
| `Screen.Reports` | `"laporan"` | `"Laporan"` | Halaman laporan transaksi |
| `Screen.Profile` | `"profile"` | `"Profil"` | Halaman profil & pengaturan |

---

### `MainScreen`

**File:** `ui/screen/MainScreen.kt`  
**Tipe:** `@Composable`

Composable root yang menjadi kerangka navigasi utama aplikasi. Menginisialisasi `CashierViewModel` (satu instance, dipakai bersama) dan mengatur tampilan navigasi adaptif berdasarkan ukuran layar.

**Fitur:**
- Mendeteksi ukuran layar: `isWideScreen` = lebar layar ≥ 600dp.
- **Ponsel (narrow):** Menampilkan `NavigationBar` (bottom navigation) di bawah layar.
- **Tablet (wide):** Menampilkan `NavigationRail` di sisi kiri layar.
- Transisi antar halaman menggunakan animasi `fadeIn() + fadeOut()` via `AnimatedContent`.
- Satu `CashierViewModel` dibuat di sini menggunakan `CashierViewModelFactory` dan diteruskan ke semua screen sebagai parameter.

---

### `CashierScreen`

**File:** `ui/screen/CashierScreen.kt`  
**Tipe:** `@Composable`

Halaman utama kasir tempat memilih menu dan membuat pesanan.

**Parameter:**
- `viewModel: CashierViewModel`
- `modifier: Modifier`

**Fitur:**
| Fitur | Keterangan |
|-------|------------|
| Search Bar | Field pencarian produk yang terhubung ke `viewModel.searchQuery`. |
| Category Filter Chips | Daftar chip horizontal untuk memfilter produk berdasarkan kategori secara lokal (in-memory). |
| Product Grid | Grid produk adaptive (`GridCells.Adaptive(140dp)`). Klik produk: langsung tambah ke keranjang (jika tanpa modifier) atau buka dialog modifier. |
| Floating Checkout Bar | Hanya di mode ponsel. Muncul saat keranjang tidak kosong, menampilkan jumlah item, total, dan tombol "Checkout". |
| Cart Modal (ponsel) | Menampilkan `CartContent` dalam `Dialog` fullscreen saat Floating Bar diklik. |
| Cart Panel (tablet) | `CartContent` ditampilkan permanen di kolom kanan layout. |
| Modifier Dialog | `AlertDialog` untuk memilih varian modifier sebelum produk ditambahkan ke keranjang. Menampilkan daftar pilihan seperti radio button custom. |
| Receipt Dialog | `ReceiptDialog` muncul otomatis setelah `checkoutSuccessState` tidak null. |

---

### `CartContent`

**File:** `ui/screen/CashierScreen.kt`  
**Tipe:** `@Composable`

Composable reusable yang menampilkan isi keranjang belanja beserta ringkasan pembayaran.

**Parameter:**
- `viewModel: CashierViewModel`
- `modifier: Modifier`
- `onCloseClick: (() -> Unit)?` — Callback tutup (hanya ada pada dialog modal ponsel)

**Fitur:**
| Fitur | Keterangan |
|-------|------------|
| Daftar Item Keranjang | `LazyColumn` setiap item menampilkan nama, varian, harga subtotal, dan kontrol kuantitas (+/-). |
| Tombol Clear Cart | Muncul (ikon tong sampah merah) ketika keranjang tidak kosong. |
| Input Bayar Cash | `OutlinedTextField` dengan `KeyboardType.Number` hanya menerima digit. |
| Quick Pay Buttons | Tombol cepat pilih nominal: Uang Pas, 10rb, 20rb, 50rb, 100rb (hanya tampil jika ≥ total). |
| Tampilan Kembalian | Menampilkan kembalian (merah jika uang kurang). |
| Tombol "Proses Transaksi" | `enabled` hanya jika `cashPaid ≥ cartTotal` dan total > 0. |

---

### `ReceiptDialog`

**File:** `ui/screen/CashierScreen.kt`  
**Tipe:** `@Composable`

Dialog yang menampilkan simulasi struk kasir termal. Digunakan setelah checkout berhasil dan juga untuk "Lihat Struk" di halaman Laporan.

**Parameter:**
- `transaction: Transaction` — Data header transaksi
- `items: List<TransactionItem>` — Daftar item yang dibeli
- `viewModel: CashierViewModel`
- `onDismiss: () -> Unit` — Callback tutup dialog

**Konten struk (simulasi kertas termal monospace):**
1. Nama toko (uppercase) & alamat
2. Nomor transaksi, tanggal/waktu, nama kasir
3. Garis pemisah (`---`)
4. Setiap item: nama, opsi modifier, qty × harga satuan, subtotal
5. Total, uang bayar (TUNAI), kembalian
6. Pesan penutup "TERIMA KASIH — LUNAS"

---

### `ProductScreen`

**File:** `ui/screen/ProductScreen.kt`  
**Tipe:** `@Composable`

Halaman manajemen produk dan kategori dengan dua tab.

**Parameter:**
- `viewModel: CashierViewModel`
- `modifier: Modifier`

**Tab 0 — Produk:**
| Fitur | Keterangan |
|-------|------------|
| Search Bar | Pencarian lokal produk berdasarkan nama atau kategori. |
| Daftar Produk | `LazyColumn` menampilkan nama, harga, kategori (badge), dan opsi modifier setiap produk. |
| Edit Produk | Klik ikon pensil → membuka dialog form edit yang sudah terisi data produk. |
| Hapus Produk | Klik ikon tong sampah → dialog konfirmasi sebelum hapus. |
| FAB (+) | Membuka dialog form tambah produk baru. |

**Tab 1 — Kategori:**
| Fitur | Keterangan |
|-------|------------|
| Daftar Kategori | Setiap baris menampilkan nama kategori dan jumlah produk di dalamnya. |
| Edit Kategori | Membuka dialog ubah nama kategori. Perubahan nama otomatis merambat ke semua produk terkait. |
| Hapus Kategori | Dialog konfirmasi yang menampilkan info berapa produk yang akan dipindahkan ke kategori fallback. |
| FAB (+) | Membuka dialog tambah kategori baru. |

**Fungsi Internal (local):**

| Fungsi | Keterangan |
|--------|------------|
| `openAddDialog()` | Mereset semua field form produk dan membuka dialog tambah produk. |
| `openEditDialog(product)` | Mengisi field form dengan data produk yang ingin diedit, lalu membuka dialog edit. |

**Dialog-dialog:**
- Dialog Tambah/Edit Produk (field: nama, harga, dropdown kategori, modifier menu)
- Dialog Tambah Kategori Baru
- Dialog Edit Nama Kategori
- Dialog Konfirmasi Hapus Kategori (info produk yang akan dipindahkan)
- Dialog Konfirmasi Hapus Produk

---

### `SlidingTabs`

**File:** `ui/screen/ProductScreen.kt`  
**Tipe:** `@Composable`

Komponen tab kustom dengan indikator geser (sliding indicator) yang dianimasikan. Digunakan di `ProductScreen` untuk beralih antara tab "Produk" dan "Kategori".

**Parameter:**
- `selectedTab: Int` — Index tab yang aktif
- `tabs: List<String>` — Daftar judul tab
- `onTabSelected: (Int) -> Unit` — Callback ketika tab diklik
- `modifier: Modifier`

**Mekanisme animasi:**
- `BoxWithConstraints` mengukur total lebar, lalu membagi rata per tab.
- Indikator (kotak berwarna primer) bergerak halus menggunakan `animateDpAsState` dengan animasi **spring** (dampingRatio = 0.82, stiffness = 380).
- Warna teks berubah halus antara `onPrimary` (aktif) dan `onSurfaceVariant` (tidak aktif) menggunakan `animateColorAsState`.

---

### `ReportScreen`

**File:** `ui/screen/ReportScreen.kt`  
**Tipe:** `@Composable`

Halaman laporan transaksi dengan filter tanggal, statistik ringkasan, dan ekspansi detail per transaksi.

**Parameter:**
- `viewModel: CashierViewModel`
- `modifier: Modifier`

**Fitur:**
| Fitur | Keterangan |
|-------|------------|
| Dashboard Metrik | Dua kartu: Total Omset dan Total Transaksi. Dihitung langsung dari `filteredTransactions`. |
| Filter Chip | Memilih rentang waktu: Hari Ini, 7 Hari Terakhir, Bulan Ini, Semua Waktu, Kustom. |
| Dialog Filter Kustom | `AlertDialog` dengan field input manual: hari, bulan, tahun untuk tanggal mulai dan selesai. |
| Daftar Transaksi | `LazyColumn` tiap baris menampilkan nomor, waktu, dan total transaksi. |
| Expand Detail | Klik baris → item-item dimuat async dari database (dengan `coroutineScope.launch`) → ditampilkan dengan animasi `expandVertically + fadeIn`. Item yang sudah dimuat dicache di `loadedItemsMap`. |
| Lihat Struk | Tombol di dalam detail yang expanded untuk menampilkan ulang `ReceiptDialog` dari data historis. |

**State Internal:**

| State | Tipe | Keterangan |
|-------|------|------------|
| `expandedStates` | `MutableMap<Int, Boolean>` | Melacak baris transaksi mana yang sedang expand. Key = `transactionId`. |
| `loadedItemsMap` | `MutableMap<Int, List<TransactionItem>>` | Cache item transaksi yang sudah dimuat agar tidak load ulang. |
| `reprintTransactionSelected` | `Pair<Transaction, List<TransactionItem>>?` | State yang memicu tampil `ReceiptDialog` untuk cetak ulang. |
| `showCustomDateDialog` | `Boolean` | Mengontrol visibilitas dialog filter tanggal kustom. |

---

### `ProfileScreen`

**File:** `ui/screen/ProfileScreen.kt`  
**Tipe:** `@Composable`

Halaman pengaturan profil toko, informasi aplikasi, dan daftar tim pengembang.

**Parameter:**
- `viewModel: CashierViewModel`
- `modifier: Modifier`

**Fitur:**
| Fitur | Keterangan |
|-------|------------|
| Form Edit Profil | Tiga `OutlinedTextField` untuk Nama Kasir, Nama Toko, dan Alamat Toko. Menggunakan draft state dari ViewModel sehingga perubahan tidak langsung tersimpan. |
| Tombol Simpan | Hanya aktif (`enabled = isProfileChanged`) jika ada perbedaan antara draft dan data tersimpan. Memanggil `viewModel.saveProfile()`. |
| Kartu Info Aplikasi | Menampilkan nama dan deskripsi singkat aplikasi Easy Cashier. |
| Daftar Tim Pengembang | Menampilkan nama, NIM, dan kelas setiap developer dengan avatar ikon. |

---

## 📐 Arsitektur Keseluruhan

Project ini menggunakan pola **MVVM (Model-View-ViewModel)** dengan **Repository Pattern** dan **Reactive Programming** (Kotlin Coroutines + Flow).

```
┌──────────────────────────────────────────────────┐
│                   UI Layer                       │
│                                                  │
│  MainActivity                                    │
│       └── MainScreen (Navigation Host)           │
│             ├── CashierScreen                    │
│             │     ├── CartContent                │
│             │     └── ReceiptDialog              │
│             ├── ProductScreen                    │
│             │     └── SlidingTabs                │
│             ├── ReportScreen                     │
│             └── ProfileScreen                    │
└──────────────────┬───────────────────────────────┘
                   │ collectAsState() / StateFlow
                   ▼
┌──────────────────────────────────────────────────┐
│                ViewModel Layer                   │
│                                                  │
│            CashierViewModel                      │
│         (CashierViewModelFactory)                │
│                                                  │
│  State Groups:                                   │
│  - Profile & Store Settings                      │
│  - Categories Management                         │
│  - Products & Search                             │
│  - Shopping Cart (in-memory)                     │
│  - Checkout & Receipt                            │
│  - Reports & Date Filter                         │
│  - Utility Formatters                            │
└──────────────────┬───────────────────────────────┘
                   │ suspend fun / Flow
                   ▼
┌──────────────────────────────────────────────────┐
│               Repository Layer                   │
│                                                  │
│             CashierRepository                    │
│         (Single source of truth)                 │
└───┬──────────┬──────────┬───────────┬────────────┘
    │          │          │           │
    ▼          ▼          ▼           ▼
┌────────┐ ┌──────┐ ┌──────────┐ ┌──────────┐
│Product │ │Trans-│ │Category  │ │UserPro-  │
│  Dao   │ │action│ │   Dao    │ │file Dao  │
│        │ │ Dao  │ │          │ │          │
└────┬───┘ └──┬───┘ └────┬─────┘ └────┬─────┘
     └────────┴──────────┴────────────┘
                    │ SQL Queries (Room)
                    ▼
┌──────────────────────────────────────────────────┐
│              Database Layer                      │
│                                                  │
│        AppDatabase (Room / SQLite)               │
│        File: "easy_cashier_db"                   │
│        Versi: 4                                  │
│                                                  │
│  Tabel: products, transactions,                  │
│         transaction_items, categories,           │
│         user_profile                             │
└──────────────────────────────────────────────────┘
```

---

*Dokumentasi ini dibuat berdasarkan analisis kode sumber project Easy Cashier.*
