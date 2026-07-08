package blog.robertotavares.cemversiculos.presentation.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import blog.robertotavares.cemversiculos.core.analytics.AnalyticsHelper
import blog.robertotavares.cemversiculos.core.billing.BillingManager
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    val products = billingManager.products
    val isLoading = billingManager.isLoading
    val isPremium = settingsRepository.getPremiumFlow()

    init {
        analyticsHelper.logPaywallVisto()
    }

    fun buyProduct(activity: Activity, productDetails: ProductDetails) {
        analyticsHelper.logAssinaturaIniciada(productDetails.productId)
        billingManager.launchBillingFlow(activity, productDetails)
    }

    fun retry() {
        billingManager.retry()
    }
}
