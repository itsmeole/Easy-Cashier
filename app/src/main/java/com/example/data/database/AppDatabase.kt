package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.UserProfileDao
import com.example.data.entity.Product
import com.example.data.entity.Transaction
import com.example.data.entity.TransactionItem
import com.example.data.entity.Category
import com.example.data.entity.UserProfile

@Database(
    entities = [Product::class, Transaction::class, TransactionItem::class, Category::class, UserProfile::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "easy_cashier_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
