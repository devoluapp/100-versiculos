package blog.robertotavares.cemversiculos.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centraliza o disparo dos eventos customizados de Firebase Analytics do app,
 * para manter os nomes e parâmetros de evento consistentes em todos os pontos de uso.
 */
@Singleton
class AnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    fun logPaywallVisto(origem: String? = null) {
        firebaseAnalytics.logEvent(EVENT_PAYWALL_VISTO, Bundle().apply {
            origem?.let { putString(PARAM_ORIGEM, it) }
        })
    }

    fun logAssinaturaIniciada(productId: String) {
        firebaseAnalytics.logEvent(EVENT_ASSINATURA_INICIADA, Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, productId)
        })
    }

    fun logCategoriaSelecionada(categoria: String) {
        firebaseAnalytics.logEvent(EVENT_CATEGORIA_SELECIONADA, Bundle().apply {
            putString(PARAM_CATEGORIA, categoria)
        })
    }

    fun logVersiculoCompartilhado(referencia: String?) {
        firebaseAnalytics.logEvent(EVENT_VERSICULO_COMPARTILHADO, Bundle().apply {
            referencia?.let { putString(PARAM_REFERENCIA, it) }
        })
    }

    fun logRewardedAssistido() {
        firebaseAnalytics.logEvent(EVENT_REWARDED_ASSISTIDO, Bundle())
    }

    companion object {
        private const val EVENT_PAYWALL_VISTO = "paywall_visto"
        private const val EVENT_ASSINATURA_INICIADA = "assinatura_iniciada"
        private const val EVENT_CATEGORIA_SELECIONADA = "categoria_selecionada"
        private const val EVENT_VERSICULO_COMPARTILHADO = "versiculo_compartilhado"
        private const val EVENT_REWARDED_ASSISTIDO = "rewarded_assistido"

        private const val PARAM_ORIGEM = "origem"
        private const val PARAM_CATEGORIA = "categoria"
        private const val PARAM_REFERENCIA = "referencia"
    }
}
