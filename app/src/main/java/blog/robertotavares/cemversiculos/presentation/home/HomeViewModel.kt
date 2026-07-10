package blog.robertotavares.cemversiculos.presentation.home

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import blog.robertotavares.cemversiculos.core.ads.AdManager
import blog.robertotavares.cemversiculos.core.analytics.AnalyticsHelper
import blog.robertotavares.cemversiculos.core.review.InAppReviewManager
import blog.robertotavares.cemversiculos.data.local.ContentItemEntity
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import blog.robertotavares.cemversiculos.presentation.paywall.PREMIUM_TEASER_VARIANT_COUNT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val settingsRepository: SettingsRepository,
    private val adManager: AdManager,
    private val analyticsHelper: AnalyticsHelper,
    private val inAppReviewManager: InAppReviewManager
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
    val streakDays: Int = settingsRepository.getStreakDays()

    /**
     * O badge de streak só deve aparecer quando representa uma conquista real
     * (uso em dias consecutivos), não no dia 1 de uma instalação nova, onde
     * [streakDays] sempre vale 1.
     */
    val showStreakBadge: Boolean get() = streakDays >= MIN_STREAK_DAYS_TO_DISPLAY

    val bannerAdUnitId: String get() = adManager.bannerAdUnitId

    private var verseSwipeCount = 0
    private val _premiumTeaserEvent = Channel<Int>(Channel.CONFLATED)
    val premiumTeaserEvent = _premiumTeaserEvent.receiveAsFlow()

    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount = _favoriteCount.asStateFlow()
    private val _showFavoriteLimitDialog = MutableStateFlow(false)
    val showFavoriteLimitDialog = _showFavoriteLimitDialog.asStateFlow()

    init {
        viewModelScope.launch {
            // flatMapLatest é essencial aqui: a versão antiga chamava loadContents(category)
            // de dentro de um collectLatest, mas loadContents lançava sua PRÓPRIA coroutine via
            // viewModelScope.launch. Como collectLatest só cancela o trabalho que roda dentro do
            // seu próprio bloco, aquele launch interno escapava da cancelação - a troca de
            // categoria nunca cancelava a carga anterior. Se a categoria anterior ainda estivesse
            // semeando (assets grandes, ou concorrendo com o Mutex de seedInitialData com o
            // VersiculoWidgetWorker) e terminasse DEPOIS da nova seleção, ela sobrescrevia
            // _contents com os dados (ou lista vazia) da categoria errada - por exemplo, o
            // usuário favoritar um verso, trocar rapidamente de categoria até "Favoritas" e ver
            // "sem versículos" porque uma carga anterior, ainda em voo, venceu a corrida e
            // sobrescreveu a lista de favoritos por último. flatMapLatest cancela de verdade o
            // Flow anterior (contentFlowForCategory) assim que uma nova categoria é selecionada.
            selectedCategory
                .flatMapLatest { category -> contentFlowForCategory(category) }
                .collectLatest { list -> updateContentsList(list) }
        }
        viewModelScope.launch {
            contentRepository.getFavoriteContents().collectLatest { favorites ->
                _favoriteCount.value = favorites.size
            }
        }
    }

    private fun contentFlowForCategory(category: String): Flow<List<ContentItemEntity>> {
        // Snapshot único de propósito (não observamos o Flow do Room continuamente): a lista é
        // reordenada por lastShownTimestamp em updateContentsList, e markAsShown grava esse
        // timestamp ~2s depois de cada verso abrir. Se observássemos o Flow ao vivo, essa
        // escrita reordenaria a lista debaixo do usuário enquanto ele ainda estivesse folheando
        // o HorizontalPager. O flatMapLatest ao redor desta função já garante que uma nova
        // seleção de categoria cancela este snapshot se ele ainda estiver em andamento.
        return flow {
            val list = if (category == "Favoritas") {
                contentRepository.getFavoriteContents().first()
            } else {
                // seedInitialData é idempotente e serializada por Mutex em
                // ContentRepositoryImpl (chamar sempre, mesmo com a categoria já semeada, é
                // barato: só faz um SELECT COUNT antes de devolver). Veja o comentário da classe
                // para o histórico do bug de semeadura concorrente que isso evita.
                contentRepository.seedInitialData(category)
                contentRepository.getContentsByCategory(category).first()
            }
            emit(list)
        }
            .onStart { _isLoading.value = true }
            .onEach { _isLoading.value = false }
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
            analyticsHelper.logCategoriaSelecionada(category)
        }
    }

    fun toggleFavorite(content: ContentItemEntity) {
        val isAddingFavorite = !content.isFavorite
        if (isAddingFavorite && !isPremium.value && _favoriteCount.value >= FREE_FAVORITE_LIMIT) {
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

    fun logVerseShared(content: ContentItemEntity) {
        analyticsHelper.logVersiculoCompartilhado(content.reference)
    }

    fun maybeRequestReview(activity: Activity) {
        inAppReviewManager.maybeRequestReview(activity, _favoriteCount.value)
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
        if (verseSwipeCount >= PREMIUM_TEASER_SWIPE_THRESHOLD) {
            verseSwipeCount = 0
            val variantIndex = settingsRepository.getPremiumTeaserVariantIndex()
            settingsRepository.savePremiumTeaserVariantIndex((variantIndex + 1) % PREMIUM_TEASER_VARIANT_COUNT)
            analyticsHelper.logPremiumTeaserExibido(variantIndex)
            _premiumTeaserEvent.trySend(variantIndex)
        }
    }

    companion object {
        private const val PREMIUM_TEASER_SWIPE_THRESHOLD = 10
        private const val FREE_FAVORITE_LIMIT = 20
        private const val MIN_STREAK_DAYS_TO_DISPLAY = 3
    }
}
