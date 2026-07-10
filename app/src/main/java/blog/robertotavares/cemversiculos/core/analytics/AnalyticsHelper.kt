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

    fun logNotificacaoExibida(referencia: String?) {
        firebaseAnalytics.logEvent(EVENT_NOTIFICACAO_EXIBIDA, Bundle().apply {
            referencia?.let { putString(PARAM_REFERENCIA, it) }
        })
    }

    /** Ver comentário em BillingManager.reportBillingFailure sobre por que isto existe. */
    fun logBillingErro(estagio: String, codigoResposta: Int?) {
        firebaseAnalytics.logEvent(EVENT_BILLING_ERRO, Bundle().apply {
            putString(PARAM_ESTAGIO, estagio)
            codigoResposta?.let { putInt(PARAM_CODIGO_RESPOSTA, it) }
        })
    }

    fun logPremiumTeaserExibido(variante: Int) {
        firebaseAnalytics.logEvent(EVENT_PREMIUM_TEASER_EXIBIDO, Bundle().apply {
            putInt(PARAM_VARIANTE, variante)
        })
    }

    companion object {
        private const val EVENT_PAYWALL_VISTO = "paywall_visto"
        private const val EVENT_ASSINATURA_INICIADA = "assinatura_iniciada"
        private const val EVENT_CATEGORIA_SELECIONADA = "categoria_selecionada"
        private const val EVENT_VERSICULO_COMPARTILHADO = "versiculo_compartilhado"
        private const val EVENT_REWARDED_ASSISTIDO = "rewarded_assistido"
        private const val EVENT_NOTIFICACAO_EXIBIDA = "notificacao_exibida"
        private const val EVENT_BILLING_ERRO = "billing_erro"
        private const val EVENT_PREMIUM_TEASER_EXIBIDO = "premium_teaser_exibido"

        private const val PARAM_ORIGEM = "origem"
        private const val PARAM_CATEGORIA = "categoria"
        private const val PARAM_REFERENCIA = "referencia"
        private const val PARAM_ESTAGIO = "estagio"
        private const val PARAM_CODIGO_RESPOSTA = "codigo_resposta"
        private const val PARAM_VARIANTE = "variante"
    }
}
