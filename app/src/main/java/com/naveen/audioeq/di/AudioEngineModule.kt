package com.naveen.audioeq.di

import com.naveen.audioeq.core.EffectCompatibilityChecker
import com.naveen.audioeq.core.GlobalAudioSessionEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioEngineModule {

    @Provides
    @Singleton
    fun provideGlobalAudioSessionEngine(): GlobalAudioSessionEngine = GlobalAudioSessionEngine()

    @Provides
    @Singleton
    fun provideCompatibilityChecker(): EffectCompatibilityChecker = EffectCompatibilityChecker()
}
