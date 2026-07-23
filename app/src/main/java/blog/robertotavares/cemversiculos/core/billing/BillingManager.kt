package blog.robertotavares.cemversiculos.core.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import blog.robertotavares.cemversiculos.core.analytics.AnalyticsHelper
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import com.android.billingclient.api.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper
) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products = _products.asStateFlow()

    // Sem isto, uma falha de conexão ou de consulta (ex.: ofertas ainda propagando no Play
    // Console, sem Play Store logada, indisponibilidade transitória) deixava a Paywall presa
    // para sempre em "Carregando ofertas...", já que a tela só decidia o que mostrar olhando
    // se `products` estava vazia - sem jeito de diferenciar "ainda carregando" de "terminou e
    // não veio nada". Ver PaywallScreen.kt.
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    // Escopo de vida do singleton (app inteiro), só para o timeout de segurança abaixo.
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var loadingTimeoutJob: Job? = null

    init {
        startConnection()
    }

    private fun startConnection() {
        _isLoading.value = true
        armLoadingTimeout()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    checkActivePurchases()
                } else {
                    reportBillingFailure("setup", billingResult)
                    _isLoading.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                reportBillingFailure("service_disconnected", null)
                _isLoading.value = false
            }
        })
    }

    /**
     * Trava de segurança: a correção anterior (setar isLoading=false em todo desfecho do
     * BillingClientStateListener/queryProductDetailsAsync) partia do pressuposto de que o SDK
     * SEMPRE chama algum callback de volta. Na prática isso continuou preso em "Carregando
     * ofertas..." - ou seja, nem onBillingSetupFinished, nem onBillingServiceDisconnected, nem o
     * callback de queryProductDetailsAsync estão disparando (serviço do Play trava a conexão
     * silenciosamente em vez de retornar erro). Sem depender de nenhum callback do SDK, isto
     * garante que a UI sempre sai do estado de loading em até TIMEOUT_MS, e loga esse caso
     * separado ("timeout") para diferenciar de uma falha explícita do BillingClient.
     */
    private fun armLoadingTimeout() {
        loadingTimeoutJob?.cancel()
        loadingTimeoutJob = managerScope.launch {
            delay(LOADING_TIMEOUT_MS)
            if (_isLoading.value) {
                reportBillingFailure("timeout", null)
                _isLoading.value = false
            }
        }
    }

    /**
     * Loga em Logcat + Crashlytics + Analytics todo desfecho de billing que não seja OK. Sem
     * isto, "a paywall não carrega" em produção é impossível de diagnosticar remotamente: o
     * código não tem como distinguir se o BillingClient nunca conectou, se a consulta voltou
     * ITEM_UNAVAILABLE (produto não existe/não está ativo no Play Console), BILLING_UNAVAILABLE
     * (sem conta de pagamentos configurada) ou outro código - cada um aponta para uma causa e
     * correção diferentes.
     */
    private fun reportBillingFailure(stage: String, billingResult: BillingResult?) {
        val code = billingResult?.responseCode
        val message = billingResult?.debugMessage
        Log.w(TAG, "Falha de billing em '$stage': code=$code message=$message")
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("billing_failure_stage", stage)
            code?.let { setCustomKey("billing_failure_code", it) }
            recordException(BillingFailureException(stage, code, message))
        }
        analyticsHelper.logBillingErro(stage, code)
    }

    /** Chamado pela tela de paywall quando o usuário toca em "Tentar novamente". */
    fun retry() {
        if (billingClient.isReady) {
            _isLoading.value = true
            armLoadingTimeout()
            queryProducts()
        } else {
            startConnection()
        }
    }

    /**
     * A Billing Library exige que cada QueryProductDetailsParams contenha produtos de um único
     * ProductType - misturar SUBS e INAPP na mesma lista lança IllegalArgumentException("All
     * products should be of the same product type"), derrubando a paywall antes mesmo do
     * callback rodar. Por isso a consulta é feita em duas chamadas separadas e os resultados são
     * combinados, no mesmo padrão de junção já usado em checkActivePurchases().
     */
    private fun queryProducts() {
        val subsProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("mensal_990")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("anual_5900")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val inappProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("vitalicio_14900")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val results = mutableListOf<ProductDetails>()
        var subsDone = false
        var inappDone = false
        var hadError = false

        fun finishIfDone() {
            if (subsDone && inappDone) {
                _products.value = results
                if (results.isEmpty() && !hadError) {
                    // OK mas lista vazia: os IDs de produto não existem ou não estão ativos no
                    // Play Console para este app/conta (causa mais comum de "paywall vazia").
                    reportBillingFailure("query_products_empty", null)
                }
                _isLoading.value = false
            }
        }

        val subsParams = QueryProductDetailsParams.newBuilder().setProductList(subsProducts).build()
        billingClient.queryProductDetailsAsync(subsParams) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                results.addAll(queryProductDetailsResult.productDetailsList)
            } else {
                hadError = true
                reportBillingFailure("query_products_subs", billingResult)
            }
            subsDone = true
            finishIfDone()
        }

        val inappParams = QueryProductDetailsParams.newBuilder().setProductList(inappProducts).build()
        billingClient.queryProductDetailsAsync(inappParams) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                results.addAll(queryProductDetailsResult.productDetailsList)
            } else {
                hadError = true
                reportBillingFailure("query_products_inapp", billingResult)
            }
            inappDone = true
            finishIfDone()
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.getOrNull(0)?.offerToken

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        if (offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        settingsRepository.saveIsPremium(true)
                    }
                }
            } else {
                settingsRepository.saveIsPremium(true)
            }
        }
    }

    private fun checkActivePurchases() {
        var hasActiveSubscription = false
        var hasLifetimePurchase = false
        var subsChecked = false
        var inappChecked = false

        fun evaluate() {
            if (subsChecked && inappChecked) {
                settingsRepository.saveIsPremium(hasActiveSubscription || hasLifetimePurchase)
            }
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                hasActiveSubscription = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            }
            subsChecked = true
            evaluate()
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                hasLifetimePurchase = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            }
            inappChecked = true
            evaluate()
        }
    }

    /** Carrega mensagem/código do BillingResult no Crashlytics, já que a stack trace sozinha não diz o motivo. */
    private class BillingFailureException(stage: String, code: Int?, message: String?) :
        Exception("Billing falhou em '$stage': code=$code message=$message")

    companion object {
        private const val TAG = "BillingManager"
        private const val LOADING_TIMEOUT_MS = 10_000L
    }
}
