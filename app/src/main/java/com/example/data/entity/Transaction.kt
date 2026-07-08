package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
