package com.eclipse.launcher.di

import android.content.Context
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.data.repository.AppRepositoryImpl
import com.eclipse.launcher.domain.repository.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context
    ): AppRepository {
        return AppRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideHomePreferences(
        @ApplicationContext context: Context
    ): HomePreferences {
        return HomePreferences(context)
    }
}
