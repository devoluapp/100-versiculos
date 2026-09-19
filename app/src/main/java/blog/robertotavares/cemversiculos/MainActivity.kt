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
import blog.robertotavares.cemversiculos.core.ads.AdManager
import blog.robertotavares.cemversiculos.core.analytics.AnalyticsHelper
import blog.robertotavares.cemversiculos.core.notification.NotificationReceiver
import blog.robertotavares.cemversiculos.presentation.home.HomeScreen
import blog.robertotavares.cemversiculos.presentation.home.HomeViewModel
import blog.robertotavares.cemversiculos.presentation.navigation.Screen
import blog.robertotavares.cemversiculos.presentation.onboarding.OnboardingScreen
import blog.robertotavares.cemversiculos.presentation.settings.SettingsScreen
import blog.robertotavares.cemversiculos.presentation.paywall.PaywallScreen
import blog.robertotavares.cemversiculos.presentation.theme.BaseTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var analyticsHelper: AnalyticsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let { handleIntent(it) }

        setContent {
            val currentTheme by homeViewModel.currentTheme.collectAsState()
            BaseTheme(themeName = currentTheme) {
                MainApp(homeViewModel)
            }
        }

        // Chamado depois de setContent, nunca antes: o SDK de consentimento (UMP) pode tentar
        // exibir o formulário de consentimento anexado à janela da Activity assim que
        // requestConsentInfoUpdate retorna, e se isso acontece antes do container de conteúdo
        // existir, o Android lança "Window couldn't find content container view" e fecha a
        // Activity. Esse era o crash mais frequente do app no Crashlytics (aberto desde a
        // v1.6, MainActivity.onCreate) - a própria amostra oficial do Google para o UMP SDK
        // também chama setContentView antes de pedir consentimento, pelo mesmo motivo.
        adManager.requestConsentAndInitialize(this)
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
            // Único ponto em que sabemos que a abertura do app veio de um toque em notificação
            // (corpo ou botão Compartilhar) - EXTRA_CONTENT_ID só é setado pelos PendingIntents
            // montados em NotificationDisplayer. Sem isto não havia como medir se as
            // notificações realmente trazem o usuário de volta.
            analyticsHelper.logNotificacaoAberta()
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
                    OnboardingScreen(
                        homeViewModel = homeViewModel,
                        onFinish = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
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
                        homeViewModel = homeViewModel,
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
