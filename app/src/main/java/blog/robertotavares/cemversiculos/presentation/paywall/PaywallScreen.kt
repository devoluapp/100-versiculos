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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
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
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
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
                text = "Seja Premium",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Desbloqueie todo o poder da Palavra de Deus no seu dia a dia.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            PremiumFeaturesList()

            Spacer(modifier = Modifier.height(40.dp))

            products.forEach { product ->
                val price = product.subscriptionOfferDetails?.getOrNull(0)
                    ?.pricingPhases?.pricingPhaseList?.getOrNull(0)?.formattedPrice ?: ""
                
                val title = if (product.productId.contains("anual")) "Plano Anual" else "Plano Mensal"
                val subtitle = if (product.productId.contains("anual")) "Melhor Custo-Benefício" else "Flexibilidade Total"

                SubscriptionCard(
                    title = title,
                    subtitle = subtitle,
                    price = price,
                    isSelected = false,
                    onClick = {
                        val activity = context as? Activity
                        activity?.let { viewModel.buyProduct(it, product) }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (products.isEmpty()) {
                CircularProgressIndicator()
                Text("Carregando ofertas...", modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Cancele quando quiser na Google Play Store.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun PremiumFeaturesList() {
    val features = listOf(
        "Até 20 notificações personalizadas por dia",
        "Acesso a todas as categorias avançadas",
        "Temas visuais exclusivos",
        "Experiência 100% sem anúncios",
        "Favoritos e histórico ilimitado",
        "Layouts de compartilhamento Premium"
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

@Composable
fun SubscriptionCard(
    title: String,
    subtitle: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
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
