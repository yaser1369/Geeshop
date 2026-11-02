package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Customer
import com.example.data.model.DebtRecord
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.data.model.UserCredential
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE code = :code LIMIT 1")
    suspend fun getProductByCode(code: String): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE stockQuantity <= minStockAlert")
    fun getLowStockProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :qty, updatedAt = :now WHERE id = :id")
    suspend fun deductStock(id: String, qty: Int, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE createdAt >= :startOfDay ORDER BY createdAt DESC")
    fun getOrdersSince(startOfDay: Long): Flow<List<Order>>

    @Query("SELECT MAX(orderNumber) FROM orders")
    suspend fun getMaxOrderNumber(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: String): List<OrderItem>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItemsFlow(orderId: String): Flow<List<OrderItem>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): Order?
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY fullName ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE currentDebt > 0 ORDER BY currentDebt DESC")
    fun getDebtorCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)

    @Query("UPDATE customers SET currentDebt = currentDebt + :amount WHERE id = :id")
    suspend fun addDebt(id: String, amount: Long)

    @Query("UPDATE customers SET currentDebt = currentDebt - :amount WHERE id = :id")
    suspend fun payDebt(id: String, amount: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtRecord(record: DebtRecord)

    @Query("SELECT * FROM debt_records WHERE customerId = :customerId ORDER BY date DESC")
    fun getCustomerDebtRecords(customerId: String): Flow<List<DebtRecord>>
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_credentials WHERE role = :role LIMIT 1")
    suspend fun getUserCredential(role: String): UserCredential?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: UserCredential)

    @Query("SELECT COUNT(*) FROM user_credentials")
    suspend fun getCredentialsCount(): Int
}
