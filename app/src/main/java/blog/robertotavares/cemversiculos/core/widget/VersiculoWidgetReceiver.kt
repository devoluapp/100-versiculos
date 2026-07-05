package blog.robertotavares.cemversiculos.core.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class VersiculoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VersiculoWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        VersiculoWidgetWorker.schedule(context)
        VersiculoWidgetWorker.refreshNow(context)
    }
}
