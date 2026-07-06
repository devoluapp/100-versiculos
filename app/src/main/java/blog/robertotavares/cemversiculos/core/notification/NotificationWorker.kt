package blog.robertotavares.cemversiculos.core.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Substitui o antigo AlarmManager.setExactAndAllowWhileIdle: dispara o lembrete de
 * versículo via WorkManager (agendamento inexato), evitando a permissão restrita
 * SCHEDULE_EXACT_ALARM.
 */
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationDisplayer: NotificationDisplayer
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            notificationDisplayer.displayAndScheduleNext()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
