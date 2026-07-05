package blog.robertotavares.cemversiculos.core.notification

import android.content.Context
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import java.util.*

object NotificationHelper {

    fun scheduleNext(context: Context, settingsRepository: SettingsRepository) {
        val scheduler = AlarmSchedulerImpl(context)
        
        val startTime = settingsRepository.getNotificationStartTime()
        val endTime = settingsRepository.getNotificationEndTime()
        var frequency = settingsRepository.getNotificationFrequency()
        val isPremium = settingsRepository.isPremium()

        // Enforce limits for free users
        if (!isPremium && frequency > 5) {
            frequency = 5
        }

        val nextTimeMs = calculateNextTriggerTime(startTime, endTime, frequency)
        scheduler.scheduleNextNotification(nextTimeMs)
    }

    fun scheduleImmediate(context: Context) {
        val scheduler = AlarmSchedulerImpl(context)
        scheduler.scheduleNextNotification(System.currentTimeMillis() + 5000)
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
