package com.pizza.kkomdae.util

import android.content.Context
import com.pizza.kkomdae.data.source.local.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddAuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    // 인증 토큰이 필요없는 API 경로 목록
    private val excludedPaths = listOf(
        "/api/sso/refresh",
        "/api/sso/login"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()

        // 제외 경로 체크
        val isExcludedPath = excludedPaths.any { path -> requestUrl.contains(path) }

        // 제외 목록에 있으면 원래 요청 그대로 진행
        if (isExcludedPath) {
            return chain.proceed(originalRequest)
        }

        // 제외 목록에 없는 경우 토큰 추가
        val tokenManager = TokenManager(context)
        val accessToken = tokenManager.getAccessToken()

        val newRequest = if (accessToken?.isNotEmpty() == true) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}