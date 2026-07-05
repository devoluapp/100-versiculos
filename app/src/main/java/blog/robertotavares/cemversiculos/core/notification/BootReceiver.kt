package blog.robertotavares.cemversiculos.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            NotificationHelper.scheduleNext(context, settingsRepository)
        }
    }
}
