package org.betofly.app.billing

object PromotionConnector {
    var onPromotionReceived: ((String) -> Unit)? = null
    var onPurchaseCompleted: (() -> Unit)? = null

    fun triggerPromotion(productId: String) {
        println(" Kotlin Connector received: $productId")
        onPromotionReceived?.invoke(productId)
    }
}