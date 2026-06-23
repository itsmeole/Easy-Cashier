package com.example.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Product
import com.example.ui.viewmodel.CashierViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductScreen(
    viewModel: CashierViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val categoriesList by viewModel.customCategories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Produk, 1 = Kategori

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Dialog States
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    
    var showCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var categoryNameError by remember { mutableStateOf(false) }
    
    var showEditCategoryDialog by remember { mutableStateOf(false) }
    var editingCategoryName by remember { mutableStateOf("") }
    var tempEditCategoryName by remember { mutableStateOf("") }
    var editCategoryError by remember { mutableStateOf(false) }

    var showDeleteCategoryConfirmation by remember { mutableStateOf(false) }
    var deletingCategoryName by remember { mutableStateOf("") }

    var showDeleteProductConfirmation by remember { mutableStateOf(false) }
    var deletingProduct by remember { mutableStateOf<Product?>(null) }

    // Form States for Products
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var modifierMenu by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    fun openAddDialog() {
        editingProduct = null
        name = ""
        priceText = ""
        category = categoriesList.firstOrNull() ?: ""
        modifierMenu = ""
        nameError = false
        priceError = false
        showDialog = true
    }

    fun openEditDialog(product: Product) {
        editingProduct = product
        name = product.name
        priceText = product.price.toInt().toString()
        category = product.category
        modifierMenu = product.modifierMenu
        nameError = false
        priceError = false
        showDialog = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeTab == 0) {
                        openAddDialog()
                    } else {
                        newCategoryName = ""
                        categoryNameError = false
                        showCategoryDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_product_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (activeTab == 0) "Tambah Produk Baru" else "Tambah Kategori Baru",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Kelola Produk",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- DESIGN SLIDE TABS ---
            SlidingTabs(
                selectedTab = activeTab,
                tabs = listOf("Produk", "Kategori"),
                onTabSelected = { activeTab = it },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (activeTab == 0) {
                // ================== TAB: PRODUK ==================
                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = "No Products",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Belum ada Produk",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Klik tombol Tambah di pojok kanan bawah.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari produk...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("product_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(30.dp)
                    )

                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "No Results",
                                    modifier = Modifier.size(72.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Produk tidak ditemukan",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Coba cari dengan kata kunci lain.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { openEditDialog(product) },
                                            onLongClick = { openEditDialog(product) }
                                        )
                                        .testTag("product_item_${product.id}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = product.category,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = product.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = viewModel.formatRupiah(product.price),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            if (product.modifierMenu.isNotEmpty()) {
                                                Text(
                                                    text = "Opsi: ${product.modifierMenu}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // Manage buttons
                                        Row {
                                            IconButton(
                                                onClick = { openEditDialog(product) },
                                                modifier = Modifier.testTag("edit_product_${product.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    deletingProduct = product
                                                    showDeleteProductConfirmation = true
                                                },
                                                modifier = Modifier.testTag("delete_product_${product.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus",
                                                    tint = Color.Red.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ================== TAB: KATEGORI ==================

                if (categoriesList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "No Categories",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Belum ada Kategori",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Klik tombol Tambah di pojok kanan bawah.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categoriesList) { categoryName ->
                            val productCount = products.count { it.category.equals(categoryName, ignoreCase = true) }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("category_item_$categoryName"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = categoryName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "$productCount Produk",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Action buttons for custom category edit and delete
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingCategoryName = categoryName
                                                tempEditCategoryName = categoryName
                                                editCategoryError = false
                                                showEditCategoryDialog = true
                                            },
                                            modifier = Modifier.testTag("edit_category_$categoryName")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Kategori",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                deletingCategoryName = categoryName
                                                showDeleteCategoryConfirmation = true
                                            },
                                            modifier = Modifier.testTag("delete_category_$categoryName")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Kategori",
                                                tint = Color.Red.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- SLIDING TAB COMPOSABLE ---
    // Beautiful dynamic Segmented-like indicator slider for the tabs
    // Standard Material Theme 3 Primary & Primary Container matching project color schemes.

    // Add / Edit Product Alert Dialog
    if (showDialog) {
        var expandedDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (editingProduct == null) "Tambah Produk Baru" else "Ubah Produk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = it.isEmpty()
                        },
                        label = { Text("Nama Produk") },
                        isError = nameError,
                        supportingText = { if (nameError) Text("Nama tidak boleh kosong") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_name_input")
                    )

                    // Price Field
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it.filter { char -> char.isDigit() }
                            priceError = priceText.isEmpty() || (priceText.toDoubleOrNull() ?: 0.0) <= 0
                        },
                        label = { Text("Harga (Rupiah)") },
                        prefix = { Text("Rp ") },
                        isError = priceError,
                        supportingText = { if (priceError) Text("Harga harus valid") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_price_input")
                    )

                    // Category Selector Box
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("product_category_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            categoriesList.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        category = option
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Modifier Menu Field
                    OutlinedTextField(
                        value = modifierMenu,
                        onValueChange = { modifierMenu = it },
                        label = { Text("Menu Modifier / Pilihan Opsi") },
                        placeholder = { Text("cth: Manis, Less Sugar, Tawar") },
                        supportingText = { Text("Pisahkan dengan koma (,) jika lebih dari satu") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_modifier_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        nameError = name.trim().isEmpty()
                        val priceNum = priceText.toDoubleOrNull() ?: 0.0
                        priceError = priceText.trim().isEmpty() || priceNum <= 0

                        if (!nameError && !priceError) {
                            val productToEdit = editingProduct
                            if (productToEdit == null) {
                                viewModel.addProduct(
                                    name = name,
                                    price = priceNum,
                                    category = category,
                                    modifierMenu = modifierMenu
                                )
                            } else {
                                viewModel.editProduct(
                                    id = productToEdit.id,
                                    name = name,
                                    price = priceNum,
                                    category = category,
                                    modifierMenu = modifierMenu
                                )
                            }
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("submit_product_button")
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Add New Category Dialog (FAB Triggered in Category Tab)
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = {
                Text(
                    text = "Tambah Kategori Baru",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = {
                            newCategoryName = it
                            categoryNameError = it.isEmpty()
                        },
                        label = { Text("Nama Kategori Baru") },
                        isError = categoryNameError,
                        supportingText = { if (categoryNameError) Text("Nama kategori tidak boleh kosong") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_category_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.trim().isEmpty()) {
                            categoryNameError = true
                        } else {
                            viewModel.addCategory(newCategoryName)
                            newCategoryName = ""
                            showCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        newCategoryName = ""
                        categoryNameError = false
                        showCategoryDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Edit Category Dialog (Ubah Kategori - Row Triggered)
    if (showEditCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showEditCategoryDialog = false },
            title = {
                Text(
                    text = "Ubah Kategori",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = tempEditCategoryName,
                        onValueChange = {
                            tempEditCategoryName = it
                            editCategoryError = it.trim().isEmpty()
                        },
                        label = { Text("Nama Kategori") },
                        isError = editCategoryError,
                        supportingText = { if (editCategoryError) Text("Nama kategori tidak boleh kosong") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_category_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempEditCategoryName.trim().isEmpty()) {
                            editCategoryError = true
                        } else {
                            viewModel.editCategory(editingCategoryName, tempEditCategoryName)
                            showEditCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEditCategoryDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete Category Confirmation Dialog (Konfirmasi Hapus Kategori - Row Triggered)
    if (showDeleteCategoryConfirmation) {
        val categoriesList by viewModel.customCategories.collectAsState()
        // Determine fallback category
        val otherCategories = categoriesList.filter { !it.equals(deletingCategoryName, ignoreCase = true) }
        val fallback = if (otherCategories.isNotEmpty()) otherCategories.first() else "Lainnya"
        val productCountInDeleting = products.count { it.category.equals(deletingCategoryName, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showDeleteCategoryConfirmation = false },
            title = {
                Text(
                    text = "Konfirmasi Hapus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Red
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus kategori \"$deletingCategoryName\"?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (productCountInDeleting > 0) {
                        Text(
                            text = "Sebanyak $productCountInDeleting produk di kategori ini akan dipindahkan secara otomatis ke kategori pencadangan \"$fallback\".",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Kategori ini kosong (tidak ada produk).",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(deletingCategoryName)
                        showDeleteCategoryConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteCategoryConfirmation = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete Product Confirmation Dialog (Konfirmasi Hapus Produk)
    if (showDeleteProductConfirmation) {
        val productToDelete = deletingProduct
        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteProductConfirmation = false },
                title = {
                    Text(
                        text = "Konfirmasi Hapus Produk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Red
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Apakah Anda yakin ingin menghapus produk \"${productToDelete.name}\"?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Tindakan ini tidak dapat dibatalkan.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(productToDelete)
                            showDeleteProductConfirmation = false
                            deletingProduct = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteProductConfirmation = false
                            deletingProduct = null
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun SlidingTabs(
    selectedTab: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
            .padding(4.dp)
    ) {
        val indicatorOffset by animateFloatAsState(
            targetValue = selectedTab.toFloat(),
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
            label = "IndicatorOffset"
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            if (indicatorOffset > 0f) {
                Spacer(modifier = Modifier.weight(indicatorOffset))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            val trailWeight = tabs.size - 1 - indicatorOffset
            if (trailWeight > 0f) {
                Spacer(modifier = Modifier.weight(trailWeight))
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onTabSelected(index) }
                        .testTag("tab_button_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    val textColor by animateColorAsState(
                        targetValue = if (selectedTab == index) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "TextColor"
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}
