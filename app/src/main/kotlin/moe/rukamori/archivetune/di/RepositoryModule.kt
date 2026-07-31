package moe.rukamori.archivetune.di

import moe.rukamori.archivetune.data.repository.AccountRepository
import moe.rukamori.archivetune.data.repository.AccountRepositoryImpl
import moe.rukamori.archivetune.data.repository.SettingsRepository
import moe.rukamori.archivetune.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository
}