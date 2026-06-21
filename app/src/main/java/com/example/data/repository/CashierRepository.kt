package com.example.data.repository

import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.UserProfileDao
import com.example.data.entity.Product
import com.example.data.entity.Transaction
import com.example.data.entity.TransactionItem
import com.example.data.entity.Category
import com.example.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

class CashierRepository(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val userProfileDao: UserProfileDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun insertOrUpdateProfile(profile: UserProfile) =
        userProfileDao.insertOrUpdateProfile(profile)

    suspend fun insertCategory(category: Category) =
        categoryDao.insertCategory(category)

    suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(category)

    suspend fun updateCategoryName(oldName: String, newName: String) =
        categoryDao.updateCategoryName(oldName, newName)

    fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query)

    suspend fun insertProduct(product: Product) =
        productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) =
        productDao.deleteProduct(product)

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    suspend fun getTransactionItems(transactionId: Int): List<TransactionItem> =
        transactionDao.getTransactionItems(transactionId)

    suspend fun executeCheckout(
        totalAmount: Double,
        cashPaid: Double,
        changeAmount: Double,
        cartItems: List<CartItemModel>
    ): Transaction {
        val timestamp = System.currentTimeMillis()
        val trans = Transaction(
            timestamp = timestamp,
            totalAmount = totalAmount,
            cashPaid = cashPaid,
            changeAmount = changeAmount
        )
        // Store transaction to SQLite and retrieve generated primary key
        val transIdLong = transactionDao.insertTransaction(trans)
        val transactionId = transIdLong.toInt()

        // Prepare child items linked to parent receipt record
        val itemsToSave = cartItems.map { cart ->
            TransactionItem(
                transactionId = transactionId,
                productId = cart.product.id,
                productName = cart.product.name,
                productPrice = cart.product.price,
                quantity = cart.quantity,
                selectedModifier = cart.selectedModifier
            )
        }
        transactionDao.insertTransactionItems(itemsToSave)

        return trans.copy(id = transactionId)
    }
}

// Visual layout helper representing a single shopping item in memory
data class CartItemModel(
    val product: Product,
    val quantity: Int,
    val selectedModifier: String
)
