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

    fun getCategoryUnlockExpiration(category: String): Long
    fun saveCategoryUnlockExpiration(category: String, expirationMillis: Long)
    fun getCategoryUnlocksVersion(): StateFlow<Int>

    fun saveWidgetVerse(verse: WidgetVerse)
    fun getWidgetVerse(): WidgetVerse?

    fun getUsageDaysCount(): Int
    fun saveUsageDaysCount(count: Int)
    fun getLastUsageDate(): String?
    fun saveLastUsageDate(date: String)
    fun getLastReviewRequestTimestamp(): Long
    fun saveLastReviewRequestTimestamp(timestamp: Long)

    fun getStreakDays(): Int
    fun saveStreakDays(count: Int)
    fun getLastStreakDate(): String?
    fun saveLastStreakDate(date: String)

    fun getPremiumTeaserVariantIndex(): Int
    fun savePremiumTeaserVariantIndex(index: Int)
}

data class WidgetVerse(val text: String, val reference: String?, val contentId: Long = 0L)
