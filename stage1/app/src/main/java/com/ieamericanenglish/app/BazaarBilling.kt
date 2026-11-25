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
                val activeIds = purchases.map { it.productId }.distinct()
                val hasTrial = activeIds.contains(BazaarBillingConfig.TRIAL_SUBSCRIPTION_SKU)
                val hasMonthly = activeIds.contains(BazaarBillingConfig.MONTHLY_SUBSCRIPTION_SKU)
                val hasYearly = activeIds.contains(BazaarBillingConfig.YEARLY_SUBSCRIPTION_SKU)
                val active = hasTrial || hasMonthly || hasYearly
                val knownIds = activeIds.filter {
                    it == BazaarBillingConfig.TRIAL_SUBSCRIPTION_SKU ||
                    it == BazaarBillingConfig.MONTHLY_SUBSCRIPTION_SKU ||
                    it == BazaarBillingConfig.YEARLY_SUBSCRIPTION_SKU
                }
                val unknownIds = activeIds - knownIds.toSet()

                android.util.Log.i(
                    "AmericanLifeEnglishBilling",
                    "SUBSCRIPTIONS_QUERY count=${purchases.size} activeIds=${activeIds.joinToString(",")} trial=$hasTrial monthly=$hasMonthly yearly=$hasYearly unknownIds=${unknownIds.joinToString(",")}"
                )

                activity.runOnUiThread {
                    onPremiumChanged(active)
                    val details = buildList {
                        if (hasTrial) add("آزمایشی")
                        if (hasMonthly) add("ماهانه")
                        if (hasYearly) add("سالانه")
                    }
                    val status = when {
                        details.isNotEmpty() -> "اشتراک‌های شناسایی‌شده: ${details.joinToString("، ")} | شناسه‌ها: ${knownIds.joinToString(", ")}"
                        unknownIds.isNotEmpty() -> "اشتراک ناشناخته از بازار دریافت شد: ${unknownIds.joinToString(", ")}"
                        else -> "اشتراک فعالی پیدا نشد"
                    }
                    onStatus(status)
                }
            }
            queryFailed { error ->
                android.util.Log.e("AmericanLifeEnglishBilling", "SUBSCRIPTIONS_QUERY_FAILED", error)
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

        // Query Bazaar for the live subscription price before opening payment.
        payment.getSubscriptionSkuDetails(skuIds = listOf(productId)) {
            getSkuDetailsSucceed { details ->
                val sku = details.firstOrNull { it.sku == productId }
                val price = sku?.price
                android.util.Log.i("AmericanLifeEnglishBilling", "SKU_PRICE sku=$productId returned=${details.size} price=${price ?: "<missing>"}")

                val isTrial = productId == BazaarBillingConfig.TRIAL_SUBSCRIPTION_SKU
                val digits = price.orEmpty().mapNotNull { ch ->
                    when (ch) {
                        in '۰'..'۹' -> ch.code - '۰'.code
                        in '٠'..'٩' -> ch.code - '٠'.code
                        in '0'..'9' -> ch.code - '0'.code
                        else -> null
                    }
                }.joinToString("")
                val amount = digits.toLongOrNull()

                if (!isTrial && (sku == null || amount == null || amount <= 0L)) {
                    onStatus("خرید متوقف شد؛ قیمت معتبر از بازار دریافت نشد. شناسه: $productId، قیمت دریافتی: ${price ?: "دریافت نشد"}")
                    return@getSkuDetailsSucceed
                }

                onStatus("قیمت دریافتی بازار برای $productId: ${price ?: "نامشخص"}؛ در حال باز کردن پرداخت…")
                openSubscriptionPayment(productId)
            }
            getSkuDetailsFailed { error ->
                android.util.Log.e("AmericanLifeEnglishBilling", "SKU_PRICE_ERROR sku=$productId message=${error.message}", error)
                if (productId == BazaarBillingConfig.TRIAL_SUBSCRIPTION_SKU) {
                    onStatus("استعلام قیمت دوره آزمایشی ناموفق بود؛ تلاش برای باز کردن خرید آزمایشی")
                    openSubscriptionPayment(productId)
                } else {
                    onStatus("خرید متوقف شد؛ استعلام قیمت از بازار ناموفق بود: ${error.message ?: "خطای نامشخص"}")
                }
            }
        }
    }

    private fun openSubscriptionPayment(productId: String) {
        payment.subscribeProduct(
            registry = activity.activityResultRegistry,
            request = PurchaseRequest(productId = productId, payload = "AmericanLifeEnglish-premium")
        ) {
            purchaseFlowBegan {
                android.util.Log.i("AmericanLifeEnglishBilling", "PURCHASE_FLOW_BEGAN sku=$productId")
                onStatus("صفحه پرداخت کافه‌بازار باز شد؛ شناسه: $productId")
            }
            failedToBeginFlow { error ->
                android.util.Log.e("AmericanLifeEnglishBilling", "PURCHASE_FLOW_FAILED sku=$productId", error)
                onStatus("شروع پرداخت ناموفق بود: ${error.message ?: "خطای نامشخص"}")
            }
            purchaseSucceed { purchase ->
                android.util.Log.i("AmericanLifeEnglishBilling", "PURCHASE_SUCCEEDED sku=${purchase.productId} state=${purchase.purchaseState} order=${purchase.orderId}")
                onStatus("خرید ثبت شد؛ شناسه: ${purchase.productId}؛ در حال بررسی وضعیت اشتراک…")
                refreshSubscriptions()
            }
            purchaseCanceled { onStatus("پرداخت لغو شد") }
            purchaseFailed { error ->
                android.util.Log.e("AmericanLifeEnglishBilling", "PURCHASE_FAILED sku=$productId", error)
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
