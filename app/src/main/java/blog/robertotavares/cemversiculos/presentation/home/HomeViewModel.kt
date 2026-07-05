package blog.robertotavares.cemversiculos.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import blog.robertotavares.cemversiculos.data.local.ContentItemEntity
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(settingsRepository.getSelectedCategory())
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _contents = MutableStateFlow<List<ContentItemEntity>>(emptyList())
    val contents = _contents.asStateFlow()

    private val _targetContentId = MutableStateFlow<Long?>(null)
    val targetContentId = _targetContentId.asStateFlow()

    private val _shouldTriggerShare = MutableStateFlow(false)
    val shouldTriggerShare = _shouldTriggerShare.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val currentTheme = settingsRepository.getThemeFlow()
    val isPremium = settingsRepository.getPremiumFlow()

    init {
        viewModelScope.launch {
            selectedCategory.collectLatest { category ->
                loadContents(category)
            }
        }
    }

    fun isOnboardingCompleted() = settingsRepository.isOnboardingCompleted()

    fun setTargetContent(id: Long, shouldShare: Boolean = false) {
        _targetContentId.value = id
        _shouldTriggerShare.value = shouldShare
    }

    fun onShareHandled() {
        _shouldTriggerShare.value = false
    }

    fun updateTheme(theme: String) {
        settingsRepository.saveSelectedTheme(theme)
    }

    fun loadContents(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val flow = if (category == "Favoritas") {
                contentRepository.getFavoriteContents()
            } else {
                contentRepository.getContentsByCategory(category)
            }
            
            val list = flow.first()
            
            if (list.isEmpty() && category != "Favoritas") {
                contentRepository.seedInitialData(category)
                val newList = contentRepository.getContentsByCategory(category).first()
                updateContentsList(newList)
            } else {
                updateContentsList(list)
            }
            _isLoading.value = false
        }
    }

    private fun updateContentsList(list: List<ContentItemEntity>) {
        val uniqueList = list.distinctBy { it.text }
        
        val sortedList = uniqueList.sortedWith(
            compareByDescending<ContentItemEntity> { it.lastShownTimestamp == null }
                .thenBy { it.lastShownTimestamp ?: 0L }
        )
        _contents.value = sortedList
    }

    fun selectCategory(category: String) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            settingsRepository.saveSelectedCategory(category)
        }
    }
    
    fun toggleFavorite(content: ContentItemEntity) {
        viewModelScope.launch {
            contentRepository.toggleFavorite(content)
            _contents.value = _contents.value.map { 
                if (it.id == content.id) it.copy(isFavorite = !it.isFavorite) else it 
            }
        }
    }

    fun markAsShown(content: ContentItemEntity) {
        viewModelScope.launch {
            contentRepository.markAsShown(content)
        }
    }
}
