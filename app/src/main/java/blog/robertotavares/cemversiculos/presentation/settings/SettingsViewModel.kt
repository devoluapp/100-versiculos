package blog.robertotavares.cemversiculos.presentation.settings

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import blog.robertotavares.cemversiculos.core.ads.AdManager
import blog.robertotavares.cemversiculos.core.notification.NotificationHelper
import blog.robertotavares.cemversiculos.core.utils.PermissionManager
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val adManager: AdManager
) : ViewModel() {

    val categoryUnlocksVersion = settingsRepository.getCategoryUnlocksVersion()

    private val _userName = MutableStateFlow(settingsRepository.getUserName())
    val userName = _userName.asStateFlow()

    private val _selectedCategory = MutableStateFlow(settingsRepository.getSelectedCategory())
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _notificationStartTime = MutableStateFlow(settingsRepository.getNotificationStartTime())
    val notificationStartTime = _notificationStartTime.asStateFlow()

    private val _notificationEndTime = MutableStateFlow(settingsRepository.getNotificationEndTime())
    val notificationEndTime = _notificationEndTime.asStateFlow()

    private val _notificationFrequency = MutableStateFlow(settingsRepository.getNotificationFrequency())
    val notificationFrequency = _notificationFrequency.asStateFlow()

    private val _selectedTheme = MutableStateFlow(settingsRepository.getSelectedTheme())
    val selectedTheme = _selectedTheme.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission = _hasNotificationPermission.asStateFlow()

    private val _hasBatteryOptimizationExemption = MutableStateFlow(false)
    val hasBatteryOptimizationExemption = _hasBatteryOptimizationExemption.asStateFlow()

    fun checkPermissions(context: Context) {
        _hasNotificationPermission.value = PermissionManager.hasNotificationPermission(context)
        _hasBatteryOptimizationExemption.value = PermissionManager.hasBatteryOptimizationExemption(context)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        PermissionManager.requestIgnoreBatteryOptimizations(context)
    }

    fun updateUserName(name: String) {
        _userName.value = name
        settingsRepository.saveUserName(name)
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
        settingsRepository.saveSelectedCategory(category)
    }

    fun updateStartTime(context: Context, time: String) {
        _notificationStartTime.value = time
        settingsRepository.saveNotificationStartTime(time)
        triggerImmediateNotification(context)
    }

    fun updateEndTime(context: Context, time: String) {
        _notificationEndTime.value = time
        settingsRepository.saveNotificationEndTime(time)
        triggerImmediateNotification(context)
    }

    fun updateFrequency(context: Context, frequency: Int) {
        _notificationFrequency.value = frequency
        settingsRepository.saveNotificationFrequency(frequency)
        triggerImmediateNotification(context)
    }

    fun updateTheme(theme: String) {
        _selectedTheme.value = theme
        settingsRepository.saveSelectedTheme(theme)
    }

    private fun triggerImmediateNotification(context: Context) {
        if (PermissionManager.hasNotificationPermission(context)) {
            NotificationHelper.scheduleImmediate(context)
        }
    }

    fun isCategoryTemporarilyUnlocked(category: String): Boolean {
        return settingsRepository.getCategoryUnlockExpiration(category) > System.currentTimeMillis()
    }

    fun unlockCategoryWithRewardedAd(activity: Activity, category: String, onResult: (Boolean) -> Unit) {
        adManager.showRewardedAd(
            activity,
            onRewardEarned = {
                settingsRepository.saveCategoryUnlockExpiration(
                    category,
                    System.currentTimeMillis() + CATEGORY_UNLOCK_DURATION_MILLIS
                )
                onResult(true)
            },
            onAdUnavailable = { onResult(false) }
        )
    }

    companion object {
        private const val CATEGORY_UNLOCK_DURATION_MILLIS = 24 * 60 * 60 * 1000L
    }
}
