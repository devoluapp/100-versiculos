package blog.robertotavares.cemversiculos.presentation.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import blog.robertotavares.cemversiculos.R
import com.android.billingclient.api.ProductDetails

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    LaunchedEffect(isPremium) {
        if (isPremium) onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
                }
            }

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFFFFD700)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.title_be_premium),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.subtitle_premium),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            PremiumFeaturesList()

            Spacer(modifier = Modifier.height(40.dp))

            if (products.isEmpty()) {
                if (isLoading) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.label_loading_offers), modifier = Modifier.padding(top = 16.dp))
                } else {
                    Text(
                        text = stringResource(R.string.label_offers_unavailable),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text(stringResource(R.string.action_try_again))
                    }
                }
            } else {
                val monthlyProduct = products.firstOrNull { it.productId == "mensal_990" }
                val annualProduct = products.firstOrNull { it.productId == "anual_5900" }
                val lifetimeProduct = products.firstOrNull { it.productId == "vitalicio_14900" }

                monthlyProduct?.let { product ->
                    PlanCard(
                        title = stringResource(R.string.label_plan_monthly),
                        subtitle = stringResource(R.string.label_full_flexibility),
                        price = product.recurringPrice(),
                        highlighted = false,
                        badge = null,
                        onClick = {
                            (context as? Activity)?.let { viewModel.buyProduct(it, product) }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                annualProduct?.let { product ->
                    PlanCard(
                        title = stringResource(R.string.label_plan_annual),
                        subtitle = stringResource(R.string.label_best_value),
                        price = product.recurringPrice(),
                        highlighted = true,
                        badge = stringResource(R.string.badge_free_trial),
                        onClick = {
                            (context as? Activity)?.let { viewModel.buyProduct(it, product) }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                lifetimeProduct?.let { product ->
                    PlanCard(
                        title = stringResource(R.string.label_plan_lifetime),
                        subtitle = stringResource(R.string.label_lifetime_subtitle),
                        price = product.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
                        highlighted = false,
                        badge = null,
                        onClick = {
                            (context as? Activity)?.let { viewModel.buyProduct(it, product) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_cancel_anytime),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun PremiumFeaturesList() {
    val features = listOf(
        stringResource(R.string.feature_notifications),
        stringResource(R.string.feature_categories),
        stringResource(R.string.feature_themes),
        stringResource(R.string.feature_no_ads),
        stringResource(R.string.feature_favorites),
        stringResource(R.string.feature_share_layouts)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        features.forEach { feature ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = feature, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * Preço recorrente de uma assinatura: pega a última fase de preço (após um eventual
 * período de teste grátis), para não exibir "Grátis" como preço principal do card.
 */
private fun ProductDetails.recurringPrice(): String {
    return subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: ""
}

@Composable
fun PlanCard(
    title: String,
    subtitle: String,
    price: String,
    highlighted: Boolean,
    badge: String?,
    onClick: () -> Unit
) {
    Column {
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = badge.uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                width = if (highlighted) 2.dp else 1.dp,
                color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Text(text = price, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
