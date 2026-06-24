package com.eclipse.launcher.di

import android.content.Context
import com.eclipse.launcher.data.datastore.HomePreferences
import com.eclipse.launcher.data.registry.WidgetRegistryImpl
import com.eclipse.launcher.data.repository.DynamicThemeRepositoryImpl
import com.eclipse.launcher.data.repository.InstalledAppsManagerImpl
import com.eclipse.launcher.data.repository.WallpaperRepositoryImpl
import com.eclipse.launcher.domain.registry.WidgetRegistry
import com.eclipse.launcher.domain.repository.DynamicThemeRepository
import com.eclipse.launcher.domain.repository.InstalledAppsManager
import com.eclipse.launcher.domain.repository.WallpaperRepository
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
    fun provideInstalledAppsManager(
        @ApplicationContext context: Context
    ): InstalledAppsManager {
        return InstalledAppsManagerImpl(context)
    }

    @Provides
    @Singleton
    fun provideHomePreferences(
        @ApplicationContext context: Context
    ): HomePreferences {
        return HomePreferences(context)
    }

    @Provides
    @Singleton
    fun provideWallpaperRepository(
        @ApplicationContext context: Context
    ): WallpaperRepository {
        return WallpaperRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDynamicThemeRepository(
        @ApplicationContext context: Context,
        homePreferences: HomePreferences
    ): DynamicThemeRepository {
        return DynamicThemeRepositoryImpl(context, homePreferences)
    }

    @Provides
    @Singleton
    fun provideWidgetRegistry(): WidgetRegistry {
        return WidgetRegistryImpl()
    }
}
