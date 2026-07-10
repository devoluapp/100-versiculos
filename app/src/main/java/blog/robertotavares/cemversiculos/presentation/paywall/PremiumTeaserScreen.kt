package blog.robertotavares.cemversiculos.presentation.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import blog.robertotavares.cemversiculos.R

/**
 * Cada variação destaca UM benefício, para não repetir sempre a mesma mensagem no mesmo lugar
 * onde antes ficava o interstitial. A rotação entre elas é feita por índice persistido em
 * SettingsRepository (ver HomeViewModel.onVerseSwiped) - não é aleatória, é sequencial, para
 * garantir que o usuário veja todos os ângulos ao longo do tempo em vez de sempre cair no mesmo.
 */
private data class TeaserVariant(val titleRes: Int, val descRes: Int, val icon: ImageVector)

private val TEASER_VARIANTS = listOf(
    TeaserVariant(R.string.teaser_title_no_ads, R.string.teaser_desc_no_ads, Icons.Default.Block),
    TeaserVariant(R.string.teaser_title_categories, R.string.teaser_desc_categories, Icons.Default.Category),
    TeaserVariant(R.string.teaser_title_favorites, R.string.teaser_desc_favorites, Icons.Default.Favorite),
    TeaserVariant(R.string.teaser_title_notifications, R.string.teaser_desc_notifications, Icons.Default.NotificationsActive),
    TeaserVariant(R.string.teaser_title_support, R.string.teaser_desc_support, Icons.Default.VolunteerActivism)
)

val PREMIUM_TEASER_VARIANT_COUNT: Int get() = TEASER_VARIANTS.size

@Composable
fun PremiumTeaserDialog(
    variantIndex: Int,
    onViewPlans: () -> Unit,
    onDismiss: () -> Unit
) {
    val variant = TEASER_VARIANTS[variantIndex % TEASER_VARIANTS.size]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(Color(0xFFFFD700).copy(alpha = 0.15f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = variant.icon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFFD700)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(variant.titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(variant.descRes),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onViewPlans,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        stringResource(R.string.action_view_premium_plans),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_continue_reading))
                }
            }
        }
    }
}
