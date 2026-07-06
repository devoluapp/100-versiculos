package blog.robertotavares.cemversiculos.core.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationHelper {

    // Nome único do WorkManager para o lembrete agendado, substituindo o antigo alarme
    // exato do AlarmManager (restrito a partir do Android 12/13 sem permissão do usuário).
    private const val REMINDER_WORK_NAME = "versiculo_reminder_notification"

    fun scheduleNext(context: Context, settingsRepository: SettingsRepository) {
        val startTime = settingsRepository.getNotificationStartTime()
        val endTime = settingsRepository.getNotificationEndTime()
        var frequency = settingsRepository.getNotificationFrequency()
        val isPremium = settingsRepository.isPremium()

        // Enforce limits for free users
        if (!isPremium && frequency > 5) {
            frequency = 5
        }

        val nextTimeMs = calculateNextTriggerTime(startTime, endTime, frequency)
        scheduleReminderWork(context, nextTimeMs)
    }

    fun scheduleImmediate(context: Context) {
        scheduleReminderWork(context, System.currentTimeMillis() + 5000)
    }

    private fun scheduleReminderWork(context: Context, triggerTimeMs: Long) {
        val delayMs = (triggerTimeMs - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            REMINDER_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun calculateNextTriggerTime(startTimeStr: String, endTimeStr: String, frequency: Int): Long {
        val now = Calendar.getInstance()
        
        val startParts = startTimeStr.split(":")
        val startHour = startParts[0].toInt()
        val startMin = startParts[1].toInt()
        
        val endParts = endTimeStr.split(":")
        val endHour = endParts[0].toInt()
        val endMin = endParts[1].toInt()

        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMin)
            set(Calendar.SECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMin)
            set(Calendar.SECOND, 0)
        }

        var startMillis = startCal.timeInMillis
        var endMillis = endCal.timeInMillis
        
        if (endMillis <= startMillis) {
            endCal.add(Calendar.DAY_OF_YEAR, 1)
            endMillis = endCal.timeInMillis
        }

        val totalDurationMillis = endMillis - startMillis
        val intervalMillis = totalDurationMillis / frequency

        var nextTrigger = now.timeInMillis + intervalMillis
        
        val currentEndCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMin)
            set(Calendar.SECOND, 0)
        }
        
        if (nextTrigger > currentEndCal.timeInMillis) {
            val nextStartCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMin)
                set(Calendar.SECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }
            nextTrigger = nextStartCal.timeInMillis
        } else if (nextTrigger < startCal.timeInMillis) {
             nextTrigger = startCal.timeInMillis
        }

        return nextTrigger
    }
}
