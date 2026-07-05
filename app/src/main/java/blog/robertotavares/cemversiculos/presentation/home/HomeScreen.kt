package blog.robertotavares.cemversiculos.presentation.home

import android.graphics.Bitmap
import android.graphics.Picture
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import blog.robertotavares.cemversiculos.R
import blog.robertotavares.cemversiculos.core.utils.ShareUtils
import blog.robertotavares.cemversiculos.data.local.ContentItemEntity
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val contents by viewModel.contents.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val targetContentId by viewModel.targetContentId.collectAsState()
    val shouldTriggerShare by viewModel.shouldTriggerShare.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    
    var showTutorial by remember { mutableStateOf(!viewModel.isOnboardingCompleted()) }

    val stableContents = remember(contents) { contents }

    val virtualCount = 10000
    val pagerState = rememberPagerState(
        initialPage = virtualCount / 2,
        pageCount = { if (stableContents.isEmpty()) 0 else virtualCount }
    )

    LaunchedEffect(stableContents, targetContentId) {
        if (stableContents.isNotEmpty() && targetContentId != null) {
            val index = stableContents.indexOfFirst { it.id == targetContentId }
            if (index != -1) {
                val currentActualIndex = pagerState.currentPage % stableContents.size
                val diff = index - currentActualIndex
                pagerState.scrollToPage(pagerState.currentPage + diff)
            }
        }
    }

    LaunchedEffect(selectedCategory) {
        if (stableContents.isNotEmpty() && targetContentId == null) {
            val startPage = virtualCount / 2
            val offset = startPage % stableContents.size
            pagerState.scrollToPage(startPage - offset)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (stableContents.isNotEmpty()) {
            val actualIndex = pagerState.currentPage % stableContents.size
            delay(2000)
            viewModel.markAsShown(stableContents[actualIndex])
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isPremium) {
                    Button(
                        onClick = onNavigateToPaywall,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SEJA PREMIUM", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Premium Ativado",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurações",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (stableContents.isEmpty()) {
                    EmptyState(selectedCategory)
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        pageSpacing = 0.dp,
                    ) { page ->
                        val actualIndex = page % stableContents.size
                        val content = stableContents[actualIndex]
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val picture = remember { Picture() }
                            var captureSize by remember { mutableStateOf(IntSize.Zero) }
                            
                            val handleShare = {
                                if (picture.width > 0 && picture.height > 0) {
                                    val bitmap = Bitmap.createBitmap(
                                        captureSize.width,
                                        captureSize.height,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = android.graphics.Canvas(bitmap)
                                    canvas.drawPicture(picture)
                                    ShareUtils.shareBitmap(context, bitmap)
                                } else {
                                    val shareText = if (content.reference != null) {
                                        "\"${content.text}\"\n\n${content.reference}"
                                    } else {
                                        content.text
                                    }
                                    ShareUtils.shareText(context, shareText)
                                }
                            }

                            if (shouldTriggerShare && targetContentId == content.id) {
                                LaunchedEffect(Unit) {
                                    delay(500)
                                    handleShare()
                                    viewModel.onShareHandled()
                                }
                            }

                            // Versão invisível para captura
                            Box(
                                modifier = Modifier
                                    .layout { measurable, constraints ->
                                        val customConstraints = constraints.copy(
                                            minWidth = 400.dp.roundToPx(),
                                            maxWidth = 400.dp.roundToPx(),
                                            minHeight = 0,
                                            maxHeight = Int.MAX_VALUE
                                        )
                                        val placeable = measurable.measure(customConstraints)
                                        captureSize = IntSize(placeable.width, placeable.height)
                                        layout(0, 0) {
                                            placeable.place(0, 0)
                                        }
                                    }
                                    .graphicsLayer { alpha = 0.01f }
                                    .drawWithCache {
                                        val width = size.width.toInt()
                                        val height = size.height.toInt()
                                        onDrawWithContent {
                                            val pictureCanvas = androidx.compose.ui.graphics.Canvas(
                                                picture.beginRecording(width, height)
                                            )
                                            draw(this, this.layoutDirection, pictureCanvas, size) {
                                                this@onDrawWithContent.drawContent()
                                            }
                                            picture.endRecording()
                                        }
                                    }
                            ) {
                                 ContentCard(
                                    content = content,
                                    isPremium = isPremium,
                                    isCapturing = true,
                                    onShare = {},
                                    onFavorite = {},
                                    onNavigateToPaywall = onNavigateToPaywall,
                                    modifier = Modifier.width(400.dp).wrapContentHeight()
                                )
                            }

                            ContentCard(
                                content = content,
                                isPremium = isPremium,
                                isCapturing = false,
                                onShare = handleShare,
                                onFavorite = { 
                                    if (isPremium) viewModel.toggleFavorite(content) 
                                    else onNavigateToPaywall()
                                },
                                onNavigateToPaywall = onNavigateToPaywall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            )
                        }
                    }
                }
            }
            
            // Ad Placeholder for Free Users
            if (!isPremium) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Anúncio (Remova com Premium)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        if (showTutorial) {
            TutorialModal(onDismiss = { showTutorial = false })
        }
    }
}

@Composable
fun TutorialModal(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val tutorialSteps = listOf(
        TutorialItem(
            "Bem-vindo!", 
            "Deslize para o lado para navegar entre os versículos.", 
            Icons.Default.Refresh
        ),
        TutorialItem(
            "Favoritos", 
            "Clique no coração para salvar os versículos que mais tocam seu coração.", 
            Icons.Default.Favorite
        ),
        TutorialItem(
            "Compartilhar", 
            "Gere uma imagem linda do versículo para compartilhar com quem você ama.", 
            Icons.Default.Share
        ),
        TutorialItem(
            "Configurações", 
            "Acesse o ícone de engrenagem para mudar seu nome, escolher temas e categorias.", 
            Icons.Default.Settings
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { 
                    if (step < tutorialSteps.size - 1) step++ else onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = tutorialSteps[step].icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = tutorialSteps[step].title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = tutorialSteps[step].description,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = if (step < tutorialSteps.size - 1) "Toque para continuar" else "Toque para começar",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class TutorialItem(val title: String, val description: String, val icon: ImageVector)

@Composable
fun EmptyState(category: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (category == "Favoritas") "Você ainda não tem versículos favoritos." else "Não há versículos para esta categoria.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ContentCard(
    content: ContentItemEntity, 
    isPremium: Boolean,
    isCapturing: Boolean,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appName = stringResource(id = R.string.app_name)
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .shadow(if (isCapturing) 0.dp else 4.dp, RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.surface
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text(
            text = "\"",
            fontSize = if (isCapturing) 80.sp else 64.sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-8).dp, y = (-24).dp)
        )
        
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 24.dp, bottom = if (isCapturing) 48.dp else 64.dp)
                .then(if (isCapturing) Modifier else Modifier.verticalScroll(scrollState)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = appName.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = if (isCapturing) 16.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "\"${content.text}\"",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = if (isCapturing) 30.sp else 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = if (isCapturing) 40.sp else 32.sp
            )
            
            content.reference?.let { ref ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = ref,
                    fontSize = if (isCapturing) 18.sp else 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        
        if (!isCapturing) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
                        .clickable { onFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (content.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (!isPremium) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f) else if (content.isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                    if (!isPremium) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp).align(Alignment.TopEnd), tint = Color.Gray)
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
                        .clickable { onShare() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = "Compartilhar",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Baixe na Play Store",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}
