package blog.robertotavares.cemversiculos.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import blog.robertotavares.cemversiculos.MainActivity
import blog.robertotavares.cemversiculos.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var contentRepository: ContentRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "MIND_CHANNEL_ID"
        const val ACTION_SHARE_APP = "ACTION_SHARE_APP"
        const val EXTRA_CONTENT_ID = "EXTRA_CONTENT_ID"
        const val EXTRA_SHOULD_SHARE = "EXTRA_SHOULD_SHARE"

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

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            "ACTION_NEXT" -> { 
                CoroutineScope(Dispatchers.IO).launch {
                    displayNotificationAndScheduleNext(context)
                }
            }
            else -> {
                CoroutineScope(Dispatchers.IO).launch {
                    displayNotificationAndScheduleNext(context)
                }
            }
        }
    }

    private suspend fun displayNotificationAndScheduleNext(context: Context) {
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
            putExtra(EXTRA_CONTENT_ID, item?.id)
        }
        val contentPi = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Intent para o botão Compartilhar (abre o app e dispara o compartilhamento)
        val shareAppIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_SHARE_APP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONTENT_ID, item?.id)
            putExtra(EXTRA_SHOULD_SHARE, true)
        }
        val shareAppPi = PendingIntent.getActivity(context, 1, shareAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        // Intent para o botão Próxima
        val nextIntent = Intent(context, NotificationReceiver::class.java).apply { 
            action = "ACTION_NEXT" 
        }
        val nextPi = PendingIntent.getBroadcast(context, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bible)
            .setContentTitle(personalizedTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(contentPi)
            .addAction(android.R.drawable.ic_menu_share, "Compartilhar", shareAppPi)
            .addAction(android.R.drawable.ic_media_next, "Próximo", nextPi)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(NOTIFICATION_ID, notification)

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
}
