package blog.robertotavares.cemversiculos.core.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider as GlanceColorProvider
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
import blog.robertotavares.cemversiculos.core.notification.NotificationReceiver
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import blog.robertotavares.cemversiculos.domain.repository.WidgetVerse
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.math.ceil

// Cada campo já carrega o par claro/escuro (ColorProvider), espelhando as paletas de
// presentation/theme/Theme.kt: nunca passe o mesmo valor para "day" e "night" aqui, senão o
// widget volta a ignorar o tema do sistema (foi exatamente esse o bug corrigido nesta função).
private data class WidgetColors(
    val background: GlanceColorProvider,
    val primary: GlanceColorProvider,
    val primarySoft: GlanceColorProvider,
    val text: GlanceColorProvider,
    val textMuted: GlanceColorProvider
)

private fun colorsForTheme(themeName: String): WidgetColors {
    val primaryColor = when (themeName) {
        "Oceano" -> Color(0xFF4CACE6)
        "Crepúsculo" -> Color(0xFFA594F9)
        "Areia" -> Color(0xFFE6C04C)
        else -> Color(0xFF4CE699)
    }
    val backgroundNight = when (themeName) {
        "Oceano" -> Color(0xFF0F1B21)
        "Crepúsculo" -> Color(0xFF1A1621)
        "Areia" -> Color(0xFF211D0F)
        else -> Color(0xFF112119)
    }
    val backgroundDay = when (themeName) {
        "Oceano" -> Color(0xFFF6F7F8)
        "Crepúsculo" -> Color(0xFFF8F6F9)
        "Areia" -> Color(0xFFF8F7F6)
        else -> Color(0xFFF6F8F7)
    }
    val textDay = Color(0xFF0F172A)
    val textNight = Color(0xFFF1F5F9)

    return WidgetColors(
        background = ColorProvider(day = backgroundDay, night = backgroundNight),
        primary = ColorProvider(day = primaryColor, night = primaryColor),
        primarySoft = ColorProvider(day = primaryColor.copy(alpha = 0.14f), night = primaryColor.copy(alpha = 0.24f)),
        text = ColorProvider(day = textDay, night = textNight),
        textMuted = ColorProvider(day = textDay.copy(alpha = 0.6f), night = textNight.copy(alpha = 0.7f))
    )
}

class VersiculoWidget : GlanceAppWidget() {

    // Necessário para que LocalSize.current reflita o tamanho real do widget na tela (após
    // redimensionamento pelo usuário), e assim a fonte do versículo possa se adaptar a ele.
    override val sizeMode: SizeMode = SizeMode.Exact

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

        provideContent {
            VersiculoWidgetContent(verse, themeName, category)
        }
    }
}

// Fonte do versículo se adapta ao tamanho do widget (ver SizeMode.Exact em VersiculoWidget),
// diminuindo até VERSE_MIN_FONT_SP; se mesmo assim o texto não couber, o card vira rolável
// (LazyColumn) em vez de cortar o versículo, já que a leitura completa é o essencial.
private const val VERSE_MAX_FONT_SP = 18f
private const val VERSE_MIN_FONT_SP = 13f

// Altura estimada (dp) ocupada pelo cabeçalho, aspas, referência e paddings do card, isto é,
// tudo que não é o texto do versículo em si - usada para saber quanto sobra para o versículo.
private const val VERSE_CHROME_HEIGHT_DP = 92f
private const val VERSE_REFERENCE_HEIGHT_DP = 30f

private data class VerseLayout(val fontSize: TextUnit, val scrollable: Boolean)

private fun computeVerseLayout(text: String, hasReference: Boolean, size: DpSize): VerseLayout {
    val availableWidthDp = size.width.value
    val reservedHeightDp = VERSE_CHROME_HEIGHT_DP + if (hasReference) VERSE_REFERENCE_HEIGHT_DP else 0f
    val availableHeightDp = (size.height.value - reservedHeightDp).coerceAtLeast(32f)

    var fontSize = VERSE_MAX_FONT_SP
    while (fontSize > VERSE_MIN_FONT_SP &&
        estimatedTextHeightDp(text, availableWidthDp, fontSize) > availableHeightDp
    ) {
        fontSize -= 1f
    }
    val stillOverflows = estimatedTextHeightDp(text, availableWidthDp, fontSize) > availableHeightDp
    return VerseLayout(fontSize.sp, scrollable = stillOverflows)
}

// Estimativa grosseira (sem medir texto de verdade, indisponível em RemoteViews/Glance):
// assume uma largura média de caractere e uma altura de linha proporcionais ao tamanho da fonte.
private fun estimatedTextHeightDp(text: String, availableWidthDp: Float, fontSizeSp: Float): Float {
    val avgCharWidthDp = fontSizeSp * 0.52f
    val charsPerLine = (availableWidthDp / avgCharWidthDp).toInt().coerceAtLeast(1)
    val lines = ceil(text.length / charsPerLine.toFloat()).coerceAtLeast(1f)
    val lineHeightDp = fontSizeSp * 1.35f
    return lines * lineHeightDp
}

@Composable
private fun VersiculoWidgetContent(
    verse: WidgetVerse?,
    themeName: String,
    category: String
) {
    val context = LocalContext.current
    val colors = colorsForTheme(themeName)
    val appName = context.getString(R.string.app_name)
    val size = LocalSize.current

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        if (verse != null) {
            putExtra(NotificationReceiver.EXTRA_CONTENT_ID, verse.contentId)
        }
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .cornerRadius(24.dp)
            .clickable(actionStartActivity(openAppIntent))
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
                        .size(22.dp)
                        .cornerRadius(6.dp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = appName.uppercase(),
                    style = TextStyle(
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            if (verse != null) {
                val layout = remember(verse.text, verse.reference, size) {
                    computeVerseLayout(verse.text, verse.reference != null, size)
                }

                if (layout.scrollable) {
                    LazyColumn(
                        modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            VerseText(verse, colors, layout.fontSize)
                        }
                    }
                } else {
                    Column(
                        modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VerseText(verse, colors, layout.fontSize)
                    }
                }
            } else {
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = context.getString(R.string.widget_no_verse_yet),
                        style = TextStyle(
                            color = colors.textMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun VerseText(verse: WidgetVerse, colors: WidgetColors, fontSize: TextUnit) {
    Text(
        text = "“",
        style = TextStyle(
            color = colors.primary,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
    )
    Text(
        text = verse.text,
        style = TextStyle(
            color = colors.text,
            fontSize = fontSize,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
    )
    if (verse.reference != null) {
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = verse.reference,
            style = TextStyle(
                color = colors.primary,
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
                color = colors.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
