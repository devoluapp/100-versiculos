package blog.robertotavares.cemversiculos.presentation.onboarding

import android.Manifest
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import blog.robertotavares.cemversiculos.R
import blog.robertotavares.cemversiculos.presentation.home.HomeViewModel
import blog.robertotavares.cemversiculos.presentation.paywall.PaywallScreen
import blog.robertotavares.cemversiculos.presentation.settings.ThemePreview

enum class OnboardingStep {
    Welcome,
    Name,
    Themes,
    Categories,
    Permissions,
    Paywall,
    Tutorial
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.Welcome) }
    val userName by viewModel.userName.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isPremium by homeViewModel.isPremium.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        currentStep = if (isPremium) OnboardingStep.Tutorial else OnboardingStep.Paywall
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "OnboardingStep"
        ) { step ->
            when (step) {
                OnboardingStep.Welcome -> WelcomeStep { currentStep = OnboardingStep.Name }
                OnboardingStep.Name -> NameStep(
                    userName = userName,
                    onNameChange = viewModel::updateUserName,
                    onNext = { currentStep = OnboardingStep.Themes }
                )
                OnboardingStep.Themes -> ThemesStep(
                    selectedTheme = selectedTheme,
                    isPremium = isPremium,
                    onThemeSelect = {
                        viewModel.updateTheme(it)
                        homeViewModel.updateTheme(it)
                    },
                    onNext = { currentStep = OnboardingStep.Categories }
                )
                OnboardingStep.Categories -> CategoriesStep(
                    selectedCategories = selectedCategories,
                    isPremium = isPremium,
                    onToggle = viewModel::toggleCategory,
                    onNext = { currentStep = OnboardingStep.Permissions }
                )
                OnboardingStep.Permissions -> PermissionsStep(
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            currentStep = if (isPremium) OnboardingStep.Tutorial else OnboardingStep.Paywall
                        }
                    },
                    onSkip = { currentStep = if (isPremium) OnboardingStep.Tutorial else OnboardingStep.Paywall }
                )
                OnboardingStep.Paywall -> PaywallScreen(
                    onDismiss = { currentStep = OnboardingStep.Tutorial }
                )
                OnboardingStep.Tutorial -> TutorialStep {
                    viewModel.completeOnboarding()
                    selectedCategories.firstOrNull()?.let { homeViewModel.selectCategory(it) }
                    onFinish()
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                    ,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(120.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(id = blog.robertotavares.cemversiculos.R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.subtitle_app_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.action_start), modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun NameStep(userName: String, onNameChange: (String) -> Unit, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.title_name_step),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.label_your_name_optional)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.action_continue), modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun ThemesStep(selectedTheme: String, isPremium: Boolean, onThemeSelect: (String) -> Unit, onNext: () -> Unit) {
    val themes = listOf("Areia", "Natureza", "Oceano", "Crepúsculo")
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.title_theme_step),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        ThemePreview(themeName = selectedTheme)

        Spacer(modifier = Modifier.height(32.dp))
        
        themes.forEach { theme ->
            val isLocked = theme == "Crepúsculo" && !isPremium
            ThemeOptionCard(
                name = theme,
                isSelected = selectedTheme == theme,
                isLocked = isLocked,
                onClick = { if (!isLocked) onThemeSelect(theme) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.action_continue), modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun ThemeOptionCard(name: String, isSelected: Boolean, isLocked: Boolean, onClick: () -> Unit) {
    val color = when(name) {
        "Oceano" -> Color(0xFF4CACE6)
        "Crepúsculo" -> Color(0xFFA594F9)
        "Areia" -> Color(0xFFE6C04C)
        else -> Color(0xFF4CE699)
    }
    
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(24.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (isLocked) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            } else if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = color)
            }
        }
    }
}

@Composable
fun CategoriesStep(selectedCategories: Set<String>, isPremium: Boolean, onToggle: (String) -> Unit, onNext: () -> Unit) {
    val categories = listOf(
        "Gratidão" to Icons.Default.Face,
        "Fé" to Icons.Default.Star,
        "Luto" to Icons.Default.Favorite,
        "Medo" to Icons.Default.Warning,
        "Raiva" to Icons.Default.Info,
        "Oração" to Icons.Default.Place,
        "Perdão" to Icons.Default.CheckCircle,
        "Solidão" to Icons.Default.Person,
        "Tristeza" to Icons.Default.KeyboardArrowDown,
        "Ansiedade" to Icons.Default.Refresh,
        "Propósito" to Icons.AutoMirrored.Filled.Send
    )

    val freeCategories = listOf("Gratidão", "Fé", "Propósito")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.title_categories_step),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { (name, icon) ->
                val isLocked = !freeCategories.contains(name) && !isPremium
                CategorySelectCard(
                    name = name,
                    icon = icon,
                    isSelected = selectedCategories.contains(name),
                    isLocked = isLocked,
                    onClick = { if (!isLocked) onToggle(name) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = selectedCategories.isNotEmpty()
        ) {
            Text(stringResource(R.string.action_continue), modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun CategorySelectCard(name: String, icon: ImageVector, isSelected: Boolean, isLocked: Boolean, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
            .border(2.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f) else if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else if (isSelected) color else MaterialTheme.colorScheme.onSurface
                )
                if (isLocked) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun PermissionsStep(onRequest: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.section_reminders),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.desc_permission_notifications),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRequest,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.action_allow), modifier = Modifier.padding(8.dp))
        }
        TextButton(onClick = onSkip) { Text(stringResource(R.string.action_not_now)) }
    }
}

@Composable
fun TutorialStep(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.title_tutorial_ready),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.desc_tutorial_ready),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(R.string.action_start_now), modifier = Modifier.padding(8.dp))
        }
    }
}
