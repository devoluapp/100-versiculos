package blog.robertotavares.cemversiculos.core.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("eu_sou_prefs", Context.MODE_PRIVATE)

    fun saveSelectedCategory(category: String) {
        sharedPreferences.edit { putString("selected_category", category) }
    }

    fun getSelectedCategory(): String {
        return sharedPreferences.getString("selected_category", "Gratidão") ?: "Gratidão"
    }

    fun saveNotificationStartTime(time: String) {
        sharedPreferences.edit { putString("notification_start_time", time) }
    }

    fun getNotificationStartTime(): String {
        return sharedPreferences.getString("notification_start_time", "08:00") ?: "08:00"
    }

    fun saveNotificationEndTime(time: String) {
        sharedPreferences.edit { putString("notification_end_time", time) }
    }

    fun getNotificationEndTime(): String {
        return sharedPreferences.getString("notification_end_time", "22:00") ?: "22:00"
    }

    fun saveNotificationFrequency(frequency: Int) {
        sharedPreferences.edit { putInt("notification_frequency", frequency) }
    }

    fun getNotificationFrequency(): Int {
        return sharedPreferences.getInt("notification_frequency", 5)
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit { putString("user_name", name) }
    }

    fun getUserName(): String {
        return sharedPreferences.getString("user_name", "") ?: ""
    }

    fun saveOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit { putBoolean("onboarding_completed", completed) }
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean("onboarding_completed", false)
    }

    fun saveOnboardingCategories(categories: Set<String>) {
        sharedPreferences.edit { putStringSet("onboarding_categories", categories) }
    }

    fun getOnboardingCategories(): Set<String> {
        return sharedPreferences.getStringSet("onboarding_categories", emptySet()) ?: emptySet()
    }

    fun saveSelectedTheme(theme: String) {
        sharedPreferences.edit { putString("selected_theme", theme) }
    }

    fun getSelectedTheme(): String {
        return sharedPreferences.getString("selected_theme", "Areia") ?: "Areia"
    }

    fun saveIsPremium(isPremium: Boolean) {
        sharedPreferences.edit { putBoolean("is_premium", isPremium) }
    }

    fun isPremium(): Boolean {
        return sharedPreferences.getBoolean("is_premium", false)
    }

    fun saveCategoryUnlockExpiration(category: String, expirationMillis: Long) {
        sharedPreferences.edit { putLong("category_unlock_$category", expirationMillis) }
    }

    fun getCategoryUnlockExpiration(category: String): Long {
        return sharedPreferences.getLong("category_unlock_$category", 0L)
    }

    fun saveWidgetVerse(text: String, reference: String?, contentId: Long) {
        sharedPreferences.edit {
            putString("widget_verse_text", text)
            putString("widget_verse_reference", reference)
            putLong("widget_verse_content_id", contentId)
        }
    }

    fun getWidgetVerseText(): String? = sharedPreferences.getString("widget_verse_text", null)

    fun getWidgetVerseReference(): String? = sharedPreferences.getString("widget_verse_reference", null)

    fun getWidgetVerseContentId(): Long = sharedPreferences.getLong("widget_verse_content_id", 0L)

    fun saveUsageDaysCount(count: Int) {
        sharedPreferences.edit { putInt("usage_days_count", count) }
    }

    fun getUsageDaysCount(): Int {
        return sharedPreferences.getInt("usage_days_count", 0)
    }

    fun saveLastUsageDate(date: String) {
        sharedPreferences.edit { putString("last_usage_date", date) }
    }

    fun getLastUsageDate(): String? = sharedPreferences.getString("last_usage_date", null)

    fun saveLastReviewRequestTimestamp(timestamp: Long) {
        sharedPreferences.edit { putLong("last_review_request_timestamp", timestamp) }
    }

    fun getLastReviewRequestTimestamp(): Long {
        return sharedPreferences.getLong("last_review_request_timestamp", 0L)
    }
}
