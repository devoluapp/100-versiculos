package blog.robertotavares.cemversiculos.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Paywall : Screen("paywall")
}
