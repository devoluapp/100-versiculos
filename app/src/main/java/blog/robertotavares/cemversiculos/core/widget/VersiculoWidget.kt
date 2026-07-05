package blog.robertotavares.cemversiculos.core.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import blog.robertotavares.cemversiculos.MainActivity
import blog.robertotavares.cemversiculos.R
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import blog.robertotavares.cemversiculos.domain.repository.WidgetVerse
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private data class WidgetColors(val background: Color, val primary: Color, val text: Color, val textMuted: Color)

private fun colorsForTheme(themeName: String): WidgetColors {
    return when (themeName) {
        "Oceano" -> WidgetColors(Color(0xFFF6F7F8), Color(0xFF4CACE6), Color(0xFF0F172A), Color(0xFF64748B))
        "Crepúsculo" -> WidgetColors(Color(0xFFF8F6F9), Color(0xFFA594F9), Color(0xFF0F172A), Color(0xFF64748B))
        "Natureza" -> WidgetColors(Color(0xFFF6F8F7), Color(0xFF4CE699), Color(0xFF0F172A), Color(0xFF64748B))
        else -> WidgetColors(Color(0xFFF8F7F6), Color(0xFFE6C04C), Color(0xFF0F172A), Color(0xFF64748B))
    }
}

class VersiculoWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VersiculoWidgetEntryPoint {
        fun settingsRepository(): SettingsRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settingsRepository = EntryPointAccessors.fromApplication(
            context,
            VersiculoWidgetEntryPoint::class.java
        ).settingsRepository()

        val verse = settingsRepository.getWidgetVerse()
        val themeName = settingsRepository.getSelectedTheme()

        provideContent {
            VersiculoWidgetContent(verse, themeName)
        }
    }
}

@Composable
private fun VersiculoWidgetContent(verse: WidgetVerse?, themeName: String) {
    val context = LocalContext.current
    val colors = colorsForTheme(themeName)
    val appName = context.getString(R.string.app_name)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = appName.uppercase(),
            style = TextStyle(
                color = ColorProvider(day = colors.primary, night = colors.primary),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (verse != null) {
            Text(
                text = "\"${verse.text}\"",
                style = TextStyle(
                    color = ColorProvider(day = colors.text, night = colors.text),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 4
            )
            if (verse.reference != null) {
                Spacer(modifier = GlanceModifier.height(6.dp))
                Text(
                    text = verse.reference,
                    style = TextStyle(
                        color = ColorProvider(day = colors.textMuted, night = colors.textMuted),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        } else {
            Text(
                text = context.getString(R.string.widget_no_verse_yet),
                style = TextStyle(
                    color = ColorProvider(day = colors.text, night = colors.text),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
