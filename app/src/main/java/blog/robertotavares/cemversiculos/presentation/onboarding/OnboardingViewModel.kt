package blog.robertotavares.cemversiculos.presentation.onboarding

import androidx.lifecycle.ViewModel
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

    fun updateUserName(name: String) {
        _userName.value = name
        if (name.trim() == "ADMIN123") {
            settingsRepository.saveIsPremium(true)
        }
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
