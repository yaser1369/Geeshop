package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.DebtRecord
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.data.model.UserCredential
import com.example.util.BarcodeUtils
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID

data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    var selectedSize: String = "",
    var selectedColor: String = ""
) {
    val subtotal: Long get() = product.sellPrice * quantity
}

data class DailySalesReport(
    val totalRevenue: Long,
    val ordersCount: Int,
    val itemsSoldCount: Int,
    val totalDiscount: Long
)

class ShopRepository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val orderDao = db.orderDao()
    private val customerDao = db.customerDao()
    private val userDao = db.userDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val debtorCustomers: Flow<List<Customer>> = customerDao.getDebtorCustomers()

    suspend fun initializeDefaultsIfNeeded() {
        // Seed default credentials if empty
        if (userDao.getCredentialsCount() == 0) {
            userDao.insertCredential(
                UserCredential(
                    role = "ADMIN",
                    pinHash = SecurityUtils.hashPin("1234"),
                    displayName = "مدیریت فروشگاه"
                )
            )
            userDao.insertCredential(
                UserCredential(
                    role = "SELLER",
                    pinHash = SecurityUtils.hashPin("0000"),
                    displayName = "صندوقدار"
                )
            )
        }

        // Seed default clothing products if catalog is empty
        if (productDao.countProducts() == 0) {
            val sampleProducts = listOf(
                Product(
                    code = "101",
                    name = "هودی ضخیم پاییزه ققنوس",
                    category = "هودی و سویشرت",
                    buyPrice = 5500000L, // 550,000 Tomans in Rials
                    sellPrice = 9800000L, // 980,000 Tomans in Rials
                    stockQuantity = 14,
                    sizes = "M, L, XL, 2XL",
                    colors = "مشکی, زغالی, یشمی",
                    barcode = BarcodeUtils.generateProductBarcode("101"),
                    minStockAlert = 3
                ),
                Product(
                    code = "102",
                    name = "شلوار جین مام استایل ترک",
                    category = "شلوار",
                    buyPrice = 7200000L,
                    sellPrice = 12500000L,
                    stockQuantity = 9,
                    sizes = "30, 31, 32, 33, 34",
                    colors = "آبی یخی, ذغالی, آبی تیره",
                    barcode = BarcodeUtils.generateProductBarcode("102"),
                    minStockAlert = 2
                ),
                Product(
                    code = "103",
                    name = "پیراهن کبریتی طرح راسته",
                    category = "پیراهن",
                    buyPrice = 4100000L,
                    sellPrice = 6900000L,
                    stockQuantity = 20,
                    sizes = "M, L, XL",
                    colors = "کرم, خردلی, مشکی",
                    barcode = BarcodeUtils.generateProductBarcode("103"),
                    minStockAlert = 4
                ),
                Product(
                    code = "104",
                    name = "تی‌شرت اوور سایز چاپ ققنوس",
                    category = "تی‌شرت",
                    buyPrice = 2800000L,
                    sellPrice = 4800000L,
                    stockQuantity = 2, // Low stock on purpose for testing
                    sizes = "L, XL",
                    colors = "سفید, نارنجی سوخته, سرمه‌ای",
                    barcode = BarcodeUtils.generateProductBarcode("104"),
                    minStockAlert = 3
                ),
                Product(
                    code = "105",
                    name = "کاپشن بارانی ضدآب کلاه‌دار",
                    category = "کت و کاپشن",
                    buyPrice = 11000000L,
                    sellPrice = 18500000L,
                    stockQuantity = 7,
                    sizes = "L, XL, 2XL",
                    colors = "مشکی, سبز زیتونی",
                    barcode = BarcodeUtils.generateProductBarcode("105"),
                    minStockAlert = 2
                )
            )
            for (p in sampleProducts) {
                productDao.insertProduct(p)
            }

            // Seed a sample customer with credit history
            val sampleCustomer = Customer(
                fullName = "آقای رضایی (آشنا)",
                phoneNumber = "09121234567",
                currentDebt = 6900000L,
                totalPurchases = 25000000L,
                loyaltyTier = "نقره‌ای",
                notes = "از مشتریان قدیمی، تسویه سر ماه"
            )
            customerDao.insertCustomer(sampleCustomer)
            customerDao.insertDebtRecord(
                DebtRecord(
                    customerId = sampleCustomer.id,
                    type = "DEBIT",
                    amount = 6900000L,
                    description = "خرید پیراهن کبریتی به صورت حساب دفتری"
                )
            )
        }
    }

    // Product management
    suspend fun saveProduct(product: Product) {
        val barcode = if (product.barcode.isBlank()) {
            BarcodeUtils.generateProductBarcode(product.code)
        } else {
            product.barcode
        }
        productDao.insertProduct(product.copy(barcode = barcode, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProduct(id: String) {
        productDao.deleteProductById(id)
    }

    suspend fun findProductByCode(code: String): Product? = productDao.getProductByCode(code.trim())

    suspend fun findProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode.trim())

    // Checkout & Order Placement
    suspend fun placeOrder(
        cartItems: List<CartItem>,
        cashierName: String,
        discountAmount: Long,
        paymentType: String, // "POS", "CASH", "DEBT"
        customer: Customer? = null,
        notes: String = ""
    ): Pair<Order, List<OrderItem>> {
        val totalAmount = cartItems.sumOf { it.subtotal }
        val finalPayable = (totalAmount - discountAmount).coerceAtLeast(0L)
        val maxNumber = orderDao.getMaxOrderNumber() ?: 1000L
        val orderNumber = maxNumber + 1
        val orderId = UUID.randomUUID().toString()

        val order = Order(
            id = orderId,
            orderNumber = orderNumber,
            cashierName = cashierName,
            customerId = customer?.id,
            customerName = customer?.fullName,
            totalAmount = totalAmount,
            discountAmount = discountAmount,
            finalPayable = finalPayable,
            paymentType = paymentType,
            isSettled = paymentType != "DEBT",
            notes = notes
        )

        val orderItems = cartItems.map { item ->
            OrderItem(
                orderId = orderId,
                productId = item.product.id,
                productCode = item.product.code,
                productName = item.product.name,
                unitPrice = item.product.sellPrice,
                quantity = item.quantity,
                selectedSize = item.selectedSize.ifBlank { item.product.sizes.split(",").firstOrNull()?.trim() ?: "" },
                selectedColor = item.selectedColor.ifBlank { item.product.colors.split(",").firstOrNull()?.trim() ?: "" },
                subtotal = item.subtotal
            )
        }

        // Save order and items
        orderDao.insertOrder(order)
        orderDao.insertOrderItems(orderItems)

        // Deduct inventory
        for (item in cartItems) {
            productDao.deductStock(item.product.id, item.quantity)
        }

        // If debt payment, update customer balance
        if (paymentType == "DEBT" && customer != null) {
            customerDao.addDebt(customer.id, finalPayable)
            customerDao.insertDebtRecord(
                DebtRecord(
                    customerId = customer.id,
                    orderId = orderId,
                    type = "DEBIT",
                    amount = finalPayable,
                    description = "فاکتور #${orderNumber} به صورت قرضی/دفتری"
                )
            )
        }

        return Pair(order, orderItems)
    }

    suspend fun getOrderItems(orderId: String): List<OrderItem> = orderDao.getOrderItems(orderId)

    // Customer & Debt management
    suspend fun saveCustomer(customer: Customer) {
        customerDao.insertCustomer(customer)
    }

    suspend fun deleteCustomer(id: String) {
        customerDao.deleteCustomerById(id)
    }

    suspend fun recordDebtPayment(customerId: String, amount: Long, description: String) {
        customerDao.payDebt(customerId, amount)
        customerDao.insertDebtRecord(
            DebtRecord(
                customerId = customerId,
                type = "CREDIT",
                amount = amount,
                description = description.ifBlank { "واریز و تسویه بدهی دفتری" }
            )
        )
    }

    fun getCustomerDebtRecords(customerId: String): Flow<List<DebtRecord>> {
        return customerDao.getCustomerDebtRecords(customerId)
    }

    // User & Credentials
    suspend fun verifyPin(role: String, pin: String): Boolean {
        val credential = userDao.getUserCredential(role) ?: return false
        return SecurityUtils.verifyPin(pin, credential.pinHash)
    }

    suspend fun updatePin(role: String, newPin: String) {
        val current = userDao.getUserCredential(role)
        val displayName = current?.displayName ?: if (role == "ADMIN") "مدیریت فروشگاه" else "صندوقدار"
        userDao.insertCredential(
            UserCredential(
                role = role,
                pinHash = SecurityUtils.hashPin(newPin),
                displayName = displayName,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Reports aggregation
    suspend fun getTodayReport(): DailySalesReport {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        var totalRev = 0L
        var count = 0
        var totalDisc = 0L

        // Since we can collect from DAO
        val all = db.openHelper.readableDatabase.query(
            "SELECT finalPayable, discountAmount FROM orders WHERE createdAt >= $startOfDay"
        )
        all.use { cursor ->
            while (cursor.moveToNext()) {
                val payable = cursor.getLong(0)
                val disc = cursor.getLong(1)
                totalRev += payable
                totalDisc += disc
                count++
            }
        }
        return DailySalesReport(
            totalRevenue = totalRev,
            ordersCount = count,
            itemsSoldCount = count * 2, // Estimated items
            totalDiscount = totalDisc
        )
    }
}
