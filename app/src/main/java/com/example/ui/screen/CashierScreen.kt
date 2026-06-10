package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.Product
import com.example.data.entity.TransactionItem
import com.example.ui.viewmodel.CashierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    viewModel: CashierViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cashPaid by viewModel.cashPaid.collectAsState()
    val checkoutSuccess by viewModel.checkoutSuccessState.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("Semua") }
    var selectedProductForModifier by remember { mutableStateOf<Product?>(null) }
    var expandedCartMobile by remember { mutableStateOf(false) }

    val categoriesFromVm by viewModel.allCategories.collectAsState()
    val categories = listOf("Semua") + categoriesFromVm

    // Filter products locally to match selectedCategoryFilter
    val filteredProducts = if (selectedCategoryFilter == "Semua") {
        products
    } else {
        products.filter { it.category == selectedCategoryFilter }
    }

    val isWideScreen = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            
            // LEFT COLUMN: Catalog exploration
            Column(
                modifier = Modifier
                    .weight(if (isWideScreen) 1.3f else 1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                // Search Bar (Sleek pill style with surfaceVariant background)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Cari produk...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
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
                        .testTag("cashier_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp)
                )

                // Category Chips Selector
                LazyRow(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategoryFilter == category
                        val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                        val borderStroke = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Surface(
                            modifier = Modifier
                                .clickable { selectedCategoryFilter = category }
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("category_chip_$category"),
                            color = chipBg,
                            border = borderStroke,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = category,
                                color = chipText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Grid Catalog list
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = "Empty menu",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Menu tidak ditemukan", fontWeight = FontWeight.Bold)
                            Text("Tambahkan produk di menu Kelola Produk", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (product.modifierMenu.isNotEmpty()) {
                                            selectedProductForModifier = product
                                        } else {
                                            viewModel.addToCart(product, "")
                                        }
                                    }
                                    .testTag("cashier_product_${product.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = product.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = viewModel.formatRupiah(product.price),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Button(
                                        onClick = {
                                            if (product.modifierMenu.isNotEmpty()) {
                                                selectedProductForModifier = product
                                            } else {
                                                viewModel.addToCart(product, "")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp),
                                        contentPadding = ButtonDefaults.ContentPadding
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Pilih",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pilih", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT COLUMN: Shopping Cart on Tablet / Split layout
            if (isWideScreen) {
                Card(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    CartContent(viewModel = viewModel)
                }
            }
        }

        // FLOATING CHECKOUT BAR (Overlaps on narrow/mobile view if cart is not empty)
        if (!isWideScreen && cartItems.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedCartMobile = true }
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .testTag("floating_checkout_bar"),
                    color = MaterialTheme.colorScheme.tertiary, // #001D36
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary) // #0061A4
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${cartItems.sumOf { it.quantity }} ITEMS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = "TOTAL",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.60f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = viewModel.formatRupiah(cartTotal),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiary
                                )
                            }
                        }
                        
                        Button(
                            onClick = { expandedCartMobile = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("mobile_checkout_btn")
                        ) {
                            Text("Checkout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // FULL SCREEN MODAL for cart review on Mobile
        if (!isWideScreen && expandedCartMobile) {
            Dialog(
                onDismissRequest = { expandedCartMobile = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    CartContent(
                        viewModel = viewModel,
                        onCloseClick = { expandedCartMobile = false }
                    )
                }
            }
        }

        // FLOATING OVERLAY: Triggered for Product Modifiers selection
        selectedProductForModifier?.let { product ->
            val modifiersList = product.modifierMenu.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            var selectedMod by remember { mutableStateOf(modifiersList.firstOrNull() ?: "") }

            AlertDialog(
                onDismissRequest = { selectedProductForModifier = null },
                title = {
                    Column {
                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Pilih variasi modifier:", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modifiersList.forEach { modifierItem ->
                            val isChosen = selectedMod == modifierItem
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedMod = modifierItem }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isChosen) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                                        .background(if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = modifierItem,
                                    fontSize = 14.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addToCart(product, selectedMod)
                            selectedProductForModifier = null
                        }
                    ) {
                        Text("Tambah ke Keranjang", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { selectedProductForModifier = null }) {
                        Text("Batal")
                    }
                }
            )
        }

        // FLOATING DIALOG: Modern Thermal Receipt Print Simulation overlay
        checkoutSuccess?.let { (transaction, items) ->
            ReceiptDialog(
                transaction = transaction,
                items = items,
                viewModel = viewModel,
                onDismiss = { viewModel.dismissCheckoutSuccess() }
            )
        }
    }
}

@Composable
fun CartContent(
    viewModel: CashierViewModel,
    modifier: Modifier = Modifier,
    onCloseClick: (() -> Unit)? = null
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val cashPaid by viewModel.cashPaid.collectAsState()

    val cashPaidAmount = cashPaid.toDoubleOrNull() ?: 0.0
    val changeAmount = cashPaidAmount - cartTotal
    val isPaymentEnough = cashPaidAmount >= cartTotal && cartTotal > 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Keranjang",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Keranjang Belanja",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (cartItems.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearCart() }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Cart", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
                if (onCloseClick != null) {
                    IconButton(onClick = onCloseClick) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup Keranjang")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LIST OF CHOSEN ITEMS
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Empty Cart",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Keranjang Kosong",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Pilih menu di sebelah kiri untuk menambah pesanan.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.selectedModifier.isNotEmpty()) {
                                        Text(
                                            text = "Varian: ${item.selectedModifier}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                                
                                Text(
                                    text = viewModel.formatRupiah(item.product.price * item.quantity),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "@ " + viewModel.formatRupiah(item.product.price),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartItemQuantity(item, item.quantity - 1) },
                                        modifier = Modifier.size(28.dp).testTag("decrease_qty_${item.product.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = item.quantity.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.updateCartItemQuantity(item, item.quantity + 1) },
                                        modifier = Modifier.size(28.dp).testTag("increase_qty_${item.product.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TRANSACTION SUMMARY & BILLING CONTROL IN CART
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Belanja:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = viewModel.formatRupiah(cartTotal),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (cartItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Input cash amount
                    OutlinedTextField(
                        value = cashPaid,
                        onValueChange = { viewModel.updateCashPaid(it.filter { c -> c.isDigit() }) },
                        label = { Text("Bayar Cash (Rupiah)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("cash_paid_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Easy Quick cash buttons shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(cartTotal, 10000.0, 20000.0, 50000.0, 100000.0).forEach { amount ->
                            if (amount >= cartTotal && amount > 0) {
                                val label = if (amount == cartTotal) "U.Pas" else viewModel.formatRupiah(amount).replace("Rp ", "").replace(".000", "rb")
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                        .clickable { viewModel.updateCashPaid(amount.toInt().toString()) }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    if (cashPaid.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kembalian:", fontSize = 13.sp, fontWeight = FontWeight.Normal)
                            Text(
                                text = if (changeAmount < 0) "Kurang " + viewModel.formatRupiah(-changeAmount) else viewModel.formatRupiah(changeAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (changeAmount < 0) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Bayar Button
        Button(
            onClick = { viewModel.executeCheckout() },
            enabled = isPaymentEnough,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("checkout_confirm_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
            )
        ) {
            Icon(imageVector = Icons.Default.LocalPrintshop, contentDescription = "Print")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Proses Transaksi", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }

        // Lift elements up so they are not too close to the bottom device edge
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ReceiptDialog(
    transaction: com.example.data.entity.Transaction,
    items: List<TransactionItem>,
    viewModel: CashierViewModel,
    onDismiss: () -> Unit
) {
    val storeName by viewModel.storeName.collectAsState()
    val storeAddress by viewModel.storeAddress.collectAsState()
    val cashierName by viewModel.cashierName.collectAsState()

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header success check icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Transaksi Berhasil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Struk Pembayaran Otomatis telah diterbitkan.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                // Simulated POS thermal printer paper
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFCFDFD))
                        .border(BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    // STORE HEADER
                    Text(
                        text = storeName.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = storeAddress,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No: EC-TRX-${transaction.id}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Tgl: ${viewModel.formatDateTime(transaction.timestamp)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Kasir: $cashierName",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "--------------------------------",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // LINE ITEMS
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items.forEach { line ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = line.productName,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = viewModel.formatRupiah(line.productPrice * line.quantity),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                }
                                if (line.selectedModifier.isNotEmpty()) {
                                    Text(
                                        text = " (Opt: ${line.selectedModifier})",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = "  ${line.quantity} x ${viewModel.formatRupiah(line.productPrice)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    Text(
                        text = "--------------------------------",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // TOTAL CALCULATIONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                        Text(
                            text = viewModel.formatRupiah(transaction.totalAmount),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TUNAI", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.DarkGray)
                        Text(
                            text = viewModel.formatRupiah(transaction.cashPaid),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("KEMBALIAN", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.DarkGray)
                        Text(
                            text = viewModel.formatRupiah(transaction.changeAmount),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "TERIMA KASIH",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "--- LUNAS ---",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_receipt_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Selesai & Tutup", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}
