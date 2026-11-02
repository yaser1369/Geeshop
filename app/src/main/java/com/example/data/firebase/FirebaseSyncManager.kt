package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.Product
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class FirebaseSyncManager(private val context: Context) {

    // Mandatory: Always resolve named database ID from firebase_applet_config.xml
    private val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    val auth: FirebaseAuth get() = Firebase.auth
    val currentUser: FirebaseUser? get() = auth.currentUser

    fun getGoogleWebClientId(): String {
        return context.getString(R.string.default_web_client_id)
    }

    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val clientId = getGoogleWebClientId()

            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: error("کاربر یافت نشد")
                Result.success(user)
            } else {
                Result.failure(Exception("نوع اعتبار دریافتی نامعتبر است"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("FirebaseSync", "ورود توسط کاربر لغو شد: ${e.message}")
            Result.failure(Exception("ورود لغو شد"))
        } catch (e: Exception) {
            Log.e("FirebaseSync", "خطا در ورود به حساب گوگل: ${e.message}", e)
            Result.failure(e)
        }
    }

    private var productsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var customersListener: ListenerRegistration? = null

    fun startRealtimeSync(db: AppDatabase, scope: CoroutineScope) {
        val user = currentUser ?: return
        val uid = user.uid
        val userDocRef = firestore.collection("users").document(uid)

        stopRealtimeSync()

        // 1. Products listener (receives items added/edited from web or 2nd cashier)
        productsListener = userDocRef.collection("products").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            scope.launch(Dispatchers.IO) {
                for (doc in snapshot.documents) {
                    try {
                        val product = Product(
                            id = doc.getString("id") ?: doc.id,
                            code = doc.getString("code") ?: "",
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "عمومی",
                            buyPrice = doc.getLong("buyPrice") ?: 0L,
                            sellPrice = doc.getLong("sellPrice") ?: 0L,
                            stockQuantity = (doc.getLong("stockQuantity") ?: 0L).toInt(),
                            sizes = doc.getString("sizes") ?: "M, L, XL",
                            colors = doc.getString("colors") ?: "",
                            barcode = doc.getString("barcode") ?: "",
                            minStockAlert = (doc.getLong("minStockAlert") ?: 3L).toInt(),
                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                            isSynced = true
                        )
                        db.productDao().insertProduct(product)
                    } catch (e: Exception) {
                        Log.e("FirebaseSync", "Error parsing remote product: ${e.message}")
                    }
                }
            }
        }

        // 2. Customers listener
        customersListener = userDocRef.collection("customers").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            scope.launch(Dispatchers.IO) {
                for (doc in snapshot.documents) {
                    try {
                        val customer = Customer(
                            id = doc.getString("id") ?: doc.id,
                            fullName = doc.getString("fullName") ?: "",
                            phoneNumber = doc.getString("phoneNumber") ?: "",
                            currentDebt = doc.getLong("currentDebt") ?: 0L,
                            totalPurchases = doc.getLong("totalPurchases") ?: 0L,
                            loyaltyTier = doc.getString("loyaltyTier") ?: "عادی",
                            notes = doc.getString("notes") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            isSynced = true
                        )
                        db.customerDao().insertCustomer(customer)
                    } catch (e: Exception) {
                        Log.e("FirebaseSync", "Error parsing remote customer: ${e.message}")
                    }
                }
            }
        }

        // 3. Orders listener (sales from other seller or web app)
        ordersListener = userDocRef.collection("orders").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            scope.launch(Dispatchers.IO) {
                for (doc in snapshot.documents) {
                    try {
                        val order = Order(
                            id = doc.getString("id") ?: doc.id,
                            orderNumber = doc.getLong("orderNumber") ?: 1001L,
                            cashierName = doc.getString("cashierName") ?: "صندوقدار",
                            customerId = doc.getString("customerId"),
                            customerName = doc.getString("customerName"),
                            totalAmount = doc.getLong("totalAmount") ?: 0L,
                            discountAmount = doc.getLong("discountAmount") ?: 0L,
                            finalPayable = doc.getLong("finalPayable") ?: 0L,
                            paymentType = doc.getString("paymentType") ?: "POS",
                            isSettled = doc.getBoolean("isSettled") ?: true,
                            notes = doc.getString("notes") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            isSynced = true
                        )
                        db.orderDao().insertOrder(order)
                    } catch (e: Exception) {
                        Log.e("FirebaseSync", "Error parsing remote order: ${e.message}")
                    }
                }
            }
        }
    }

    fun stopRealtimeSync() {
        productsListener?.remove()
        productsListener = null
        ordersListener?.remove()
        ordersListener = null
        customersListener?.remove()
        customersListener = null
    }

    fun signOut() {
        stopRealtimeSync()
        auth.signOut()
    }

    /**
     * Backs up all local products, orders and customers into Firestore cloud
     */
    suspend fun backupToCloud(
        products: List<Product>,
        orders: List<Order>,
        customers: List<Customer>
    ): Result<String> {
        val user = currentUser ?: return Result.failure(Exception("لطفاً ابتدا با حساب گوگل وارد شوید"))
        val uid = user.uid

        return try {
            val userDocRef = firestore.collection("users").document(uid)

            // 1. Sync Products
            val productsCollection = userDocRef.collection("products")
            for (p in products) {
                val map = hashMapOf(
                    "id" to p.id,
                    "code" to p.code,
                    "name" to p.name,
                    "category" to p.category,
                    "buyPrice" to p.buyPrice,
                    "sellPrice" to p.sellPrice,
                    "stockQuantity" to p.stockQuantity,
                    "sizes" to p.sizes,
                    "colors" to p.colors,
                    "barcode" to p.barcode,
                    "minStockAlert" to p.minStockAlert,
                    "updatedAt" to p.updatedAt
                )
                productsCollection.document(p.id).set(map, SetOptions.merge()).await()
            }

            // 2. Sync Customers
            val customersCollection = userDocRef.collection("customers")
            for (c in customers) {
                val map = hashMapOf(
                    "id" to c.id,
                    "fullName" to c.fullName,
                    "phoneNumber" to c.phoneNumber,
                    "currentDebt" to c.currentDebt,
                    "totalPurchases" to c.totalPurchases,
                    "loyaltyTier" to c.loyaltyTier,
                    "notes" to c.notes,
                    "createdAt" to c.createdAt
                )
                customersCollection.document(c.id).set(map, SetOptions.merge()).await()
            }

            // 3. Sync Orders
            val ordersCollection = userDocRef.collection("orders")
            for (o in orders) {
                val map = hashMapOf(
                    "id" to o.id,
                    "orderNumber" to o.orderNumber,
                    "cashierName" to o.cashierName,
                    "customerId" to (o.customerId ?: ""),
                    "customerName" to (o.customerName ?: ""),
                    "totalAmount" to o.totalAmount,
                    "discountAmount" to o.discountAmount,
                    "finalPayable" to o.finalPayable,
                    "paymentType" to o.paymentType,
                    "isSettled" to o.isSettled,
                    "createdAt" to o.createdAt
                )
                ordersCollection.document(o.id).set(map, SetOptions.merge()).await()
            }

            // 4. Save a Backup Snapshot record
            val backupId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val backupRecord = hashMapOf(
                "backupId" to backupId,
                "timestamp" to now,
                "productsCount" to products.size,
                "ordersCount" to orders.size,
                "customersCount" to customers.size
            )
            userDocRef.collection("backups").document(backupId).set(backupRecord).await()

            Result.success("پشتیبان‌گیری ابری ${products.size} کالا و ${orders.size} فاکتور با موفقیت در فایربیس ثبت شد.")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "خطا در آپلود پشتیبان به فایربیس: ${e.message}", e)
            Result.failure(e)
        }
    }
}
