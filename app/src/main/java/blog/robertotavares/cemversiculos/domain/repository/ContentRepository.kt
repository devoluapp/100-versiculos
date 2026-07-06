package blog.robertotavares.cemversiculos.domain.repository

import blog.robertotavares.cemversiculos.data.local.ContentItemEntity
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getContentsByCategory(category: String): Flow<List<ContentItemEntity>>
    fun getFavoriteContents(): Flow<List<ContentItemEntity>>
    suspend fun getNextContentToDisplay(category: String): ContentItemEntity?
    suspend fun getOrderedContents(category: String): List<ContentItemEntity>
    suspend fun toggleFavorite(content: ContentItemEntity)
    suspend fun markAsShown(content: ContentItemEntity)
    suspend fun seedInitialData(category: String)
}
