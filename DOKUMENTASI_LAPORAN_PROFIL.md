# 📘 Dokumentasi Lengkap Fitur Laporan Transaksi & Kelola Profil

Dokumen ini menjelaskan secara menyeluruh tentang arsitektur, kode, letak file, setiap **fungsi/metode**, serta **cara kerja detail kode** baik di sisi **Frontend** (Jetpack Compose & State-Flow ViewModel) maupun **Backend** (Room Database, DAO, dan Repository) untuk dua fitur tambahan aplikasi **Easy Cashier**: **Kelola Profil Toko & Kasir** serta **Laporan Transaksi**.

---

## 🗺️ 1. Gambaran Umum Arsitektur & Aliran Data

Kedua fitur ini juga mengikuti pola arsitektur **MVVM (Model-View-ViewModel)** dengan pemanfaatan stream reaktif untuk data tanggal dan draft data:

```
┌────────────────────────────────────────────────────────┐
│                   FRONTEND (UI LAYOUT)                 │
│  [ProfileScreen.kt]                 [ReportScreen.kt]  │
└───────────▲────────────────────────────────────────────┘
            │ collectAsState() / Event Triggers
┌───────────▼────────────────────────────────────────────┐
│                    VIEWMODEL LAYER                     │
│               [CashierViewModel.kt]                    │
└───────────▲────────────────────────────────────────────┘
            │ suspend fun / Kotlin Flow streams
┌───────────▼────────────────────────────────────────────┐
│                   REPOSITORY LAYER                     │
│               [CashierRepository.kt]                   │
└───────────▲────────────────────────────────────────────┘
            │ database operations (coroutine-safe)
┌───────────▼────────────────────────────────────────────┐
│                 BACKEND DATA LAYER (DAO)               │
│         [UserProfileDao]      [TransactionDao]         │
│                        │                               │
│                        ▼                               │
│               [AppDatabase (Room)]                     │
└────────────────────────────────────────────────────────┘
```

---

## 🏪 2. Fitur Kelola Profil Toko & Kasir

Fitur ini memungkinkan pengguna untuk memperbarui nama toko, alamat, dan nama kasir yang aktif bertugas. Data ini nantinya digunakan untuk informasi struk belanja.

### A. Backend & Data Layer (Model & DAO)

#### 1. Entitas Data (Database Tables)
*   **`UserProfile`** ([UserProfile.kt:L6-12](file:///d:/Personal/Ezha/Coding/Easy-Cashier/app/src/main/java/com/example/data/entity/UserProfile.kt#L6-L12)):
    Merepresentasikan baris tunggal profil toko di tabel `user_profile`.
    ```kotlin
    @Entity(tableName = "user_profile")
    data class UserProfile(
        @PrimaryKey val id: Int = 1, // Singleton pattern di database (selalu ID = 1)
        val storeName: String,
        val storeAddress: String,
        val cashierName: String
    )
    ```
    *   **Penjelasan Fungsi:** Menyimpan profil toko. Kolom `id` disetel default `1` secara permanen untuk mengimplementasikan pola Singleton di database SQLite, sehingga data di tabel ini tidak akan pernah lebih dari satu baris.

#### 2. Interface DAO (Data Access Object)
*   **`UserProfileDao`** ([UserProfileDao.kt:L11-17](file:///d:/Personal/Ezha/Coding/Easy-Cashier/app/src/main/java/com/example/data/dao/UserProfileDao.kt#L11-L17)):
    ```kotlin
    @Dao
    interface UserProfileDao {
        @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
        fun getUserProfile(): Flow<UserProfile?>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertOrUpdateProfile(profile: UserProfile)
    }
    ```
    *   **Penjelasan Fungsi:**
        *   `getUserProfile`: Mengambil satu-satunya baris data profil toko (ID = 1). Mengembalikan Flow reaktif yang dapat memancarkan data bertipe nullable (`UserProfile?`) apabila tabel masih kosong.
        *   `insertOrUpdateProfile`: Menggunakan strategi `REPLACE` untuk menyimpan atau menindih data yang ada. Karena ID objek baru selalu disetel ke `1`, metode ini akan selalu memperbarui baris data profil yang sama.

---

### B. Repositori (`CashierRepository.kt`)

Membungkus DAO untuk penyajian data profil:
```kotlin
// CashierRepository.kt L23-26
val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

suspend fun insertOrUpdateProfile(profile: UserProfile) =
    userProfileDao.insertOrUpdateProfile(profile)
```
*   **Penjelasan Fungsi:** Repositori mengekspos stream data `userProfile` agar bisa diakses secara asinkron dan meluncurkan operasi tulis `insertOrUpdateProfile` di coroutines thread.

---

### C. ViewModel Layer (`CashierViewModel.kt`)

Mengelola status draft editing dan perubahan profil toko.

#### 1. Properti State & Draft (StateFlow)
```kotlin
// CashierViewModel.kt L31-71
val userProfile: StateFlow<UserProfile> = repository.userProfile
    .map { it ?: UserProfile(storeName = "Easy Cashier", storeAddress = "Kota Bandung, Jawa Barat", cashierName = "Leonard / Rezha") }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile(storeName = "Easy Cashier", storeAddress = "Kota Bandung, Jawa Barat", cashierName = "Leonard / Rezha"))

private val _draftStoreName = MutableStateFlow("")
val draftStoreName = _draftStoreName.asStateFlow()

private val _draftStoreAddress = MutableStateFlow("")
val draftStoreAddress = _draftStoreAddress.asStateFlow()

private val _draftCashierName = MutableStateFlow("")
val draftCashierName = _draftCashierName.asStateFlow()
```
*   **Penjelasan Fungsi:**
    *   `userProfile`: Flow reaktif yang memberikan nilai default apabila profil belum pernah dikonfigurasi di database.
    *   `_draftStoreName`, `_draftStoreAddress`, `_draftCashierName`: State sementara di memori untuk menyimpan input ketikan kasir sebelum mereka benar-benar menekan tombol Simpan.

#### 2. Logika Validasi Perubahan (Draft State Pattern)
```kotlin
// CashierViewModel.kt L86-99
val isProfileChanged: StateFlow<Boolean> = combine(
    userProfile,
    draftStoreName,
    draftStoreAddress,
    draftCashierName
) { profile, draftName, draftAddr, draftCashier ->
    profile.storeName != draftName.trim() ||
    profile.storeAddress != draftAddr.trim() ||
    profile.cashierName != draftCashier.trim()
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
```
*   **Penjelasan Fungsi:** Fungsi `combine` menggabungkan data asli dari database (`userProfile`) dengan tiga variabel state draft ketikan. Jika ada satu saja perbedaan teks, `isProfileChanged` akan memancarkan nilai `true`, yang akan digunakan untuk mengaktifkan tombol simpan di UI.

#### 3. Menyimpan Perubahan Ke Database
```kotlin
// CashierViewModel.kt L101-110
fun saveProfile() {
    viewModelScope.launch {
        val updatedProfile = UserProfile(
            storeName = _draftStoreName.value.trim(),
            storeAddress = _draftStoreAddress.value.trim(),
            cashierName = _draftCashierName.value.trim()
        )
        repository.insertOrUpdateProfile(updatedProfile)
    }
}
```
*   **Penjelasan Fungsi:** Membaca data ketikan draft terbaru, membungkusnya ke entitas `UserProfile` baru (dengan ID default = 1), lalu menyimpannya ke SQLite secara asinkron di dalam scope coroutine.

---

### D. Frontend / UI Layer (`ProfileScreen.kt`)

*   **Penyusunan Form dan Tombol Simpan Reaktif** ([ProfileScreen.kt:L99-132](file:///d:/Personal/Ezha/Coding/Easy-Cashier/app/src/main/java/com/example/ui/screen/ProfileScreen.kt#L99-L132)):
    ```kotlin
    OutlinedTextField(
        value = cashierName,
        onValueChange = { viewModel.updateDraftCashierName(it) },
        label = { Text("Nama Kasir") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    // ... (Text field nama & alamat toko) ...
    Button(
        onClick = { viewModel.saveProfile() },
        enabled = isProfileChanged,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Text("Simpan Perubahan")
    }
    ```
    *   **Penjelasan Fungsi:** Komponen UI Compose ini merender form edit profil toko. Teks input terikat langsung ke StateFlow draft ViewModel. Properti `enabled` pada tombol `Button` disinkronkan dengan `isProfileChanged` reaktif sehingga tidak ada pemborosan penulisan data ke SQLite jika data input tidak berubah.

---

## 📊 3. Fitur Laporan Transaksi

Fitur ini menyajikan ringkasan omset penjualan, jumlah total struk belanja, visualisasi filter waktu, dan detail item belanja yang terjual di masa lampau.

### A. Backend & Data Layer (Model & DAO)

Fitur Laporan menggunakan entitas **`Transaction`** dan **`TransactionItem`** yang sama dengan fitur Kasir.
*   **Kueri DAO Rentang Tanggal** ([TransactionDao.kt:L23-24](file:///d:/Personal/Ezha/Coding/Easy-Cashier/app/src/main/java/com/example/data/dao/TransactionDao.kt#L23-L24)):
    ```kotlin
    @Query("SELECT * FROM transactions WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    ```
    *   **Penjelasan Fungsi:** Mengambil semua header transaksi belanja yang dibuat dalam batas waktu millisecond `:startDate` dan `:endDate` secara berurutan dari transaksi terbaru ke lama.

---

### B. ViewModel Layer (`CashierViewModel.kt`)

#### 1. Filter Kategori Waktu Reaktif (StateFlow)
```kotlin
// CashierViewModel.kt L310-380
private val _reportFilterType = MutableStateFlow(3) // Default: Semua Waktu (3)
val reportFilterType = _reportFilterType.asStateFlow()

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
val filteredTransactions: StateFlow<List<Transaction>> = combine(
    _reportFilterType,
    _customStartDate,
    _customEndDate
) { type, customStart, customEnd ->
    val cal = Calendar.getInstance()
    // Reset jam ke 00:00:00 untuk awal hari
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
    
    when (type) {
        0 -> Pair(cal.timeInMillis, System.currentTimeMillis()) // Hari Ini
        1 -> { cal.add(Calendar.DAY_OF_YEAR, -7); Pair(cal.timeInMillis, System.currentTimeMillis()) } // 7 Hari
        2 -> { cal.set(Calendar.DAY_OF_MONTH, 1); Pair(cal.timeInMillis, System.currentTimeMillis()) } // Bulan Ini
        4 -> { // Kustom Tanggal
            val calStart = Calendar.getInstance().apply { timeInMillis = customStart; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }
            val calEnd = Calendar.getInstance().apply { timeInMillis = customEnd; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) }
            Pair(calStart.timeInMillis, calEnd.timeInMillis)
        }
        else -> Pair(0L, Long.MAX_VALUE) // Semua Waktu (Type = 3)
    }
}.flatMapLatest { range ->
    if (range.first == 0L && range.second == Long.MAX_VALUE) {
        repository.allTransactions
    } else {
        repository.getTransactionsByDateRange(range.first, range.second)
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```
*   **Penjelasan Fungsi:** Logika bisnis penyaringan waktu:
    1. Fungsi `combine` memantau tipe filter aktif dan rentang tanggal kustom.
    2. Menghitung timestamp Epoch Millisecond batas awal dan akhir menggunakan `Calendar` API.
    3. Operator `flatMapLatest` mengeksekusi kueri filter database yang sesuai. Bila filter disetel ke "Semua Waktu", ia membaca seluruh riwayat tanpa filter tanggal.

#### 2. Lazy Loading Detail Item Struk
```kotlin
// CashierViewModel.kt L383-385
suspend fun loadTransactionItems(transactionId: Int): List<TransactionItem> {
    return repository.getTransactionItems(transactionId)
}
```
*   **Penjelasan Fungsi:** Fungsi ini dipanggil secara asinkron untuk mengambil rincian detail belanja hanya ketika baris transaksi diperluas oleh kasir di layar laporan. Ini mencegah pengambilan seluruh data relasi besar secara massal di awal kueri, yang sangat menghemat memori perangkat.

---

### C. Frontend / UI Layer (`ReportScreen.kt`)

*   **Penyaringan Tombol Tanggal Carousel** ([ReportScreen.kt:L202-244](file:///d:/Personal/Ezha/Coding/Easy-Cashier/app/src/main/java/com/example/ui/screen/ReportScreen.kt#L202-L244)):
    ```kotlin
    LazyRow(
        modifier = Modifier.padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filtersList.size) { index ->
            val typeName = filtersList[index]
            val isSelected = filterType == index
            
            Surface(
                modifier = Modifier
                    .clickable {
                        if (index == 4) {
                            showCustomDateDialog = true
                        } else {
                            viewModel.setFilterType(index)
                        }
                    }
                    .clip(RoundedCornerShape(12.dp)),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            ) {
                Text(text = typeName, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
            }
        }
    }
    ```
    *   **Penjelasan Fungsi:** Komponen gulir horizontal yang merender tombol-tombol filter waktu. Mengetuk tombol selain "Kustom" akan langsung mengubah state filter di ViewModel. Mengetuk opsi "Kustom" akan memunculkan dialog masukan penanggalan.

*   **Expandable List & Lazy-Loaded Detail Rows** ([ReportScreen.kt:L272-286](file:///d:/Personal/Ezha/Coding/Easy-Cashier/app/src/main/java/com/example/ui/screen/ReportScreen.kt#L272-L286)):
    ```kotlin
    val isExpanded = expandedStates[transaction.id] == true
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val newState = !isExpanded
                expandedStates[transaction.id] = newState
                if (newState && !loadedItemsMap.containsKey(transaction.id)) {
                    coroutineScope.launch {
                        val items = viewModel.loadTransactionItems(transaction.id)
                        loadedItemsMap[transaction.id] = items
                    }
                }
            }
    ) {
        // ... (Header Transaksi) ...
        AnimatedVisibility(visible = isExpanded) {
            Column {
                val itemsList = loadedItemsMap[transaction.id] ?: emptyList()
                itemsList.forEach { item ->
                    Text(text = "${item.productName} x${item.quantity} ${viewModel.formatRupiah(item.productPrice)}")
                }
            }
        }
    }
    ```
    *   **Penjelasan Fungsi:** Desain list interaktif yang hemat resource memori:
        *   Komponen menyimpan status perluasan baris di map `expandedStates`.
        *   Ketika baris diklik dan **diperluas (expanded)**, program mengecek apakah rincian item belanja sudah ada di map `loadedItemsMap`.
        *   Jika data belum ada di memori local UI, program meluncurkan scope coroutine asinkron untuk mengambil detail item dari database (`viewModel.loadTransactionItems`) secara dinamis.
        *   Komponen `AnimatedVisibility` memberikan visual transisi buka-tutup baris yang mulus untuk menyajikan detail produk belanja tersebut.
