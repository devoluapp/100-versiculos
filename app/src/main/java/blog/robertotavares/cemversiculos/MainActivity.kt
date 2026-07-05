package blog.robertotavares.cemversiculos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import blog.robertotavares.cemversiculos.core.notification.NotificationReceiver
import blog.robertotavares.cemversiculos.presentation.home.HomeScreen
import blog.robertotavares.cemversiculos.presentation.home.HomeViewModel
import blog.robertotavares.cemversiculos.presentation.navigation.Screen
import blog.robertotavares.cemversiculos.presentation.onboarding.OnboardingScreen
import blog.robertotavares.cemversiculos.presentation.settings.SettingsScreen
import blog.robertotavares.cemversiculos.presentation.paywall.PaywallScreen
import blog.robertotavares.cemversiculos.presentation.theme.BaseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        intent?.let { handleIntent(it) }
        
        setContent {
            val currentTheme by homeViewModel.currentTheme.collectAsState()
            BaseTheme(themeName = currentTheme) {
                MainApp(homeViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val contentId = intent.getLongExtra(NotificationReceiver.EXTRA_CONTENT_ID, -1L)
        if (contentId != -1L) {
            val shouldShare = intent.getBooleanExtra(NotificationReceiver.EXTRA_SHOULD_SHARE, false)
            homeViewModel.setTargetContent(contentId, shouldShare)
        }
    }
}

@Composable
fun MainApp(homeViewModel: HomeViewModel) {
    val navController = rememberNavController()
    
    val startDestination = if (homeViewModel.isOnboardingCompleted()) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    })
                }
                composable(Screen.Home.route) { 
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) }
                    ) 
                }
                composable(Screen.Settings.route) { 
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) }
                    ) 
                }
                composable(Screen.Paywall.route) {
                    PaywallScreen(
                        onDismiss = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
