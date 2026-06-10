package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    var searchQuery by remember { mutableStateOf("") }

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
    
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    
    // Floating Action Menu States
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var categoryNameError by remember { mutableStateOf(false) }
    
    // Form States
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var modifierMenu by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    fun openAddDialog() {
        editingProduct = null
        name = ""
        priceText = ""
        category = "Makanan"
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
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Expanded floating action options
                AnimatedVisibility(
                    visible = isMenuExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Option 1: Tambah Kategori
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF6A6A6A))
                                .clickable {
                                    isMenuExpanded = false
                                    showCategoryDialog = true
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Tambah Kategori",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tambah Kategori",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Option 2: Tambah Produk
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF6A6A6A))
                                .clickable {
                                    isMenuExpanded = false
                                    openAddDialog()
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = "Tambah Produk Baru",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tambah Produk Baru",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = { isMenuExpanded = !isMenuExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("add_product_fab")
                ) {
                    Icon(
                        imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Expand menu tambah",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
                                        onClick = { viewModel.deleteProduct(product) },
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
    }
    }

    // Add / Edit Product Alert Dialog
    if (showDialog) {
        val categoriesList by viewModel.customCategories.collectAsState()
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
                                .menuAnchor()
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
                        placeholder = { Text("cth: Manis, Kurang Sugar, Tawar") },
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

    // Add New Category Dialog
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
}
