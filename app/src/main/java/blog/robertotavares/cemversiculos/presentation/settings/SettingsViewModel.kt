package blog.robertotavares.cemversiculos.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import blog.robertotavares.cemversiculos.core.notification.NotificationHelper
import blog.robertotavares.cemversiculos.core.utils.PermissionManager
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

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

    private val _canScheduleExactAlarms = MutableStateFlow(false)
    val canScheduleExactAlarms = _canScheduleExactAlarms.asStateFlow()

    fun checkPermissions(context: Context) {
        _hasNotificationPermission.value = PermissionManager.hasNotificationPermission(context)
        _canScheduleExactAlarms.value = PermissionManager.canScheduleExactAlarms(context)
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
        if (PermissionManager.hasNotificationPermission(context) && 
            PermissionManager.canScheduleExactAlarms(context)) {
            NotificationHelper.scheduleImmediate(context)
        }
    }
}
