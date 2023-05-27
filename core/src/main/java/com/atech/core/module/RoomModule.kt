package com.atech.core.module

import android.content.Context
import androidx.room.Room
import com.atech.core.data.database.LinkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LinkDatabase =
        Room.databaseBuilder(
            context,
            LinkDatabase::class.java,
            LinkDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDao(database: LinkDatabase) = database.linkDao()
}