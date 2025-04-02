package com.pizza.kkomdae.di

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pizza.kkomdae.BuildConfig
import com.pizza.kkomdae.util.AddAuthInterceptor
import com.pizza.kkomdae.util.DateUtil
import com.pizza.kkomdae.util.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    const val SERVER_URL = BuildConfig.SERVER_URL

    @Provides
    @Singleton
    fun providesConverterFactory() : GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .setDateFormat(DateUtil.serverDateFormat.toPattern())
                .create()

        )
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(
        @ApplicationContext context: Context, // Hilt를 통해 Application Context 주입
        tokenAuthenticator: TokenAuthenticator,
        addAuthInterceptor: AddAuthInterceptor
    ) : OkHttpClient.Builder{
        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.e("Post", "log: message ${message}")
            }
        })
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder().apply {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            addInterceptor(logging)
            authenticator(tokenAuthenticator)
            addInterceptor(addAuthInterceptor)
        }
    }
    // TokenAuthenticator를 제공하는 추가 메서드
    @Provides
    @Singleton
    fun providesTokenAuthenticator(@ApplicationContext context: Context): TokenAuthenticator {
        return TokenAuthenticator(context)
    }

    @Provides
    @Singleton
    fun providesRetrofit(
        client: OkHttpClient.Builder,
        gsonConverterFactory: GsonConverterFactory
    ) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(SERVER_URL)
//            .addConverterFactory(gsonConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client.build())
            .build()
    }

    val gson : Gson = GsonBuilder()
        .setLenient()
        .setPrettyPrinting()  // JSON을 보기 좋게 출력
        .create()
}