package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_items")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: Int,
    val productId: Int,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val selectedModifier: String // The choice made by user on checkout, e.g. "Less Sugar"
)
