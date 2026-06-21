package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.CashierRepository

class CashierViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CashierViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            val repository = CashierRepository(db.productDao(), db.transactionDao(), db.categoryDao(), db.userProfileDao())
            @Suppress("UNCHECKED_CAST")
            return CashierViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
