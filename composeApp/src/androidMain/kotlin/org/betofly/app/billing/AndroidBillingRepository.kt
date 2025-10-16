package org.betofly.app.billing

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.betofly.app.model.Theme
import org.betofly.app.repository.BillingRepository
import org.betofly.app.repository.PurchaseResult
import org.betofly.app.repository.ThemeRepository
import java.util.logging.Handler

class AndroidBillingRepository(
    private val context: Context,
    private val themeRepository: ThemeRepository
) : BillingRepository {

    private var purchaseContinuation: CancellableContinuation<PurchaseResult>? = null
    private var isBillingReady = false

    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchases != null) handlePurchases(purchases)
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    purchaseContinuation?.resume(PurchaseResult.Failure) {}
                    purchaseContinuation = null
                }
                else -> {
                    purchaseContinuation?.resume(
                        PurchaseResult.Error("Billing error: ${billingResult.debugMessage}")
                    ) {}
                    purchaseContinuation = null
                }
            }
        }
        .enablePendingPurchases()
        .build()

    private val themeProducts = listOf(
        "theme_royal_blue",
        "theme_graphite_gold"
    )

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isBillingReady = false
                Log.w("Billing", "Service Google Play Billing disabled ⚠️")
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    billingClient.startConnection(this)
                }, 2000)
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingReady = true
                    Log.d("Billing", "BillingClient ready ✅")

                    billingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    ) { br, purchases ->
                        if (br.responseCode == BillingClient.BillingResponseCode.OK) {
                            handlePurchases(purchases)
                        }
                    }

                } else {
                    isBillingReady = false
                    Log.e("Billing", "Connection error: ${result.debugMessage}")
                }
            }
        })
    }

    override suspend fun getThemes(): List<Theme> {
        return themeRepository.getAllThemes()
    }

    override suspend fun purchaseTheme(themeId: String): PurchaseResult =
        suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                val productDetails = queryProduct(themeId)
                if (productDetails == null) {
                    continuation.resume(PurchaseResult.Error("Product not found")) {}
                    return@launch
                }

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    ).build()

                val activity = context as? Activity
                if (activity == null) {
                    continuation.resume(PurchaseResult.Error("No activity context")) {}
                    return@launch
                }

                purchaseContinuation = continuation

                val billingResult = billingClient.launchBillingFlow(activity, flowParams)
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    continuation.resume(PurchaseResult.Failure) {}
                    purchaseContinuation = null
                }
            }
        }

    override suspend fun restorePurchases(): PurchaseResult {
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val purchases = result.purchasesList
        return if (purchases.isNotEmpty()) {
            handlePurchases(purchases)
            PurchaseResult.Success
        } else {
            PurchaseResult.Failure
        }
    }

    private suspend fun queryProduct(productId: String): ProductDetails? {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        val productDetailsResult = billingClient.queryProductDetails(queryProductDetailsParams)
        return productDetailsResult.productDetailsList?.firstOrNull()
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) {}

                val themeId = purchase.products.firstOrNull()
                if (themeId != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        themeRepository.markThemePurchased(themeId)
                        themeRepository.setCurrentTheme(themeId)
                    }
                }

                purchaseContinuation?.resume(PurchaseResult.Success) {}
                purchaseContinuation = null
            }
        }
    }
}