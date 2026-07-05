package blog.robertotavares.cemversiculos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ContentItemEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
}
