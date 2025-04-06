package com.pizza.kkomdae.di

import com.pizza.kkomdae.data.repository.FinalRepositoryImpl
import com.pizza.kkomdae.data.repository.InspectRepositoryImpl
import com.pizza.kkomdae.data.repository.LoginRepositoryImpl
import com.pizza.kkomdae.data.repository.Step1RepositoryImpl
import com.pizza.kkomdae.data.repository.Step2RepositoryImpl
import com.pizza.kkomdae.data.repository.Step3RepositoryImpl
import com.pizza.kkomdae.data.repository.UserInfoRepositoryImpl
import com.pizza.kkomdae.domain.repository.FinalRepository
import com.pizza.kkomdae.domain.repository.InspectRepository
import com.pizza.kkomdae.domain.repository.LoginRepository
import com.pizza.kkomdae.domain.repository.Step1Repository
import com.pizza.kkomdae.domain.repository.Step2Repository
import com.pizza.kkomdae.domain.repository.Step3Repository
import com.pizza.kkomdae.domain.repository.UserRepository
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
    abstract fun bindLoginRepository(
        impl: LoginRepositoryImpl
    ): LoginRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserInfoRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindInspectRepository(
        impl: InspectRepositoryImpl
    ): InspectRepository

    @Binds
    @Singleton
    abstract fun bindStep1Repository(
        impl: Step1RepositoryImpl
    ): Step1Repository

    @Binds
    @Singleton
    abstract fun bindStep2Repository(
        impl: Step2RepositoryImpl
    ): Step2Repository

    @Binds
    @Singleton
    abstract fun bindStep3Repository(
        impl: Step3RepositoryImpl
    ): Step3Repository

    @Binds
    @Singleton
    abstract fun bindFinalRepository(
        impl: FinalRepositoryImpl
    ): FinalRepository
}