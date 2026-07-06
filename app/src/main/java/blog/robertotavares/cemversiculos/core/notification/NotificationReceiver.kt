package blog.robertotavares.cemversiculos.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationDisplayer: NotificationDisplayer

    companion object {
        const val ACTION_SHARE_APP = "ACTION_SHARE_APP"
        const val EXTRA_CONTENT_ID = "EXTRA_CONTENT_ID"
        const val EXTRA_SHOULD_SHARE = "EXTRA_SHOULD_SHARE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationDisplayer.displayAndScheduleNext()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
