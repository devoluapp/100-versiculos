package blog.robertotavares.cemversiculos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "content_items",
    indices = [Index(value = ["text", "authorOrCategory"], unique = true)]
)
data class ContentItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val reference: String? = null,
    val authorOrCategory: String, 
    val isUserCreated: Boolean = false,
    val isFavorite: Boolean = false,
    val categoryId: Long? = null,
    val shownCount: Int = 0,
    val lastShownTimestamp: Long? = null
)
