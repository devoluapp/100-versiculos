package blog.robertotavares.cemversiculos.data.repository

import android.content.Context
import blog.robertotavares.cemversiculos.data.local.ContentDao
import blog.robertotavares.cemversiculos.data.local.ContentItemEntity
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    @ApplicationContext private val context: Context
) : ContentRepository {

    private val gson = Gson()

    override fun getContentsByCategory(category: String): Flow<List<ContentItemEntity>> {
        return contentDao.getContentsByCategory(category)
    }

    override fun getFavoriteContents(): Flow<List<ContentItemEntity>> {
        return contentDao.getFavoriteContents()
    }

    override suspend fun getNextContentToDisplay(category: String): ContentItemEntity? {
        if (contentDao.getContentCountByCategory(category) == 0) {
            seedInitialData(category)
        }
        return contentDao.getNextContentToDisplay(category)
    }

    override suspend fun getOrderedContents(category: String): List<ContentItemEntity> {
        if (contentDao.getContentCountByCategory(category) == 0) {
            seedInitialData(category)
        }
        return contentDao.getOrderedContentsByCategory(category)
    }

    override suspend fun toggleFavorite(content: ContentItemEntity) {
        contentDao.update(content.copy(isFavorite = !content.isFavorite))
    }

    override suspend fun markAsShown(content: ContentItemEntity) {
        contentDao.update(content.copy(
            lastShownTimestamp = System.currentTimeMillis(),
            shownCount = content.shownCount + 1
        ))
    }

    override suspend fun seedInitialData(category: String) {
        if (contentDao.getContentCountByCategory(category) > 0) return

        val fileName = when (category) {
            "Gratidão" -> "gratidao.json"
            "Fé" -> "fe.json"
            "Luto" -> "luto.json"
            "Medo" -> "medo.json"
            "Raiva" -> "raiva.json"
            "Oração" -> "oracao.json"
            "Perdão" -> "perdao.json"
            "Solidão" -> "solidao.json"
            "Tristeza" -> "tristeza.json"
            "Ansiedade" -> "ansiedade.json"
            "Propósito" -> "proposito.json"
            else -> null
        }

        fileName?.let {
            try {
                val jsonString = context.assets.open(it).bufferedReader().use { it.readText() }
                val categoryData: CategoryJson = gson.fromJson(jsonString, CategoryJson::class.java)
                
                categoryData.versiculos.forEach { v ->
                    contentDao.insert(ContentItemEntity(
                        text = v.texto,
                        reference = v.referencia,
                        authorOrCategory = category
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private data class CategoryJson(
        val categoria: String,
        val versiculos: List<VerseJson>
    )

    private data class VerseJson(
        val id: Int,
        val referencia: String,
        val texto: String
    )
}
