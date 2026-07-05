package blog.robertotavares.cemversiculos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM content_items WHERE isUserCreated = :isUserCreated")
    fun getAllContents(isUserCreated: Boolean = false): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE authorOrCategory = :category")
    fun getContentsByCategory(category: String): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE isFavorite = 1")
    fun getFavoriteContents(): Flow<List<ContentItemEntity>>

    /**
     * Busca a próxima conteúdo baseada nos critérios:
     * 1. Prioriza as que nunca foram exibidas (lastShownTimestamp IS NULL).
     * 2. Entre as nunca exibidas, escolhe aleatoriamente.
     * 3. Se todas já foram exibidas, escolhe a que foi exibida há mais tempo (menor lastShownTimestamp).
     */
    @Query("""
        SELECT * FROM content_items 
        WHERE authorOrCategory = :category 
        ORDER BY 
            lastShownTimestamp IS NOT NULL ASC,
            CASE WHEN lastShownTimestamp IS NULL THEN RANDOM() ELSE lastShownTimestamp END ASC
        LIMIT 1
    """)
    suspend fun getNextContentToDisplay(category: String): ContentItemEntity?

    @Query("SELECT COUNT(*) FROM content_items WHERE authorOrCategory = :category")
    suspend fun getContentCountByCategory(category: String): Int

    @Query("SELECT * FROM content_items ORDER BY shownCount ASC, RANDOM() LIMIT 1")
    suspend fun getNextRandomContent(): ContentItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ContentItemEntity)

    @Update
    suspend fun update(item: ContentItemEntity)

    @Delete
    suspend fun delete(item: ContentItemEntity)
}
