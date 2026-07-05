package blog.robertotavares.cemversiculos.presentation.home

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import blog.robertotavares.cemversiculos.core.ads.AdManager
import blog.robertotavares.cemversiculos.data.local.ContentItemEntity
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val settingsRepository: SettingsRepository,
    private val adManager: AdManager
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

    val bannerAdUnitId: String get() = adManager.bannerAdUnitId

    private var verseSwipeCount = 0
    private val _interstitialEvent = Channel<Unit>(Channel.CONFLATED)
    val interstitialEvent = _interstitialEvent.receiveAsFlow()

    private var favoriteCount = 0
    private val _showFavoriteLimitDialog = MutableStateFlow(false)
    val showFavoriteLimitDialog = _showFavoriteLimitDialog.asStateFlow()

    init {
        viewModelScope.launch {
            selectedCategory.collectLatest { category ->
                loadContents(category)
            }
        }
        viewModelScope.launch {
            contentRepository.getFavoriteContents().collectLatest { favorites ->
                favoriteCount = favorites.size
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
        val isAddingFavorite = !content.isFavorite
        if (isAddingFavorite && !isPremium.value && favoriteCount >= FREE_FAVORITE_LIMIT) {
            _showFavoriteLimitDialog.value = true
            return
        }
        viewModelScope.launch {
            contentRepository.toggleFavorite(content)
            _contents.value = _contents.value.map {
                if (it.id == content.id) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }

    fun dismissFavoriteLimitDialog() {
        _showFavoriteLimitDialog.value = false
    }

    fun markAsShown(content: ContentItemEntity) {
        viewModelScope.launch {
            contentRepository.markAsShown(content)
        }
    }

    fun onVerseSwiped() {
        if (isPremium.value) return
        verseSwipeCount++
        if (verseSwipeCount >= INTERSTITIAL_SWIPE_THRESHOLD) {
            verseSwipeCount = 0
            _interstitialEvent.trySend(Unit)
        }
    }

    fun showInterstitial(activity: Activity) {
        adManager.showInterstitialIfAvailable(activity)
    }

    companion object {
        private const val INTERSTITIAL_SWIPE_THRESHOLD = 10
        private const val FREE_FAVORITE_LIMIT = 20
    }
}
