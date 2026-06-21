package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Product
import com.example.data.entity.Transaction
import com.example.data.entity.TransactionItem
import com.example.data.entity.Category
import com.example.data.entity.UserProfile
import com.example.data.repository.CartItemModel
import com.example.data.repository.CashierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CashierViewModel(private val repository: CashierRepository) : ViewModel() {

    // --- STORE & PROFILE SETTINGS (ROOM PERSISTED) ---
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile(storeName = "Easy Cashier", storeAddress = "Kota Bandung, Jawa Barat", cashierName = "Leonard / Rezha") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile(storeName = "Easy Cashier", storeAddress = "Kota Bandung, Jawa Barat", cashierName = "Leonard / Rezha")
        )

    val storeName: StateFlow<String> = userProfile
        .map { it.storeName }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Easy Cashier"
        )

    val storeAddress: StateFlow<String> = userProfile
        .map { it.storeAddress }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Kota Bandung, Jawa Barat"
        )

    val cashierName: StateFlow<String> = userProfile
        .map { it.cashierName }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Leonard / Rezha"
        )

    // --- DRAFT/EDITING STATE ---
    private val _draftStoreName = MutableStateFlow("")
    val draftStoreName = _draftStoreName.asStateFlow()

    private val _draftStoreAddress = MutableStateFlow("")
    val draftStoreAddress = _draftStoreAddress.asStateFlow()

    private val _draftCashierName = MutableStateFlow("")
    val draftCashierName = _draftCashierName.asStateFlow()

    fun updateDraftStoreName(name: String) {
        _draftStoreName.value = name
    }

    fun updateDraftStoreAddress(address: String) {
        _draftStoreAddress.value = address
    }

    fun updateDraftCashierName(name: String) {
        _draftCashierName.value = name
    }

    // Reactive check if profile draft differs from saved database state
    val isProfileChanged: StateFlow<Boolean> = combine(
        userProfile,
        draftStoreName,
        draftStoreAddress,
        draftCashierName
    ) { profile, draftName, draftAddr, draftCashier ->
        profile.storeName != draftName.trim() ||
        profile.storeAddress != draftAddr.trim() ||
        profile.cashierName != draftCashier.trim()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

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

    // --- DYNAMIC CATEGORIES ---
    val customCategories: StateFlow<List<String>> = repository.allCategories
        .map { list -> list.map { it.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- PRODUCT SEARCH & LIST STATE ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Dynamic product flow based on search query
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = _searchQuery
        .flatMapLatest { query ->
            if (query.trim().isEmpty()) {
                repository.allProducts
            } else {
                repository.searchProducts(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCategories: StateFlow<List<String>> = combine(
        products,
        customCategories
    ) { productList, customList ->
        val dbCategories = productList.map { it.category }.filter { it.isNotEmpty() }
        (customList + dbCategories).distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addCategory(categoryName: String) {
        val trimmed = categoryName.trim()
        if (trimmed.isNotEmpty()) {
            viewModelScope.launch {
                val exists = repository.allCategories.first().any { it.name.equals(trimmed, ignoreCase = true) }
                if (!exists) {
                    repository.insertCategory(Category(trimmed))
                }
            }
        }
    }

    fun editCategory(oldName: String, newName: String) {
        val oldTrimmed = oldName.trim()
        val newTrimmed = newName.trim()
        if (oldTrimmed.isEmpty() || newTrimmed.isEmpty() || oldTrimmed == newTrimmed) return
        
        viewModelScope.launch {
            repository.updateCategoryName(oldTrimmed, newTrimmed)
            
            // Update all products in DB that have this category
            val allProds = repository.allProducts.first()
            allProds.forEach { product ->
                if (product.category.equals(oldTrimmed, ignoreCase = true)) {
                    repository.updateProduct(product.copy(category = newTrimmed))
                }
            }
        }
    }

    fun deleteCategory(categoryName: String) {
        val trimmed = categoryName.trim()
        viewModelScope.launch {
            repository.deleteCategory(Category(trimmed))
            
            val remainingCats = repository.allCategories.first().map { it.name }
            val fallback = if (remainingCats.isNotEmpty()) remainingCats.first() else "Lainnya"
            if (!remainingCats.any { it.equals(fallback, ignoreCase = true) }) {
                repository.insertCategory(Category(fallback))
            }
            
            val allProds = repository.allProducts.first()
            allProds.forEach { product ->
                if (product.category.equals(trimmed, ignoreCase = true)) {
                    repository.updateProduct(product.copy(category = fallback))
                }
            }
        }
    }

    // --- SHOPPING CART STATE ---
    private val _cartItems = MutableStateFlow<List<CartItemModel>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    val cartTotal: StateFlow<Double> = _cartItems
        .combine(MutableStateFlow(0.0)) { items, _ ->
            items.sumOf { it.product.price * it.quantity }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun addToCart(product: Product, selectedModifier: String = "") {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst {
            it.product.id == product.id && it.selectedModifier == selectedModifier
        }
        if (index != -1) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(CartItemModel(product, 1, selectedModifier))
        }
        _cartItems.value = currentList
    }

    fun removeFromCart(cartItem: CartItemModel) {
        val currentList = _cartItems.value.filterNot { it == cartItem }
        _cartItems.value = currentList
    }

    fun updateCartItemQuantity(cartItem: CartItemModel, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(cartItem)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOf(cartItem)
        if (index != -1) {
            currentList[index] = cartItem.copy(quantity = quantity)
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cashPaid.value = ""
        _checkoutSuccessState.value = null
    }

    // --- CHECKOUT & RECEIPT (STRUK) STATE ---
    private val _cashPaid = MutableStateFlow("")
    val cashPaid = _cashPaid.asStateFlow()

    fun updateCashPaid(paid: String) {
        _cashPaid.value = paid
    }

    // Pair of Transaction to its children items, indicating final receipt printout trigger
    private val _checkoutSuccessState = MutableStateFlow<Pair<Transaction, List<TransactionItem>>?>(null)
    val checkoutSuccessState = _checkoutSuccessState.asStateFlow()

    fun dismissCheckoutSuccess() {
        _checkoutSuccessState.value = null
    }

    fun executeCheckout() {
        val total = cartTotal.value
        val paidText = _cashPaid.value
        val paidAmount = paidText.toDoubleOrNull() ?: 0.0
        if (paidAmount < total) {
            // Insufficient payment
            return
        }

        val change = paidAmount - total
        val itemsInCart = _cartItems.value

        viewModelScope.launch {
            val transaction = repository.executeCheckout(
                totalAmount = total,
                cashPaid = paidAmount,
                changeAmount = change,
                cartItems = itemsInCart
            )
            // Query final items stored in db to ensure correct primary keys
            val dbItems = repository.getTransactionItems(transaction.id)
            _checkoutSuccessState.value = Pair(transaction, dbItems)
            
            // Clean cart after checkout succeeds
            _cartItems.value = emptyList()
            _cashPaid.value = ""
        }
    }

    // --- REPORTS WITH DATE FILTER STATE ---
    // Filters: 0 = Hari Ini, 1 = 7 Hari Terakhir, 2 = Bulan Ini, 3 = Semua Waktu, 4 = Kustom
    private val _reportFilterType = MutableStateFlow(3) 
    val reportFilterType = _reportFilterType.asStateFlow()

    private val _customStartDate = MutableStateFlow(System.currentTimeMillis())
    val customStartDate = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow(System.currentTimeMillis())
    val customEndDate = _customEndDate.asStateFlow()

    fun setFilterType(type: Int) {
        _reportFilterType.value = type
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _customStartDate.value = start
        _customEndDate.value = end
        _reportFilterType.value = 4 // Switch to Custom
    }

    // Dynamic flow listing only filtered sales transactions
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        _reportFilterType,
        _customStartDate,
        _customEndDate
    ) { type, customStart, customEnd ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        when (type) {
            0 -> { // Hari Ini (Today)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                Pair(start, cal.timeInMillis)
            }
            1 -> { // 7 Hari Terakhir
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val start = cal.timeInMillis
                Pair(start, System.currentTimeMillis())
            }
            2 -> { // Bulan Ini (This Month)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                Pair(start, System.currentTimeMillis())
            }
            4 -> { // Kustom
                // Ensure correct day coverage for Custom Start/End
                val calStart = Calendar.getInstance().apply { timeInMillis = customStart; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
                val calEnd = Calendar.getInstance().apply { timeInMillis = customEnd; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }
                Pair(calStart.timeInMillis, calEnd.timeInMillis)
            }
            else -> { // Semua Waktu (All Time)
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }.flatMapLatest { range ->
        if (range.first == 0L && range.second == Long.MAX_VALUE) {
            repository.allTransactions
        } else {
            repository.getTransactionsByDateRange(range.first, range.second)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Retrieve list of transaction item lines for drawing a historic item inside Report Screen
    suspend fun loadTransactionItems(transactionId: Int): List<TransactionItem> {
        return repository.getTransactionItems(transactionId)
    }

    // --- PRODUCT EDITING / ADDING OPERATIONS ---
    fun addProduct(name: String, price: Double, category: String, modifierMenu: String) {
        viewModelScope.launch {
            repository.insertProduct(
                Product(
                    name = name.trim(),
                    price = price,
                    category = category.trim(),
                    modifierMenu = modifierMenu.trim()
                )
            )
        }
    }

    fun editProduct(id: Int, name: String, price: Double, category: String, modifierMenu: String) {
        viewModelScope.launch {
            repository.updateProduct(
                Product(
                    id = id,
                    name = name.trim(),
                    price = price,
                    category = category.trim(),
                    modifierMenu = modifierMenu.trim()
                )
            )
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    init {
        // Collect user profile and pre-fill draft states
        viewModelScope.launch {
            userProfile.collect { profile ->
                _draftStoreName.value = profile.storeName
                _draftStoreAddress.value = profile.storeAddress
                _draftCashierName.value = profile.cashierName
            }
        }
    }

    // --- UTILITY FORMATTERS ---
    fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp " + formatter.format(amount)
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }
}
