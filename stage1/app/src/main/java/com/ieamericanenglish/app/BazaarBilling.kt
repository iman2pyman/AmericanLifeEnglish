package com.ieamericanenglish.app

import androidx.activity.ComponentActivity
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest

/**
 * Cafe Bazaar subscription integration.
 * The RSA value is a PUBLIC verification key from the Bazaar developer panel.
 * It is not the APK signing private key.
 */
object BazaarBillingConfig {
    const val RSA_PUBLIC_KEY = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDnutWH2SlBsrw8zkVcx4je/PLxT79ltQMprjg65qwXuboCAyZuqcBgqNtdkm4184u0trRop6uHka9pJ0HfRILsrPPB+3PvJI4ycvnjrewyBEwWfCvPzXU1k3Oahkp+H4i+1hwEYiQAZ5GvAviJdjQFts4YEc5v4EFx2xnxmdYSVc+oSJHyliAi0msyG3bSjjfyDVfZtHl6LX1y9FkpeU5S0K/yb7NagYrQmmo24mMCAwEAAQ=="
    const val MONTHLY_SUBSCRIPTION_SKU = "premium_monthly"
    const val YEARLY_SUBSCRIPTION_SKU = "Premium_Yearly"
    const val TRIAL_SUBSCRIPTION_SKU = "trial_subscription"
}

class BazaarBillingManager(
    private val activity: ComponentActivity,
    private val onPremiumChanged: (Boolean) -> Unit,
    private val onStatus: (String) -> Unit
) {
    private val payment = Payment(
        context = activity,
        config = PaymentConfiguration(
            localSecurityCheck = SecurityCheck.Enable(
                rsaPublicKey = BazaarBillingConfig.RSA_PUBLIC_KEY
            )
        )
    )

    private var connection: Connection? = null
    private var connected = false
    private var pendingProductId: String? = null

    fun connect() {
        if (connection != null) return
        connection = payment.connect {
            connectionSucceed {
                connected = true
                onStatus("اتصال به پرداخت کافه‌بازار برقرار شد")
                refreshSubscriptions()
                pendingProductId?.let { productId ->
                    pendingProductId = null
                    launchSubscription(productId)
                }
            }
            connectionFailed { error ->
                connected = false
                connection = null
                pendingProductId = null
                onStatus("اتصال به کافه‌بازار ناموفق بود: ${error.message ?: "خطای نامشخص"}")
            }
            disconnected {
                connected = false
                connection = null
                onStatus("ارتباط با کافه‌بازار قطع شد؛ وضعیت اشتراک پس از اتصال دوباره بررسی می‌شود")
            }
        }
    }

    fun refreshSubscriptions() {
        if (!connected) return
        payment.getSubscribedProducts {
            querySucceed { purchases ->
                val active = purchases.any {
                    it.productId == BazaarBillingConfig.MONTHLY_SUBSCRIPTION_SKU ||
                    it.productId == BazaarBillingConfig.YEARLY_SUBSCRIPTION_SKU ||
                    it.productId == BazaarBillingConfig.TRIAL_SUBSCRIPTION_SKU
                }
                activity.runOnUiThread {
                    onPremiumChanged(active)
                    onStatus(if (active) "اشتراک ویژه فعال است" else "اشتراک فعالی پیدا نشد")
                }
            }
            queryFailed { error ->
                activity.runOnUiThread {
                    onStatus("بررسی اشتراک ناموفق بود: ${error.message ?: "خطای نامشخص"}")
                }
            }
        }
    }

    fun subscribe(productId: String) {
        if (productId != BazaarBillingConfig.MONTHLY_SUBSCRIPTION_SKU &&
            productId != BazaarBillingConfig.YEARLY_SUBSCRIPTION_SKU &&
            productId != BazaarBillingConfig.TRIAL_SUBSCRIPTION_SKU
        ) {
            onStatus("شناسه اشتراک معتبر نیست")
            return
        }
        if (connected) {
            launchSubscription(productId)
        } else {
            pendingProductId = productId
            onStatus("در حال اتصال به کافه‌بازار؛ صفحه خرید به‌صورت خودکار باز می‌شود…")
            connect()
        }
    }

    private fun launchSubscription(productId: String) {
        if (!connected) {
            pendingProductId = productId
            onStatus("اتصال کافه‌بازار برقرار نیست؛ دوباره تلاش می‌کنیم")
            connect()
            return
        }
        payment.subscribeProduct(
            registry = activity.activityResultRegistry,
            request = PurchaseRequest(
                productId = productId,
                payload = "AmericanLifeEnglish-premium"
            )
        ) {
            purchaseFlowBegan { onStatus("در حال انتقال به صفحه پرداخت کافه‌بازار…") }
            failedToBeginFlow { error ->
                onStatus("شروع پرداخت ناموفق بود: ${error.message ?: "خطای نامشخص"}")
            }
            purchaseSucceed {
                onStatus("خرید ثبت شد؛ در حال بررسی وضعیت اشتراک…")
                refreshSubscriptions()
            }
            purchaseCanceled { onStatus("پرداخت لغو شد") }
            purchaseFailed { error ->
                onStatus("پرداخت ناموفق بود: ${error.message ?: "خطای نامشخص"}")
            }
        }
    }

    fun disconnect() {
        connection?.disconnect()
        connection = null
        connected = false
    }
}
