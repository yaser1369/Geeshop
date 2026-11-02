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
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

    suspend fun deleteOrderAndRestoreStock(orderId: String) {
        val order = orderDao.getOrderById(orderId) ?: return
        val items = orderDao.getOrderItems(orderId)

        // 1. Restore product stock in warehouse
        for (item in items) {
            productDao.restoreStock(item.productId, item.quantity)
        }

        // 2. If it was a debt/credit purchase, reduce the customer's balance
        if (order.paymentType == "DEBT" && !order.customerId.isNullOrBlank()) {
            customerDao.payDebt(order.customerId, order.finalPayable)
            customerDao.insertDebtRecord(
                DebtRecord(
                    customerId = order.customerId,
                    type = "CREDIT",
                    amount = order.finalPayable,
                    description = "ابطال و حذف فاکتور #${order.orderNumber} (بازگشت کالا)"
                )
            )
        }

        // 3. Delete order and items from database
        orderDao.deleteOrderItemsByOrderId(orderId)
        orderDao.deleteOrderById(orderId)
    }

    suspend fun getOrderWithItems(orderId: String): Pair<Order, List<OrderItem>>? {
        val order = orderDao.getOrderById(orderId) ?: return null
        val items = orderDao.getOrderItems(orderId)
        return Pair(order, items)
    }

    suspend fun updateOrderWithStockDelta(
        orderId: String,
        updatedItems: List<OrderItem>,
        discountAmount: Long,
        paymentType: String,
        customerId: String?,
        customerName: String?,
        notes: String
    ) {
        val oldOrder = orderDao.getOrderById(orderId) ?: return
        val oldItems = orderDao.getOrderItems(orderId)

        // 1. Calculate stock adjustments: diff = newQuantity - oldQuantity
        val oldQtyMap = oldItems.associate { it.productId to it.quantity }
        val newQtyMap = updatedItems.associate { it.productId to it.quantity }
        val allProductIds = (oldQtyMap.keys + newQtyMap.keys).toSet()

        for (pid in allProductIds) {
            val oldQty = oldQtyMap[pid] ?: 0
            val newQty = newQtyMap[pid] ?: 0
            val diff = newQty - oldQty
            if (diff > 0) {
                // More sold -> deduct from stock
                productDao.deductStock(pid, diff)
            } else if (diff < 0) {
                // Less sold -> return back to warehouse
                productDao.restoreStock(pid, -diff)
            }
        }

        // 2. Recalculate financial amounts
        val newTotalAmount = updatedItems.sumOf { it.unitPrice * it.quantity }
        val newFinalPayable = (newTotalAmount - discountAmount).coerceAtLeast(0L)

        // 3. Balance customer debt if old or new order used DEBT
        if (oldOrder.paymentType == "DEBT" && !oldOrder.customerId.isNullOrBlank()) {
            customerDao.payDebt(oldOrder.customerId, oldOrder.finalPayable)
            customerDao.insertDebtRecord(
                DebtRecord(
                    customerId = oldOrder.customerId,
                    type = "CREDIT",
                    amount = oldOrder.finalPayable,
                    description = "بستانکاری بابت اصلاح و ویرایش فاکتور #${oldOrder.orderNumber}"
                )
            )
        }
        if (paymentType == "DEBT" && !customerId.isNullOrBlank()) {
            customerDao.addDebt(customerId, newFinalPayable)
            customerDao.insertDebtRecord(
                DebtRecord(
                    customerId = customerId,
                    type = "DEBIT",
                    amount = newFinalPayable,
                    description = "بدهی جدید ناشی از اصلاح فاکتور #${oldOrder.orderNumber}"
                )
            )
        }

        // 4. Replace items in database
        orderDao.deleteOrderItemsByOrderId(orderId)
        val normalizedItems = updatedItems.map { it.copy(subtotal = it.unitPrice * it.quantity) }
        orderDao.insertOrderItems(normalizedItems)

        // 5. Update order record
        val updatedOrder = oldOrder.copy(
            customerId = customerId,
            customerName = customerName,
            totalAmount = newTotalAmount,
            discountAmount = discountAmount,
            finalPayable = newFinalPayable,
            paymentType = paymentType,
            isSettled = paymentType != "DEBT",
            notes = notes
        )
        orderDao.insertOrder(updatedOrder)
    }

    suspend fun generateComprehensiveBackupJson(): String {
        val productsList = productDao.getAllProducts().first()
        val customersList = customerDao.getAllCustomers().first()
        val ordersList = orderDao.getAllOrders().first()

        val root = JSONObject()

        // 1. Metadata Object
        val metadata = JSONObject()
        metadata.put("appName", "ققنوس شاپ (Ghoghnous Shop)")
        metadata.put("appId", "com.aistudio.ghoghnous.shop")
        metadata.put("version", "1.2.0")
        val now = System.currentTimeMillis()
        metadata.put("exportedAtTimestamp", now)
        val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(now))
        metadata.put("exportedAtFormatted", dateFormatted)

        val stats = JSONObject()
        stats.put("totalProductsCount", productsList.size)
        stats.put("totalCustomersCount", customersList.size)
        stats.put("totalOrdersCount", ordersList.size)
        stats.put("totalRevenueRials", ordersList.sumOf { it.finalPayable })
        stats.put("totalDiscountsRials", ordersList.sumOf { it.discountAmount })
        stats.put("totalCustomerDebtRials", customersList.sumOf { it.currentDebt })
        metadata.put("stats", stats)

        root.put("metadata", metadata)

        // 2. Data Object
        val data = JSONObject()

        // Products Array
        val productsArray = JSONArray()
        for (p in productsList) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("code", p.code)
            obj.put("name", p.name)
            obj.put("category", p.category)
            obj.put("buyPrice", p.buyPrice)
            obj.put("sellPrice", p.sellPrice)
            obj.put("stockQuantity", p.stockQuantity)
            obj.put("sizes", p.sizes)
            obj.put("colors", p.colors)
            obj.put("barcode", p.barcode)
            obj.put("minStockAlert", p.minStockAlert)
            obj.put("updatedAt", p.updatedAt)
            productsArray.put(obj)
        }
        data.put("products", productsArray)

        // Customers Array
        val customersArray = JSONArray()
        for (c in customersList) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("fullName", c.fullName)
            obj.put("phoneNumber", c.phoneNumber)
            obj.put("currentDebt", c.currentDebt)
            obj.put("totalPurchases", c.totalPurchases)
            obj.put("loyaltyTier", c.loyaltyTier)
            obj.put("notes", c.notes)
            obj.put("createdAt", c.createdAt)
            customersArray.put(obj)
        }
        data.put("customers", customersArray)

        // Orders Array with itemized order items
        val ordersArray = JSONArray()
        for (o in ordersList) {
            val obj = JSONObject()
            obj.put("id", o.id)
            obj.put("orderNumber", o.orderNumber)
            obj.put("cashierName", o.cashierName)
            obj.put("customerId", o.customerId ?: "")
            obj.put("customerName", o.customerName ?: "")
            obj.put("totalAmount", o.totalAmount)
            obj.put("discountAmount", o.discountAmount)
            obj.put("finalPayable", o.finalPayable)
            obj.put("paymentType", o.paymentType)
            obj.put("isSettled", o.isSettled)
            obj.put("notes", o.notes)
            obj.put("createdAt", o.createdAt)

            val items = orderDao.getOrderItems(o.id)
            val itemsArray = JSONArray()
            for (item in items) {
                val itemObj = JSONObject()
                itemObj.put("id", item.id)
                itemObj.put("productId", item.productId)
                itemObj.put("productCode", item.productCode)
                itemObj.put("productName", item.productName)
                itemObj.put("unitPrice", item.unitPrice)
                itemObj.put("quantity", item.quantity)
                itemObj.put("selectedSize", item.selectedSize)
                itemObj.put("selectedColor", item.selectedColor)
                itemObj.put("subtotal", item.subtotal)
                itemsArray.put(itemObj)
            }
            obj.put("items", itemsArray)
            ordersArray.put(obj)
        }
        data.put("orders", ordersArray)

        root.put("data", data)
        return root.toString(2)
    }

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
