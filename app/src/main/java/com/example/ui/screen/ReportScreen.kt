package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalPrintshop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Transaction
import com.example.data.entity.TransactionItem
import com.example.ui.viewmodel.CashierViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun ReportScreen(
    viewModel: CashierViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val dynamicTextColor = if (isDark) Color(0xFF9ECAFF) else Color.DarkGray

    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val filterType by viewModel.reportFilterType.collectAsState()
    val customStart by viewModel.customStartDate.collectAsState()
    val customEnd by viewModel.customEndDate.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Map which tracks which transaction was expanded and loaded items
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }
    val loadedItemsMap = remember { mutableStateMapOf<Int, List<TransactionItem>>() }

    // Receipt reprint simulation
    var reprintTransactionSelected by remember { mutableStateOf<Pair<Transaction, List<TransactionItem>>?>(null) }

    // Simple Custom Date dialog state
    var showCustomDateDialog by remember { mutableStateOf(false) }

    var startDay by remember { mutableStateOf("1") }
    var startMonth by remember { mutableStateOf("6") } // June
    var startYear by remember { mutableStateOf("2026") }
    
    var endDay by remember { mutableStateOf("9") }
    var endMonth by remember { mutableStateOf("6") } // June
    var endYear by remember { mutableStateOf("2026") }

    // Re-calculate statistics for the current selection of transactions
    val totalRevenue = filteredTransactions.sumOf { it.totalAmount }
    val totalTransactionsCount = filteredTransactions.size
    val averageBasketValue = if (totalTransactionsCount > 0) totalRevenue / totalTransactionsCount else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Laporan Transaksi",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // METRICS DASHBOARD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Total Revenue
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Total Omset", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.formatRupiah(totalRevenue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Card 2: Total Receipts
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Total Transaksi", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalTransactionsCount trx",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // DATE RANGE FILTER CAROUSEL
        val filtersList = listOf("Hari Ini", "7 Hari Terakhir", "Bulan Ini", "Semua Waktu", "Kustom")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Filter Tanggal:", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
        }

        LazyRow(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtersList.size) { index ->
                val typeName = filtersList[index]
                val isSelected = filterType == index || (index == 4 && filterType == 4)
                val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                val textCol = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                
                Surface(
                    modifier = Modifier
                        .clickable {
                            if (index == 4) {
                                showCustomDateDialog = true
                            } else {
                                viewModel.setFilterType(index)
                            }
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("report_filter_$typeName"),
                    color = bg,
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (index == 4 && filterType == 4) {
                                "${viewModel.formatDateOnly(customStart)} - ${viewModel.formatDateOnly(customEnd)}"
                            } else {
                                typeName
                            },
                            color = textCol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // TRANSACTIONS LIST
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "No Sales",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Belum ada omset penjualan", fontWeight = FontWeight.Bold)
                    Text("Transaksi checkout Anda akan terekam otomatis di sini.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { transaction ->
                    val isExpanded = expandedStates[transaction.id] == true
                    val linesLoaded = loadedItemsMap[transaction.id] ?: emptyList()

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
                            .testTag("transaction_row_${transaction.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Primary Info Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Transaksi #EC-${transaction.id}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = viewModel.formatDateTime(transaction.timestamp),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = viewModel.formatRupiah(transaction.totalAmount),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // EXPANDABLE ITEMS DETAIL PANEL
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                            .padding(bottom = 8.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Display list items nested
                                    if (linesLoaded.isEmpty()) {
                                        Text("Memuat rincian...", fontSize = 11.sp, color = Color.Gray)
                                    } else {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        ) {
                                            linesLoaded.forEach { item ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1.3f)) {
                                                        Text(
                                                            text = item.productName,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        if (item.selectedModifier.isNotEmpty()) {
                                                            Text(
                                                                text = "Opsi: ${item.selectedModifier}",
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                    
                                                    Text(
                                                        text = "${item.quantity} x",
                                                        fontSize = 12.sp,
                                                        color = dynamicTextColor,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.weight(0.4f)
                                                    )
                                                    
                                                    Text(
                                                        text = viewModel.formatRupiah(item.productPrice * item.quantity),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.End,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }

                                        // Total / Tunai summary breakdown
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp)
                                                .background(Color.Gray.copy(alpha = 0.05f))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Bayar Cash: ${viewModel.formatRupiah(transaction.cashPaid)}   |   Kembali: ${viewModel.formatRupiah(transaction.changeAmount)}",
                                                fontSize = 11.sp,
                                                color = dynamicTextColor
                                            )
                                            
                                            // Simulated Reprint button
                                            Row(
                                                modifier = Modifier
                                                    .clickable { reprintTransactionSelected = Pair(transaction, linesLoaded) }
                                                    .padding(horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalPrintshop,
                                                    contentDescription = "Cetak Ulang",
                                                    modifier = Modifier.size(13.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    "Lihat Struk",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
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
    }

    // Custom Calendar Date selection Dialog
    if (showCustomDateDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDateDialog = false },
            title = {
                Text(
                    text = "Filter Rentang Tanggal Kustom",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Start Date block
                    Column {
                        Text("Mulai Tanggal:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Day
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = startDay,
                                    onValueChange = { startDay = it.filter { c -> c.isDigit() } },
                                    label = { Text("Tgl") }
                                )
                            }
                            // Month (1-12)
                            Box(modifier = Modifier.weight(1.2f)) {
                                OutlinedTextField(
                                    value = startMonth,
                                    onValueChange = { startMonth = it.filter { c -> c.isDigit() } },
                                    label = { Text("Bln") }
                                )
                            }
                            // Year
                            Box(modifier = Modifier.weight(1.5f)) {
                                OutlinedTextField(
                                    value = startYear,
                                    onValueChange = { startYear = it.filter { c -> c.isDigit() } },
                                    label = { Text("Thn") }
                                )
                            }
                        }
                    }

                    // End Date block
                    Column {
                        Text("Sampai Tanggal:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Day
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = endDay,
                                    onValueChange = { endDay = it.filter { c -> c.isDigit() } },
                                    label = { Text("Tgl") }
                                )
                            }
                            // Month (1-12)
                            Box(modifier = Modifier.weight(1.2f)) {
                                OutlinedTextField(
                                    value = endMonth,
                                    onValueChange = { endMonth = it.filter { c -> c.isDigit() } },
                                    label = { Text("Bln") }
                                )
                            }
                            // Year
                            Box(modifier = Modifier.weight(1.5f)) {
                                OutlinedTextField(
                                    value = endYear,
                                    onValueChange = { endYear = it.filter { c -> c.isDigit() } },
                                    label = { Text("Thn") }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dStart = startDay.toIntOrNull() ?: 1
                        val mStart = (startMonth.toIntOrNull() ?: 6) - 1 // 0-indexed calendar
                        val yStart = startYear.toIntOrNull() ?: 2026

                        val dEnd = endDay.toIntOrNull() ?: 9
                        val mEnd = (endMonth.toIntOrNull() ?: 6) - 1
                        val yEnd = endYear.toIntOrNull() ?: 2026

                        val cal = Calendar.getInstance()
                        cal.set(yStart, mStart, dStart, 0, 0, 0)
                        val startMillis = cal.timeInMillis

                        cal.set(yEnd, mEnd, dEnd, 23, 59, 59)
                        val endMillis = cal.timeInMillis

                        viewModel.setCustomDateRange(startMillis, endMillis)
                        showCustomDateDialog = false
                    },
                    modifier = Modifier.testTag("submit_custom_date_filter")
                ) {
                    Text("Terapkan", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCustomDateDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Simulated Reprint Struk overlay
    reprintTransactionSelected?.let { (transaction, items) ->
        ReceiptDialog(
            transaction = transaction,
            items = items,
            viewModel = viewModel,
            onDismiss = { reprintTransactionSelected = null }
        )
    }
}
