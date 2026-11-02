package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "products",
    indices = [Index(value = ["code"], unique = true), Index(value = ["barcode"])]
)
data class Product(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val code: String, // Short store code e.g. "101", "204" for fast typing
    val name: String, // e.g. "پیراهن کبریتی ققنوس"
    val category: String, // e.g. "پیراهن", "شلوار", "تی‌شرت", "کت و هودی"
    val buyPrice: Long, // In Rials
    val sellPrice: Long, // In Rials
    val stockQuantity: Int,
    val sizes: String = "M, L, XL", // Comma-separated sizes
    val colors: String = "مشکی, سرمه‌ای",
    val barcode: String = "",
    val minStockAlert: Int = 3,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderNumber: Long,
    val cashierName: String,
    val customerId: String? = null,
    val customerName: String? = null,
    val totalAmount: Long, // Sum before discount in Rials
    val discountAmount: Long, // In Rials
    val finalPayable: Long, // totalAmount - discountAmount
    val paymentType: String, // "POS" (کارتخوان), "CASH" (نقدی), "DEBT" (حساب دفتری / نسیه)
    val isSettled: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"]), Index(value = ["productId"])]
)
data class OrderItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val productId: String,
    val productCode: String,
    val productName: String,
    val unitPrice: Long,
    val quantity: Int,
    val selectedSize: String = "",
    val selectedColor: String = "",
    val subtotal: Long
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val fullName: String,
    val phoneNumber: String, // e.g. "09123456789"
    val currentDebt: Long = 0L, // In Rials
    val totalPurchases: Long = 0L, // In Rials
    val loyaltyTier: String = "عادی", // "عادی", "نقره‌ای", "طلایی"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(
    tableName = "debt_records",
    indices = [Index(value = ["customerId"])]
)
data class DebtRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val orderId: String? = null,
    val type: String, // "DEBIT" (بدهی جدید) or "CREDIT" (پرداخت و تسویه)
    val amount: Long, // In Rials
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "user_credentials")
data class UserCredential(
    @PrimaryKey
    val role: String, // "ADMIN" or "SELLER"
    val pinHash: String, // SHA-256 hashed 4-digit PIN
    val displayName: String,
    val updatedAt: Long = System.currentTimeMillis()
)
