package com.atech.backup.module

import com.atech.backup.utils.DriveScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {


    @DriveScope
    @Singleton
    @Provides
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}