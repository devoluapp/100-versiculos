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
}
