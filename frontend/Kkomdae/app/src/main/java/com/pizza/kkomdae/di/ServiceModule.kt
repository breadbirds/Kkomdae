package com.pizza.kkomdae.di

import com.pizza.kkomdae.data.source.remote.InspectService
import com.pizza.kkomdae.data.source.remote.LoginService
import com.pizza.kkomdae.data.source.remote.Step1Service
import com.pizza.kkomdae.data.source.remote.Step2Service
import com.pizza.kkomdae.data.source.remote.Step3Service
import com.pizza.kkomdae.data.source.remote.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun providesLoginService(retrofit: Retrofit) : LoginService = retrofit.create(LoginService::class.java)


    @Singleton
    @Provides
    fun providesUserService(retrofit: Retrofit) : UserService = retrofit.create(UserService::class.java)

    @Singleton
    @Provides
    fun providesInspectService(retrofit: Retrofit) : InspectService = retrofit.create(InspectService::class.java)

    @Singleton
    @Provides
    fun providesPhotoService(retrofit: Retrofit) : Step1Service = retrofit.create(Step1Service::class.java)

    @Singleton
    @Provides
    fun providesStep2Service(retrofit: Retrofit) : Step2Service = retrofit.create(Step2Service::class.java)

    @Singleton
    @Provides
    fun providesStep3Service(retrofit: Retrofit) : Step3Service = retrofit.create(Step3Service::class.java)
}