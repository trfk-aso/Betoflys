package org.betofly.app.billing

import io.ktor.client.request.invoke
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.betofly.app.model.Theme
import org.betofly.app.repository.BillingRepository
import org.betofly.app.repository.PurchaseResult
import org.betofly.app.repository.ThemeRepository
import platform.Foundation.NSSet
import platform.StoreKit.*
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.collections.forEach

class IOSBillingRepository(
    private val themeRepository: ThemeRepository
) : BillingRepository {

    private val storeKitDelegate = StoreKitDelegate(themeRepository)

    init {
        SKPaymentQueue.defaultQueue().addTransactionObserver(storeKitDelegate)
        storeKitDelegate.fetchProducts()
    }

    override suspend fun getThemes(): List<Theme> {
        return themeRepository.getAllThemes()
    }

    override suspend fun purchaseTheme(themeId: String): PurchaseResult {
        return storeKitDelegate.purchaseTheme(themeId)
    }

    override suspend fun restorePurchases(): PurchaseResult {
        return storeKitDelegate.restorePurchases()
    }
}

// Отдельный класс для StoreKit делегатов
private class StoreKitDelegate(
    private val themeRepository: ThemeRepository
) : NSObject(), SKProductsRequestDelegateProtocol, SKPaymentTransactionObserverProtocol {

    private var purchaseContinuation: ((PurchaseResult) -> Unit)? = null
    private val themeProducts = setOf("theme_blue", "theme_gold")
    private val products: MutableMap<String, SKProduct> = mutableMapOf()

    fun fetchProducts() {
        val request = SKProductsRequest(productIdentifiers = themeProducts)
        request.delegate = this
        request.start()
    }

    suspend fun purchaseTheme(themeId: String): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            val product = products[themeId]
            if (product == null) {
                continuation.resume(PurchaseResult.Error("Product not found"))
                return@suspendCancellableCoroutine
            }

            purchaseContinuation = { result -> continuation.resume(result) }

            val payment = SKPayment.paymentWithProduct(product)
            SKPaymentQueue.defaultQueue().addPayment(payment)
        }

    suspend fun restorePurchases(): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            purchaseContinuation = { result -> continuation.resume(result) }
            SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
        }

    // --- SKProductsRequestDelegateProtocol ---
    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        val skProducts = didReceiveResponse.products ?: return
        skProducts.forEach { any ->
            val product = any as? SKProduct ?: return@forEach
            val id = product.productIdentifier ?: return@forEach
            products[id] = product
        }
    }

    // --- SKPaymentTransactionObserverProtocol ---
    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        updatedTransactions.forEach { any ->
            val transaction = any as? SKPaymentTransaction ?: return@forEach
            when (transaction.transactionState) {
                SKPaymentTransactionState.SKPaymentTransactionStatePurchased -> handlePurchased(transaction)
                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    purchaseContinuation?.invoke(PurchaseResult.Failure)
                    purchaseContinuation = null
                    SKPaymentQueue.defaultQueue().finishTransaction(transaction)
                }
                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> handlePurchased(transaction)
                else -> {}
            }
        }
    }

    private fun handlePurchased(transaction: SKPaymentTransaction) {
        val themeId = transaction.payment.productIdentifier ?: return
        GlobalScope.launch {
            themeRepository.markThemePurchased(themeId)
            themeRepository.setCurrentTheme(themeId)
        }
        purchaseContinuation?.invoke(PurchaseResult.Success)
        purchaseContinuation = null
        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
    }
}