package blog.robertotavares.cemversiculos.presentation.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import blog.robertotavares.cemversiculos.core.billing.BillingManager
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val products = billingManager.products
    val isPremium = settingsRepository.getPremiumFlow()

    fun buyProduct(activity: Activity, productDetails: ProductDetails) {
        billingManager.launchBillingFlow(activity, productDetails)
    }
}
