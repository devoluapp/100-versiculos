package blog.robertotavares.cemversiculos.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    fun getSelectedCategory(): String
    fun saveSelectedCategory(category: String)
    fun getNotificationStartTime(): String
    fun saveNotificationStartTime(time: String)
    fun getNotificationEndTime(): String
    fun saveNotificationEndTime(time: String)
    fun getNotificationFrequency(): Int
    fun saveNotificationFrequency(frequency: Int)
    
    fun getUserName(): String
    fun saveUserName(name: String)
    fun isOnboardingCompleted(): Boolean
    fun saveOnboardingCompleted(completed: Boolean)
    fun getOnboardingCategories(): Set<String>
    fun saveOnboardingCategories(categories: Set<String>)

    fun getSelectedTheme(): String
    fun saveSelectedTheme(theme: String)
    fun getThemeFlow(): StateFlow<String>

    fun isPremium(): Boolean
    fun saveIsPremium(isPremium: Boolean)
    fun getPremiumFlow(): StateFlow<Boolean>
}
