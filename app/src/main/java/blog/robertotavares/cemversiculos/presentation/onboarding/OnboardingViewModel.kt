package blog.robertotavares.cemversiculos.presentation.onboarding

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
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories = _selectedCategories.asStateFlow()

    private val _selectedTheme = MutableStateFlow("Areia")
    val selectedTheme = _selectedTheme.asStateFlow()

    private val _notificationFrequency = MutableStateFlow(settingsRepository.getNotificationFrequency())
    val notificationFrequency = _notificationFrequency.asStateFlow()

    private val _notificationStartTime = MutableStateFlow(settingsRepository.getNotificationStartTime())
    val notificationStartTime = _notificationStartTime.asStateFlow()

    private val _notificationEndTime = MutableStateFlow(settingsRepository.getNotificationEndTime())
    val notificationEndTime = _notificationEndTime.asStateFlow()

    fun updateUserName(name: String) {
        _userName.value = name
    }

    fun updateTheme(theme: String) {
        _selectedTheme.value = theme
    }

    fun toggleCategory(category: String) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
    }

    fun updateNotificationFrequency(frequency: Int) {
        _notificationFrequency.value = frequency
    }

    fun updateNotificationStartTime(time: String) {
        _notificationStartTime.value = time
    }

    fun updateNotificationEndTime(time: String) {
        _notificationEndTime.value = time
    }

    /**
     * Chamado ao confirmar a etapa de configuração de lembretes no onboarding. Sem isto, a
     * primeira agenda de notificações só nascia quando o usuário abria Configurações e mexia
     * manualmente em algum campo (SettingsViewModel.trigger*), ou no próximo boot do aparelho
     * (BootReceiver) - ou seja, quem nunca visitasse Configurações simplesmente não recebia
     * notificação nenhuma.
     *
     * Usa scheduleImmediate (dispara em ~5s) em vez de scheduleNext (que respeitaria a janela
     * configurada e pode legitimamente cair só no dia seguinte, ex.: confirmar às 19h numa
     * janela 08h-22h calcula o próximo horário e resulta em ~12h de espera). Sem feedback rápido
     * aqui, o agendamento acontece silenciosamente e o usuário não tem como saber que funcionou -
     * mesmo problema que SettingsViewModel.triggerImmediateNotification já resolve ao mudar um
     * horário em Configurações. NotificationDisplayer.displayAndScheduleNext() já rechama
     * scheduleNext() ao final de toda exibição, então esse disparo imediato não substitui a
     * agenda real recorrente - só a antecipa em uma execução de teste.
     */
    fun confirmNotifications(context: Context) {
        settingsRepository.saveNotificationFrequency(_notificationFrequency.value)
        settingsRepository.saveNotificationStartTime(_notificationStartTime.value)
        settingsRepository.saveNotificationEndTime(_notificationEndTime.value)
        if (PermissionManager.hasNotificationPermission(context)) {
            NotificationHelper.scheduleImmediate(context)
        }
    }

    fun completeOnboarding() {
        settingsRepository.saveUserName(_userName.value)
        settingsRepository.saveOnboardingCategories(_selectedCategories.value)
        settingsRepository.saveSelectedTheme(_selectedTheme.value)
        if (_selectedCategories.value.isNotEmpty()) {
            settingsRepository.saveSelectedCategory(_selectedCategories.value.first())
        }
        settingsRepository.saveOnboardingCompleted(true)
    }

    fun isOnboardingCompleted() = settingsRepository.isOnboardingCompleted()
}
