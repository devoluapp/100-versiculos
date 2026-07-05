package blog.robertotavares.cemversiculos.core.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import blog.robertotavares.cemversiculos.domain.repository.WidgetVerse
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class VersiculoWidgetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contentRepository: ContentRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val category = settingsRepository.getSelectedCategory()
            val content = contentRepository.getNextContentToDisplay(category)
            if (content != null) {
                settingsRepository.saveWidgetVerse(WidgetVerse(content.text, content.reference))
                contentRepository.markAsShown(content)
                VersiculoWidget().updateAll(applicationContext)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "versiculo_widget_daily_update"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<VersiculoWidgetWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun refreshNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<VersiculoWidgetWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
