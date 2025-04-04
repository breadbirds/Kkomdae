package com.pizza.kkomdae.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.pizza.kkomdae.LoginActivity
import com.pizza.kkomdae.data.model.dto.RefreshTokenRequest
import com.pizza.kkomdae.data.source.local.SecureTokenManager
import com.pizza.kkomdae.data.source.local.TokenManager
import com.pizza.kkomdae.util.RetrofitUtil.Companion.loginService
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.atomic.AtomicInteger
class TokenAuthenticator(private val context: Context) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // ✅ 이전 요청이 있다면 이미 재시도한 거다 → 중단
        if (responseCount(response) >= 2) {
            Log.d("Authenticator", "요청당 최대 재시도 1회 초과, 중단.")
            return null
        }

        if (!isTokenExpired(response)) return null

        Log.d("Authenticator", "401 발생 - 토큰 재발급 시도")

        val tokenManager = TokenManager(context)
        val secureTokenManager = SecureTokenManager(context)
        val refreshToken = secureTokenManager.getRefreshToken()

        val reissueResponse = try {
            loginService.postRefreshToken(RefreshTokenRequest(refreshToken ?: ""))
                .execute()
        } catch (e: Exception) {
            Log.e("Authenticator", "토큰 재발급 중 오류", e)
            return null
        }

        return if (reissueResponse.isSuccessful) {
            val newJwtToken = reissueResponse.body()?.jwt
            val newRefreshToken = reissueResponse.body()?.refreshToken

            tokenManager.saveAccessToken(newJwtToken ?: "")
            secureTokenManager.saveRefreshToken(newRefreshToken ?: "")

            Log.d("Authenticator", "토큰 재발급 성공")

            response.request.newBuilder()
                .header("Authorization", "Bearer $newJwtToken")
                .build()
        } else {
            Log.d("Authenticator", "토큰 재발급 실패 → 로그아웃 처리")
            forceLogout()
            null
        }
    }

    private fun isTokenExpired(response: Response): Boolean {
        return response.code == 401
    }

    // ✅ 요청 재시도 횟수 계산
    private fun responseCount(response: Response): Int {
        var current = response
        var count = 1
        while (current.priorResponse != null) {
            count++
            current = current.priorResponse!!
        }
        return count
    }

    // ✅ 로그아웃 처리
    private fun forceLogout() {
        if (context is Application) {
            val appContext = context.applicationContext
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(appContext, "세션이 만료되어 로그아웃되었습니다.", Toast.LENGTH_LONG).show()
            }
            val intent = Intent(appContext, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            appContext.startActivity(intent)
        }
    }
}

