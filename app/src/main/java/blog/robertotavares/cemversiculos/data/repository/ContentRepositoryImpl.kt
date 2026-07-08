package blog.robertotavares.cemversiculos.data.repository

import android.content.Context
import android.util.Log
import blog.robertotavares.cemversiculos.data.local.ContentDao
import blog.robertotavares.cemversiculos.data.local.ContentItemEntity
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CUIDADO AO MEXER NA SEMEADURA (seedInitialData / seedMutex):
 *
 * Esta classe é um singleton (Hilt @Singleton), e a semeadura de uma categoria a partir do
 * JSON em assets/ é acionada por DOIS chamadores independentes que podem rodar ao mesmo tempo
 * na primeira abertura do app após a instalação:
 *   1. HomeViewModel, ao carregar a tela inicial (chama getContentsByCategory/seedInitialData).
 *   2. VersiculoWidgetWorker, um job do WorkManager agendado em App.onCreate() a cada início
 *      de processo. Como o agendamento usa ExistingPeriodicWorkPolicy.KEEP, ele só executa
 *      quase imediatamente na primeiríssima vez (antes de o trabalho periódico já existir) -
 *      exatamente a mesma janela em que o HomeViewModel também está semeando.
 *
 * Sem sincronização, os dois liam "contagem == 0" simultaneamente, ambos tentavam inserir, e
 * qualquer erro transitório (ex.: banco SQLite temporariamente ocupado pela outra escrita,
 * mais provável logo após uma instalação fresca com o aparelho ocupado com outras
 * inicializações) era engolido silenciosamente pelo catch, deixando a categoria vazia. Como a
 * Home lê a lista uma única vez (flow.first()), a tela ficava presa em "Não há versículos para
 * esta categoria" pelo resto da sessão, mesmo que o outro processo tivesse semeado com sucesso
 * segundos depois. Reabrir o app no dia seguinte "curava" o problema sozinho porque o
 * WorkManager não tenta rodar de novo tão cedo (próxima execução só em 24h), removendo o
 * segundo escritor concorrente.
 *
 * O Mutex abaixo serializa toda checagem-e-inserção por categoria, então NÃO remova nem
 * contorne essa sincronização, e não adicione um novo caminho de escrita em content_items que
 * não passe por dentro do lock.
 */
@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    @ApplicationContext private val context: Context
) : ContentRepository {

    private val gson = Gson()
    private val seedMutex = Mutex()

    override fun getContentsByCategory(category: String): Flow<List<ContentItemEntity>> {
        return contentDao.getContentsByCategory(category)
    }

    override fun getFavoriteContents(): Flow<List<ContentItemEntity>> {
        return contentDao.getFavoriteContents()
    }

    override suspend fun getNextContentToDisplay(category: String): ContentItemEntity? {
        seedInitialData(category)
        return contentDao.getNextContentToDisplay(category)
    }

    override suspend fun getOrderedContents(category: String): List<ContentItemEntity> {
        seedInitialData(category)
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

    // Idempotente e seguro para chamadas concorrentes: veja o comentário da classe sobre por
    // que essa serialização com seedMutex existe. A checagem de contagem é refeita dentro do
    // lock (double-checked) para que um segundo chamador que esperou o lock não insira de novo.
    override suspend fun seedInitialData(category: String) {
        seedMutex.withLock {
            if (contentDao.getContentCountByCategory(category) > 0) return@withLock

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
                    Log.e(TAG, "Falha ao semear versículos da categoria '$category' a partir de '$it'", e)
                }
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

    private companion object {
        const val TAG = "ContentRepository"
    }
}
