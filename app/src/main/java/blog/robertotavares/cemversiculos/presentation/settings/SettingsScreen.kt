package blog.robertotavares.cemversiculos.presentation.settings

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import blog.robertotavares.cemversiculos.R
import blog.robertotavares.cemversiculos.core.utils.PermissionManager
import blog.robertotavares.cemversiculos.core.widget.VersiculoWidgetReceiver
import blog.robertotavares.cemversiculos.presentation.home.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(), 
    onBack: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val startTime by viewModel.notificationStartTime.collectAsState()
    val endTime by viewModel.notificationEndTime.collectAsState()
    val frequency by viewModel.notificationFrequency.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsState()
    val canScheduleExactAlarms by viewModel.canScheduleExactAlarms.collectAsState()
    val isPremium by homeViewModel.isPremium.collectAsState()
    val categoryUnlocksVersion by viewModel.categoryUnlocksVersion.collectAsState()

    val context = LocalContext.current
    var showPermissionModal by remember { mutableStateOf(false) }
    var categoryToUnlock by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.checkPermissions(context)
        showPermissionModal = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    if (!isPremium) {
                        IconButton(onClick = onNavigateToPaywall) {
                            Icon(Icons.Default.Star, contentDescription = stringResource(R.string.cd_premium), tint = Color(0xFFFFD700))
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Seção: Perfil
            SettingsSection(title = stringResource(R.string.section_profile), icon = Icons.Default.Person) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = viewModel::updateUserName,
                    label = { Text(stringResource(R.string.label_your_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Seção: Temas com Preview
            SettingsSection(title = stringResource(R.string.section_theme), icon = Icons.Default.Edit) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ThemePreview(themeName = selectedTheme)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf("Natureza", "Oceano", "Crepúsculo", "Areia")
                        themes.forEach { theme ->
                            val isExclusive = theme == "Crepúsculo"
                            ThemeChip(
                                name = theme,
                                isSelected = selectedTheme == theme,
                                isLocked = isExclusive && !isPremium,
                                modifier = Modifier.weight(1f),
                                onClick = { 
                                    if (isExclusive && !isPremium) {
                                        onNavigateToPaywall()
                                    } else {
                                        viewModel.updateTheme(theme)
                                        homeViewModel.updateTheme(theme)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Seção: Categoria
            SettingsSection(title = stringResource(R.string.section_category), icon = Icons.AutoMirrored.Filled.List) {
                val allCategories = listOf(
                    "Gratidão", "Fé", "Luto", "Medo", "Raiva", 
                    "Oração", "Perdão", "Solidão", "Tristeza", 
                    "Ansiedade", "Propósito", "Favoritas"
                )
                
                val freeCategories = listOf("Gratidão", "Fé", "Propósito", "Favoritas")

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allCategories.forEach { category ->
                        val isTemporarilyUnlocked = remember(categoryUnlocksVersion, category) {
                            viewModel.isCategoryTemporarilyUnlocked(category)
                        }
                        val isLocked = !freeCategories.contains(category) && !isPremium && !isTemporarilyUnlocked
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                if (isLocked) {
                                    categoryToUnlock = category
                                } else {
                                    viewModel.updateCategory(category)
                                    homeViewModel.selectCategory(category)
                                }
                            },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(category)
                                    if (isLocked) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Seção: Notificações
            SettingsSection(title = stringResource(R.string.section_reminders), icon = Icons.Default.Notifications) {
                if (!hasNotificationPermission || !canScheduleExactAlarms) {
                    Button(
                        onClick = { showPermissionModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_configure_permissions))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(stringResource(R.string.label_times_per_day), style = MaterialTheme.typography.bodyLarge)
                                if (!isPremium) {
                                    Text(stringResource(R.string.label_free_max_5), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (frequency > 1) viewModel.updateFrequency(context, frequency - 1) }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.cd_decrease))
                                }
                                Text(
                                    frequency.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = {
                                    if (frequency < 5 || isPremium) {
                                        if (frequency < 20) viewModel.updateFrequency(context, frequency + 1)
                                    } else {
                                        onNavigateToPaywall()
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (frequency >= 5 && !isPremium) Icons.Default.Lock else Icons.Default.KeyboardArrowUp,
                                        contentDescription = stringResource(R.string.cd_increase),
                                        modifier = Modifier.size(if (frequency >= 5 && !isPremium) 18.dp else 24.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TimeSettingCard(
                                label = stringResource(R.string.label_start_time),
                                time = startTime,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val parts = startTime.split(":")
                                    TimePickerDialog(context, { _, h, m ->
                                        viewModel.updateStartTime(context, String.format(Locale.getDefault(), "%02d:%02d", h, m))
                                    }, parts[0].toInt(), parts[1].toInt(), true).show()
                                }
                            )
                            TimeSettingCard(
                                label = stringResource(R.string.label_end_time),
                                time = endTime,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val parts = endTime.split(":")
                                    TimePickerDialog(context, { _, h, m ->
                                        viewModel.updateEndTime(context, String.format(Locale.getDefault(), "%02d:%02d", h, m))
                                    }, parts[0].toInt(), parts[1].toInt(), true).show()
                                }
                            )
                        }
                    }
                }
            }

            // Seção: Widget
            SettingsSection(title = stringResource(R.string.section_widget), icon = Icons.Default.Home) {
                val appName = stringResource(R.string.app_name)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.desc_add_widget),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Button(
                        onClick = {
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val provider = ComponentName(context, VersiculoWidgetReceiver::class.java)
                            val canPin = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                appWidgetManager.isRequestPinAppWidgetSupported
                            if (canPin) {
                                appWidgetManager.requestPinAppWidget(provider, null, null)
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.widget_manual_instructions, appName),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_add_widget))
                    }
                    Text(
                        stringResource(R.string.widget_manual_instructions, appName),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showPermissionModal) {
        PermissionDialog(
            onDismiss = { showPermissionModal = false },
            onGrantNotifications = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    PermissionManager.openAppSettings(context)
                }
            },
            onGrantAlarms = {
                PermissionManager.openExactAlarmSettings(context)
            },
            hasNotificationPermission = hasNotificationPermission,
            canScheduleExactAlarms = canScheduleExactAlarms
        )
    }

    categoryToUnlock?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToUnlock = null },
            title = { Text(stringResource(R.string.title_unlock_category)) },
            text = { Text(stringResource(R.string.desc_unlock_category)) },
            confirmButton = {
                Button(onClick = {
                    categoryToUnlock = null
                    onNavigateToPaywall()
                }) {
                    Text(stringResource(R.string.action_premium_option))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    categoryToUnlock = null
                    (context as? Activity)?.let { activity ->
                        viewModel.unlockCategoryWithRewardedAd(activity, category) { success ->
                            if (success) {
                                viewModel.updateCategory(category)
                                homeViewModel.selectCategory(category)
                            } else {
                                Toast.makeText(context, context.getString(R.string.error_ad_unavailable), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text(stringResource(R.string.action_watch_ad_24h))
                }
            }
        )
    }
}

@Composable
fun PermissionStatusItem(title: String, isGranted: Boolean, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.Medium)
        }
        if (!isGranted) {
            TextButton(onClick = onAction) {
                Text(stringResource(R.string.action_allow))
            }
        } else {
            Text(stringResource(R.string.label_active), color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onGrantNotifications: () -> Unit,
    onGrantAlarms: () -> Unit,
    hasNotificationPermission: Boolean,
    canScheduleExactAlarms: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_permissions_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val appName = stringResource(id = R.string.app_name)
                Text(stringResource(R.string.dialog_permissions_message, appName))

                if (!hasNotificationPermission) {
                    Text(stringResource(R.string.dialog_permission_notifications))
                }
                if (!canScheduleExactAlarms) {
                    Text(stringResource(R.string.dialog_permission_alarms))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (!hasNotificationPermission) onGrantNotifications()
                else if (!canScheduleExactAlarms) onGrantAlarms()
                else onDismiss()
            }) {
                Text(stringResource(R.string.action_configure))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ThemePreview(themeName: String) {
    val themeColor = when(themeName) {
        "Oceano" -> Color(0xFF4CACE6)
        "Crepúsculo" -> Color(0xFFA594F9)
        "Areia" -> Color(0xFFE6C04C)
        else -> Color(0xFF4CE699)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            themeColor.copy(alpha = 0.2f),
                            themeColor.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = blog.robertotavares.cemversiculos.R.string.app_name).uppercase(),
                    color = themeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "\"Exemplo de Versículo\"",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@Composable
fun ThemeChip(name: String, isSelected: Boolean, isLocked: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = when(name) {
        "Oceano" -> Color(0xFF4CACE6)
        "Crepúsculo" -> Color(0xFFA594F9)
        "Areia" -> Color(0xFFE6C04C)
        else -> Color(0xFF4CE699)
    }
    
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            if (isLocked) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun TimeSettingCard(label: String, time: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
