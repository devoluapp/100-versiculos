package blog.robertotavares.cemversiculos.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import blog.robertotavares.cemversiculos.BuildConfig
import blog.robertotavares.cemversiculos.core.analytics.AnalyticsHelper
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper
) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    private val _isMobileAdsInitialized = MutableStateFlow(false)
    val isMobileAdsInitialized = _isMobileAdsInitialized.asStateFlow()

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewardedAd = false

    val bannerAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else BuildConfig.ADMOB_BANNER_AD_UNIT_ID

    private val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_AD_UNIT_ID else BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID

    private val rewardedAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_AD_UNIT_ID else BuildConfig.ADMOB_REWARDED_AD_UNIT_ID

    private val isPremium: Boolean
        get() = settingsRepository.getPremiumFlow().value

    /**
     * Solicita atualização de consentimento via UMP e, somente se o usuário puder receber
     * anúncios (consentimento obtido ou não exigido) e não for Premium, inicializa o Mobile Ads SDK.
     */
    fun requestConsentAndInitialize(activity: Activity) {
        if (isPremium || _isMobileAdsInitialized.value) return

        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Erro ao exibir formulário de consentimento: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds()
                    }
                }
            },
            { requestError ->
                Log.w(TAG, "Erro ao atualizar informações de consentimento: ${requestError.message}")
                if (consentInformation.canRequestAds()) {
                    initializeMobileAds()
                }
            }
        )
    }

    private fun initializeMobileAds() {
        if (isPremium || _isMobileAdsInitialized.value) return
        MobileAds.initialize(context) {
            _isMobileAdsInitialized.value = true
            loadInterstitial()
            loadRewardedAd()
        }
    }

    fun loadInterstitial() {
        if (isPremium || !_isMobileAdsInitialized.value || isLoadingInterstitial || interstitialAd != null) return

        isLoadingInterstitial = true
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            }
        )
    }

    /**
     * Exibe o interstitial já carregado, se houver. Sempre no-op para usuários Premium.
     * Depois de exibido (ou se não houver anúncio pronto), dispara o carregamento do próximo.
     */
    fun showInterstitialIfAvailable(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (isPremium) {
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            loadInterstitial()
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitial()
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitial()
                onAdDismissed()
            }
        }
        ad.show(activity)
    }

    fun loadRewardedAd() {
        if (isPremium || !_isMobileAdsInitialized.value || isLoadingRewardedAd || rewardedAd != null) return

        isLoadingRewardedAd = true
        RewardedAd.load(
            context,
            rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewardedAd = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoadingRewardedAd = false
                }
            }
        )
    }

    /**
     * Exibe o rewarded já carregado, se houver, chamando [onRewardEarned] apenas se o usuário
     * assistir até o fim. Se não houver anúncio pronto, dispara o carregamento e chama
     * [onAdUnavailable] imediatamente. Sempre no-op para usuários Premium.
     */
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdUnavailable: () -> Unit = {}) {
        if (isPremium) {
            onAdUnavailable()
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            loadRewardedAd()
            onAdUnavailable()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewardedAd()
            }
        }

        ad.show(activity, OnUserEarnedRewardListener {
            analyticsHelper.logRewardedAssistido()
            onRewardEarned()
        })
    }

    companion object {
        private const val TAG = "AdManager"

        // IDs de teste oficiais do Google (https://developers.google.com/admob/android/test-ads)
        private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
        private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
