package blog.robertotavares.cemversiculos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import blog.robertotavares.cemversiculos.core.review.InAppReviewManager
import blog.robertotavares.cemversiculos.core.widget.VersiculoWidgetWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var inAppReviewManager: InAppReviewManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        VersiculoWidgetWorker.schedule(this)
        inAppReviewManager.recordAppOpened()
    }
}
