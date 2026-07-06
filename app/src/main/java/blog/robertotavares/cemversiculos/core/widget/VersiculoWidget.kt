package blog.robertotavares.cemversiculos.core.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import blog.robertotavares.cemversiculos.MainActivity
import blog.robertotavares.cemversiculos.R
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import blog.robertotavares.cemversiculos.domain.repository.WidgetVerse
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private data class WidgetColors(
    val background: Color,
    val primary: Color,
    val primarySoft: Color,
    val text: Color,
    val textMuted: Color
)

private fun colorsForTheme(themeName: String): WidgetColors {
    val primary = when (themeName) {
        "Oceano" -> Color(0xFF4CACE6)
        "Crepúsculo" -> Color(0xFFA594F9)
        "Areia" -> Color(0xFFE6C04C)
        else -> Color(0xFF4CE699)
    }
    return WidgetColors(
        background = Color(0xFFFCFBF9),
        primary = primary,
        primarySoft = primary.copy(alpha = 0.14f),
        text = Color(0xFF1C1B1F),
        textMuted = Color(0xFF6B6B6B)
    )
}

class VersiculoWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VersiculoWidgetEntryPoint {
        fun settingsRepository(): SettingsRepository
        fun contentRepository(): ContentRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            VersiculoWidgetEntryPoint::class.java
        )
        val settingsRepository = entryPoint.settingsRepository()
        val contentRepository = entryPoint.contentRepository()

        val themeName = settingsRepository.getSelectedTheme()
        val category = settingsRepository.getSelectedCategory()

        // Autossuficiente: se ainda não há versículo em cache (ex.: worker diário
        // nunca rodou), busca um agora mesmo para não deixar o widget vazio.
        var verse = settingsRepository.getWidgetVerse()
        if (verse == null) {
            val content = contentRepository.getNextContentToDisplay(category)
            if (content != null) {
                verse = WidgetVerse(content.text, content.reference, content.id)
                settingsRepository.saveWidgetVerse(verse)
                contentRepository.markAsShown(content)
            }
        }

        // Permite "voltar" apenas se o versículo atual não for o primeiro
        // da categoria na ordem de navegação sequencial dos botões do widget.
        val canGoBack = verse?.let { v ->
            val orderedContents = contentRepository.getOrderedContents(category)
            orderedContents.indexOfFirst { it.id == v.contentId } > 0
        } ?: false

        provideContent {
            VersiculoWidgetContent(verse, themeName, category, canGoBack)
        }
    }
}

/**
 * Move o versículo exibido no widget para o próximo (direção +1, com retorno ao
 * início ao chegar no fim) ou para o anterior (direção -1, sem efeito se já for o
 * primeiro da categoria), sem precisar abrir o app.
 */
class VersiculoWidgetNavigationAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val direction = parameters[directionKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            VersiculoWidget.VersiculoWidgetEntryPoint::class.java
        )
        val settingsRepository = entryPoint.settingsRepository()
        val contentRepository = entryPoint.contentRepository()

        val category = settingsRepository.getSelectedCategory()
        val orderedContents = contentRepository.getOrderedContents(category)
        if (orderedContents.isEmpty()) return

        val currentVerse = settingsRepository.getWidgetVerse()
        val currentIndex = currentVerse
            ?.let { v -> orderedContents.indexOfFirst { it.id == v.contentId } }
            ?.takeIf { it >= 0 }
            ?: 0

        val newIndex = if (direction > 0) {
            (currentIndex + 1) % orderedContents.size
        } else {
            if (currentIndex <= 0) return
            currentIndex - 1
        }

        val newContent = orderedContents[newIndex]
        settingsRepository.saveWidgetVerse(
            WidgetVerse(newContent.text, newContent.reference, newContent.id)
        )
        contentRepository.markAsShown(newContent)
        VersiculoWidget().updateAll(context)
    }

    companion object {
        val directionKey = ActionParameters.Key<Int>("nav_direction")
    }
}

@Composable
private fun VersiculoWidgetContent(
    verse: WidgetVerse?,
    themeName: String,
    category: String,
    canGoBack: Boolean
) {
    val context = LocalContext.current
    val colors = colorsForTheme(themeName)
    val appName = context.getString(R.string.app_name)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .cornerRadius(24.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        // Faixa de destaque colorida no topo, no tom do tema selecionado
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(6.dp)
                .background(colors.primary)
        ) {}

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category, colors)
                Spacer(modifier = GlanceModifier.width(8.dp))
                Box(modifier = GlanceModifier.defaultWeight()) {}
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher),
                    contentDescription = appName,
                    modifier = GlanceModifier
                        .size(16.dp)
                        .cornerRadius(4.dp)
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = appName.uppercase(),
                    style = TextStyle(
                        color = ColorProvider(day = colors.textMuted, night = colors.textMuted),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Column(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (verse != null) {
                    Text(
                        text = "“",
                        style = TextStyle(
                            color = ColorProvider(day = colors.primary, night = colors.primary),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = verse.text,
                        style = TextStyle(
                            color = ColorProvider(day = colors.text, night = colors.text),
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 5
                    )
                    if (verse.reference != null) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = verse.reference,
                            style = TextStyle(
                                color = ColorProvider(day = colors.primary, night = colors.primary),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                } else {
                    Text(
                        text = context.getString(R.string.widget_no_verse_yet),
                        style = TextStyle(
                            color = ColorProvider(day = colors.textMuted, night = colors.textMuted),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            if (verse != null) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavButton(
                        symbol = "‹",
                        colors = colors,
                        enabled = canGoBack,
                        action = if (canGoBack) {
                            actionRunCallback<VersiculoWidgetNavigationAction>(
                                actionParametersOf(VersiculoWidgetNavigationAction.directionKey to -1)
                            )
                        } else null
                    )
                    Box(modifier = GlanceModifier.defaultWeight()) {}
                    NavButton(
                        symbol = "›",
                        colors = colors,
                        enabled = true,
                        action = actionRunCallback<VersiculoWidgetNavigationAction>(
                            actionParametersOf(VersiculoWidgetNavigationAction.directionKey to 1)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NavButton(symbol: String, colors: WidgetColors, enabled: Boolean, action: Action?) {
    var modifier = GlanceModifier
        .size(28.dp)
        .background(colors.primarySoft)
        .cornerRadius(14.dp)
    if (action != null) {
        modifier = modifier.clickable(action)
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = symbol,
            style = TextStyle(
                color = ColorProvider(
                    day = if (enabled) colors.primary else colors.textMuted,
                    night = if (enabled) colors.primary else colors.textMuted
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun CategoryBadge(category: String, colors: WidgetColors) {
    Box(
        modifier = GlanceModifier
            .background(colors.primarySoft)
            .cornerRadius(10.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = category.uppercase(),
            style = TextStyle(
                color = ColorProvider(day = colors.primary, night = colors.primary),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
