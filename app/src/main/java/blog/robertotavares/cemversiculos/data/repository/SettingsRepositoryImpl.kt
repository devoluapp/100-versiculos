package blog.robertotavares.cemversiculos.data.repository

import blog.robertotavares.cemversiculos.core.utils.PreferenceManager
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import blog.robertotavares.cemversiculos.domain.repository.WidgetVerse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferenceManager: PreferenceManager
) : SettingsRepository {

    private val _themeFlow = MutableStateFlow(preferenceManager.getSelectedTheme())
    override fun getThemeFlow(): StateFlow<String> = _themeFlow.asStateFlow()

    private val _premiumFlow = MutableStateFlow(preferenceManager.isPremium())
    override fun getPremiumFlow(): StateFlow<Boolean> = _premiumFlow.asStateFlow()

    override fun getSelectedCategory(): String {
        return preferenceManager.getSelectedCategory()
    }

    override fun saveSelectedCategory(category: String) {
        preferenceManager.saveSelectedCategory(category)
    }

    override fun getNotificationStartTime(): String {
        return preferenceManager.getNotificationStartTime()
    }

    override fun saveNotificationStartTime(time: String) {
        preferenceManager.saveNotificationStartTime(time)
    }

    override fun getNotificationEndTime(): String {
        return preferenceManager.getNotificationEndTime()
    }

    override fun saveNotificationEndTime(time: String) {
        preferenceManager.saveNotificationEndTime(time)
    }

    override fun getNotificationFrequency(): Int {
        return preferenceManager.getNotificationFrequency()
    }

    override fun saveNotificationFrequency(frequency: Int) {
        preferenceManager.saveNotificationFrequency(frequency)
    }

    override fun getUserName(): String = preferenceManager.getUserName()
    
    override fun saveUserName(name: String) = preferenceManager.saveUserName(name)

    override fun isOnboardingCompleted(): Boolean = preferenceManager.isOnboardingCompleted()

    override fun saveOnboardingCompleted(completed: Boolean) = preferenceManager.saveOnboardingCompleted(completed)

    override fun getOnboardingCategories(): Set<String> = preferenceManager.getOnboardingCategories()

    override fun saveOnboardingCategories(categories: Set<String>) = preferenceManager.saveOnboardingCategories(categories)

    override fun getSelectedTheme(): String = preferenceManager.getSelectedTheme()

    override fun saveSelectedTheme(theme: String) {
        preferenceManager.saveSelectedTheme(theme)
        _themeFlow.value = theme
    }

    override fun isPremium(): Boolean = preferenceManager.isPremium()

    override fun saveIsPremium(isPremium: Boolean) {
        preferenceManager.saveIsPremium(isPremium)
        _premiumFlow.value = isPremium
    }

    private val _categoryUnlocksVersion = MutableStateFlow(0)
    override fun getCategoryUnlocksVersion(): StateFlow<Int> = _categoryUnlocksVersion.asStateFlow()

    override fun getCategoryUnlockExpiration(category: String): Long =
        preferenceManager.getCategoryUnlockExpiration(category)

    override fun saveCategoryUnlockExpiration(category: String, expirationMillis: Long) {
        preferenceManager.saveCategoryUnlockExpiration(category, expirationMillis)
        _categoryUnlocksVersion.value++
    }

    override fun saveWidgetVerse(verse: WidgetVerse) {
        preferenceManager.saveWidgetVerse(verse.text, verse.reference, verse.contentId)
    }

    override fun getWidgetVerse(): WidgetVerse? {
        val text = preferenceManager.getWidgetVerseText() ?: return null
        return WidgetVerse(
            text,
            preferenceManager.getWidgetVerseReference(),
            preferenceManager.getWidgetVerseContentId()
        )
    }

    override fun getUsageDaysCount(): Int = preferenceManager.getUsageDaysCount()

    override fun saveUsageDaysCount(count: Int) = preferenceManager.saveUsageDaysCount(count)

    override fun getLastUsageDate(): String? = preferenceManager.getLastUsageDate()

    override fun saveLastUsageDate(date: String) = preferenceManager.saveLastUsageDate(date)

    override fun getLastReviewRequestTimestamp(): Long = preferenceManager.getLastReviewRequestTimestamp()

    override fun saveLastReviewRequestTimestamp(timestamp: Long) =
        preferenceManager.saveLastReviewRequestTimestamp(timestamp)

    override fun getStreakDays(): Int = preferenceManager.getStreakDays()

    override fun saveStreakDays(count: Int) = preferenceManager.saveStreakDays(count)

    override fun getLastStreakDate(): String? = preferenceManager.getLastStreakDate()

    override fun saveLastStreakDate(date: String) = preferenceManager.saveLastStreakDate(date)
}
