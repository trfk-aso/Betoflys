package org.betofly.app.repository

import org.betofly.app.model.Theme

interface BillingRepository {
    suspend fun getThemes(): List<Theme>
    suspend fun purchaseTheme(themeId: String): PurchaseResult
    suspend fun restorePurchases(): PurchaseResult
}

sealed class PurchaseResult {
    object Success : PurchaseResult()
    object Failure : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}