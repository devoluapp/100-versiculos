package blog.robertotavares.cemversiculos.core.review

import android.app.Activity
import android.content.Context
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controla o gatilho da avaliação in-app (Play In-App Review): elegibilidade por
 * dias de uso distintos ou quantidade de favoritos, e frequência mínima entre pedidos
 * para não incomodar o usuário nem chamar a API à toa em toda sessão.
 */
@Singleton
class InAppReviewManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    private val reviewManager = ReviewManagerFactory.create(context)

    /** Deve ser chamado uma vez por processo (ex.: Application.onCreate) para contar dias de uso. */
    fun recordAppOpened() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (settingsRepository.getLastUsageDate() != today) {
            settingsRepository.saveLastUsageDate(today)
            settingsRepository.saveUsageDaysCount(settingsRepository.getUsageDaysCount() + 1)
        }
    }

    /**
     * Dispara o fluxo nativo de avaliação do Play Store se o usuário já estiver no
     * 3º dia de uso ou tiver ao menos 5 favoritos, respeitando o intervalo mínimo
     * entre pedidos.
     */
    fun maybeRequestReview(activity: Activity, favoriteCount: Int) {
        if (!isEligible(favoriteCount) || !canRequestNow()) return

        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                settingsRepository.saveLastReviewRequestTimestamp(System.currentTimeMillis())
                reviewManager.launchReviewFlow(activity, task.result)
            }
        }
    }

    private fun isEligible(favoriteCount: Int): Boolean {
        return settingsRepository.getUsageDaysCount() >= USAGE_DAYS_THRESHOLD ||
            favoriteCount >= FAVORITE_THRESHOLD
    }

    private fun canRequestNow(): Boolean {
        val lastRequest = settingsRepository.getLastReviewRequestTimestamp()
        if (lastRequest == 0L) return true
        return System.currentTimeMillis() - lastRequest >= MIN_INTERVAL_MILLIS
    }

    companion object {
        private const val USAGE_DAYS_THRESHOLD = 3
        private const val FAVORITE_THRESHOLD = 5
        private const val MIN_INTERVAL_MILLIS = 30L * 24 * 60 * 60 * 1000 // 30 dias
    }
}
