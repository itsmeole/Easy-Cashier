package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String, // e.g. "Makanan", "Minuman", "Snack", "Lainnya"
    val modifierMenu: String // e.g. "Normal, Less Sugar, No Sugar" or "Pedas, Sedang, Biasa"
)
