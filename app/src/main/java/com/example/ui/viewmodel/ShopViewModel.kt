package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.data.repository.CartItem
import com.example.data.repository.DailySalesReport
import com.example.data.repository.ShopRepository
import com.example.util.BarcodeUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    POS, // صندوق فروشگاهی
    INVENTORY, // مدیریت کالاها و انبار
    CUSTOMERS, // مشتریان و حساب دفتری
    REPORTS, // گزارش‌های روزانه و ماهیانه
    SYNC_SETTINGS // تغییر رمز و معماری دیتابیس
}

enum class DiscountType {
    FIXED_RIAL, // تخفیف مبلغی به ریال
    PERCENT // تخفیف درصدی
}

data class CompletedReceiptState(
    val order: Order,
    val items: List<OrderItem>
)

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopRepository = ShopRepository(AppDatabase.getDatabase(application))

    // Active User Role: "ADMIN" or "SELLER"
    private val _currentRole = MutableStateFlow("ADMIN")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(ScreenTab.POS)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Products & Filtering
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("همه")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, cat ->
        products.filter { p ->
            val matchCat = cat == "همه" || p.category == cat
            val matchQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.code.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true)
            matchCat && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart in POS
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _discountType = MutableStateFlow(DiscountType.FIXED_RIAL)
    val discountType: StateFlow<DiscountType> = _discountType.asStateFlow()

    private val _discountValue = MutableStateFlow(0L)
    val discountValue: StateFlow<Long> = _discountValue.asStateFlow()

    private val _selectedCustomerForOrder = MutableStateFlow<Customer?>(null)
    val selectedCustomerForOrder: StateFlow<Customer?> = _selectedCustomerForOrder.asStateFlow()

    // Active Customers
    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debtorCustomers: StateFlow<List<Customer>> = repository.debtorCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product Modals
    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    private val _isAddProductOpen = MutableStateFlow(false)
    val isAddProductOpen: StateFlow<Boolean> = _isAddProductOpen.asStateFlow()

    private val _barcodeModalProduct = MutableStateFlow<Product?>(null)
    val barcodeModalProduct: StateFlow<Product?> = _barcodeModalProduct.asStateFlow()

    // Last completed order for receipt dialog
    private val _lastCompletedOrder = MutableStateFlow<CompletedReceiptState?>(null)
    val lastCompletedOrder: StateFlow<CompletedReceiptState?> = _lastCompletedOrder.asStateFlow()

    // Reports state
    private val _todayReport = MutableStateFlow<DailySalesReport?>(null)
    val todayReport: StateFlow<DailySalesReport?> = _todayReport.asStateFlow()

    // User alerts / feedback message
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            refreshReport()
        }
    }

    fun setTab(tab: ScreenTab) {
        // If seller tries to access admin-only tabs, block or notify
        if (_currentRole.value == "SELLER" && (tab == ScreenTab.INVENTORY || tab == ScreenTab.REPORTS)) {
            viewModelScope.launch {
                _toastEvent.emit("دسترسی محدود: این بخش مخصوص مدیر است. لطفاً نقش مدیر را فعال کنید.")
            }
            return
        }
        _currentTab.value = tab
    }

    fun switchRole(newRole: String) {
        _currentRole.value = newRole
        if (newRole == "SELLER" && (_currentTab.value == ScreenTab.INVENTORY || _currentTab.value == ScreenTab.REPORTS)) {
            _currentTab.value = ScreenTab.POS
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(cat: String) {
        _selectedCategory.value = cat
    }

    // POS Cart Actions
    fun addToCart(product: Product) {
        if (product.stockQuantity <= 0) {
            viewModelScope.launch {
                _toastEvent.emit("موجودی کالا در انبار به اتمام رسیده است!")
            }
            return
        }

        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            val item = current[existingIndex]
            if (item.quantity + 1 > product.stockQuantity) {
                viewModelScope.launch {
                    _toastEvent.emit("موجودی کافی در انبار نیست (حداکثر ${product.stockQuantity})")
                }
                return
            }
            current[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            val defaultSize = product.sizes.split(",").firstOrNull()?.trim() ?: ""
            val defaultColor = product.colors.split(",").firstOrNull()?.trim() ?: ""
            current.add(CartItem(product = product, quantity = 1, selectedSize = defaultSize, selectedColor = defaultColor))
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else if (newQty > item.product.stockQuantity) {
                viewModelScope.launch {
                    _toastEvent.emit("موجودی کافی در انبار نیست (حداکثر ${item.product.stockQuantity})")
                }
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun updateCartItemAttributes(productId: String, size: String, color: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            current[index] = current[index].copy(selectedSize = size, selectedColor = color)
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountValue.value = 0L
        _selectedCustomerForOrder.value = null
    }

    fun setDiscount(type: DiscountType, value: Long) {
        _discountType.value = type
        _discountValue.value = value
    }

    fun setSelectedCustomerForOrder(customer: Customer?) {
        _selectedCustomerForOrder.value = customer
    }

    fun calculateTotalAmount(): Long = _cartItems.value.sumOf { it.subtotal }

    fun calculateDiscountAmount(): Long {
        val total = calculateTotalAmount()
        return when (_discountType.value) {
            DiscountType.FIXED_RIAL -> _discountValue.value.coerceAtMost(total)
            DiscountType.PERCENT -> {
                val pct = _discountValue.value.coerceIn(0L, 100L)
                (total * pct) / 100L
            }
        }
    }

    fun calculateFinalPayable(): Long {
        val total = calculateTotalAmount()
        val discount = calculateDiscountAmount()
        return (total - discount).coerceAtLeast(0L)
    }

    fun findAndAddByCodeOrBarcode(codeOrBarcode: String) {
        viewModelScope.launch {
            val trimmed = codeOrBarcode.trim()
            val product = repository.findProductByCode(trimmed)
                ?: repository.findProductByBarcode(trimmed)

            if (product != null) {
                addToCart(product)
                _toastEvent.emit("کالای «${product.name}» به فاکتور اضافه شد")
            } else {
                _toastEvent.emit("کالایی با کد یا بارکد $trimmed یافت نشد!")
            }
        }
    }

    fun completeOrder(paymentType: String, notes: String = "") {
        if (_cartItems.value.isEmpty()) {
            viewModelScope.launch { _toastEvent.emit("سبد خرید خالی است!") }
            return
        }

        if (paymentType == "DEBT" && _selectedCustomerForOrder.value == null) {
            viewModelScope.launch {
                _toastEvent.emit("جهت ثبت فاکتور قرضی/دفتری، لطفاً ابتدا مشتری را انتخاب نمایید.")
            }
            return
        }

        viewModelScope.launch {
            try {
                val discount = calculateDiscountAmount()
                val cashier = if (_currentRole.value == "ADMIN") "مدیریت فروشگاه" else "صندوقدار"
                val (order, items) = repository.placeOrder(
                    cartItems = _cartItems.value,
                    cashierName = cashier,
                    discountAmount = discount,
                    paymentType = paymentType,
                    customer = _selectedCustomerForOrder.value,
                    notes = notes
                )

                _lastCompletedOrder.value = CompletedReceiptState(order, items)
                clearCart()
                refreshReport()
                _toastEvent.emit("فاکتور #${order.orderNumber} با موفقیت ثبت شد")
            } catch (e: Exception) {
                _toastEvent.emit("خطا در ثبت فاکتور: ${e.message}")
            }
        }
    }

    fun closeReceiptModal() {
        _lastCompletedOrder.value = null
    }

    // Product Management (Admin)
    fun openAddProduct() {
        _editingProduct.value = null
        _isAddProductOpen.value = true
    }

    fun openEditProduct(product: Product) {
        _editingProduct.value = product
        _isAddProductOpen.value = true
    }

    fun closeProductModal() {
        _editingProduct.value = null
        _isAddProductOpen.value = false
    }

    fun saveProduct(
        code: String,
        name: String,
        category: String,
        buyPrice: Long,
        sellPrice: Long,
        stock: Int,
        sizes: String,
        colors: String,
        barcode: String,
        minAlert: Int
    ) {
        viewModelScope.launch {
            val current = _editingProduct.value
            val product = Product(
                id = current?.id ?: java.util.UUID.randomUUID().toString(),
                code = code.trim(),
                name = name.trim(),
                category = category.trim(),
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                stockQuantity = stock,
                sizes = sizes.trim(),
                colors = colors.trim(),
                barcode = barcode.trim().ifBlank { BarcodeUtils.generateProductBarcode(code) },
                minStockAlert = minAlert
            )
            repository.saveProduct(product)
            closeProductModal()
            _toastEvent.emit("کالای «$name» با موفقیت ذخیره شد")
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product.id)
            _toastEvent.emit("کالای «${product.name}» حذف شد")
        }
    }

    fun showBarcodeModal(product: Product) {
        _barcodeModalProduct.value = product
    }

    fun closeBarcodeModal() {
        _barcodeModalProduct.value = null
    }

    // Customer Management
    fun saveCustomer(fullName: String, phone: String, notes: String) {
        viewModelScope.launch {
            val customer = Customer(
                fullName = fullName.trim(),
                phoneNumber = phone.trim(),
                notes = notes.trim()
            )
            repository.saveCustomer(customer)
            _toastEvent.emit("مشتری جدید «$fullName» اضافه شد")
        }
    }

    fun recordDebtPayment(customerId: String, amount: Long, description: String) {
        viewModelScope.launch {
            repository.recordDebtPayment(customerId, amount, description)
            _toastEvent.emit("پرداخت با موفقیت در حساب دفتری ثبت شد")
        }
    }

    // Security & PIN update
    fun updateRolePin(role: String, newPin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
                _toastEvent.emit("پین‌کد باید ۴ رقم باشد!")
                return@launch
            }
            repository.updatePin(role, newPin)
            _toastEvent.emit("رمز عبور ${if (role == "ADMIN") "مدیر" else "فروشنده"} با موفقیت تغییر کرد")
            onSuccess()
        }
    }

    private fun refreshReport() {
        viewModelScope.launch {
            _todayReport.value = repository.getTodayReport()
        }
    }
}
