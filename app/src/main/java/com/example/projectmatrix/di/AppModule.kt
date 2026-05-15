package com.example.projectmatrix.di

import android.content.Context
import android.content.SharedPreferences
import com.example.projectmatrix.data.repository.MessengerRepositoryImpl
import com.example.projectmatrix.domain.repository.MessengerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMessengerRepository(repository: MessengerRepositoryImpl): MessengerRepository
}

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("min_auth", Context.MODE_PRIVATE)
}
