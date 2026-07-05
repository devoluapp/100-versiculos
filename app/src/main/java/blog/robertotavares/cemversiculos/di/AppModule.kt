package blog.robertotavares.cemversiculos.di

import android.content.Context
import androidx.room.Room
import blog.robertotavares.cemversiculos.data.local.AppDatabase
import blog.robertotavares.cemversiculos.data.local.ContentDao
import blog.robertotavares.cemversiculos.core.utils.PreferenceManager
import blog.robertotavares.cemversiculos.data.repository.ContentRepositoryImpl
import blog.robertotavares.cemversiculos.data.repository.SettingsRepositoryImpl
import blog.robertotavares.cemversiculos.domain.repository.ContentRepository
import blog.robertotavares.cemversiculos.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "eusou_app_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideContentDao(db: AppDatabase): ContentDao {
        return db.contentDao()
    }

    @Provides
    @Singleton
    fun provideContentRepository(
        contentDao: ContentDao,
        @ApplicationContext context: Context
    ): ContentRepository {
        return ContentRepositoryImpl(contentDao, context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(preferenceManager: PreferenceManager): SettingsRepository {
        return SettingsRepositoryImpl(preferenceManager)
    }
}
