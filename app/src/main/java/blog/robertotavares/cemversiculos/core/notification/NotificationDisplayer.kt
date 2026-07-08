package blog.robertotavares.cemversiculos.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import blog.robertotavares.cemversiculos.MainActivity
import blog.robertotavares.cemversiculos.R
import blog.robertotavares.cemversiculos.core.analytics.AnalyticsHelper
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monta e exibe a notificação de versículo (compartilhada pelo disparo agendado via
 * WorkManager e pela ação "Próximo" do próprio BroadcastReceiver), e agenda a seguinte.
 */
@Singleton
class NotificationDisplayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentRepository: ContentRepository,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper
) {

    suspend fun displayAndScheduleNext() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(notificationManager)

        val selectedCategory = settingsRepository.getSelectedCategory()
        val item = contentRepository.getNextContentToDisplay(selectedCategory)

        val userName = settingsRepository.getUserName()
        val greeting = getGreeting()
        val prePhrase = PRE_PHRASES.random()

        val personalizedTitle = if (userName.isNotBlank()) {
            "$greeting, $userName! $prePhrase:"
        } else {
            "$greeting! $prePhrase:"
        }

        val contentText = if (item != null && item.reference != null) {
            "${item.text} (${item.reference})"
        } else {
            item?.text ?: "Tudo posso naquele que me fortalece."
        }

        item?.let {
            contentRepository.markAsShown(it)
        }

        // Intent para abrir o app ao clicar na notificação
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationReceiver.EXTRA_CONTENT_ID, item?.id)
        }
        val contentPi = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Intent para o botão Compartilhar (abre o app e dispara o compartilhamento)
        val shareAppIntent = Intent(context, MainActivity::class.java).apply {
            action = NotificationReceiver.ACTION_SHARE_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(NotificationReceiver.EXTRA_CONTENT_ID, item?.id)
            putExtra(NotificationReceiver.EXTRA_SHOULD_SHARE, true)
        }
        val shareAppPi = PendingIntent.getActivity(context, 1, shareAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Intent para o botão Próxima
        val nextIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_NEXT
        }
        val nextPi = PendingIntent.getBroadcast(context, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Intent para o botão Excluir
        val dismissIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_DISMISS
        }
        val dismissPi = PendingIntent.getBroadcast(context, 3, dismissIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Sem setAutoCancel: a notificação não deve sumir sozinha ao ser tocada (nem pelo
        // conteúdo, nem pelos botões), pois isso impedia o usuário de voltar à bandeja para
        // terminar de ler o versículo. A saída explícita agora é o botão Excluir (ou o gesto
        // de arrastar do sistema).
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bible)
            .setContentTitle(personalizedTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(contentPi)
            .addAction(android.R.drawable.ic_menu_share, "Compartilhar", shareAppPi)
            .addAction(android.R.drawable.ic_media_next, "Próximo", nextPi)
            .addAction(android.R.drawable.ic_menu_delete, "Excluir", dismissPi)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        analyticsHelper.logNotificacaoExibida(item?.reference)

        NotificationHelper.scheduleNext(context, settingsRepository)
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Bom dia"
            in 12..17 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    private fun createChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Versículos do dia",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "MIND_CHANNEL_ID"

        private val PRE_PHRASES = listOf(
            "um versículo para você",
            "medite nesta palavra",
            "Palavra do dia",
            "receba esta mensagem",
            "uma palavra de esperança",
            "fortaleça seu coração",
            "leia e medite",
            "mensagem de fé"
        )
    }
}
