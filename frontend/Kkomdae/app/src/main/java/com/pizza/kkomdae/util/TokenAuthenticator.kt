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

class TokenAuthenticator(val context: Context) : Authenticator {
    private val refreshAttempts = AtomicInteger(0)
    private val MAX_REFRESH_ATTEMPTS = 1 // 최대 재시도 횟수 제한

    private var tokenCheck = true
    override fun authenticate(route: Route?, response: Response): Request? {
        if (isTokenExpired(response) && refreshAttempts.getAndIncrement() < MAX_REFRESH_ATTEMPTS) {
            Log.e("Authenticator", response.toString())
            Log.e("Authenticator", "토큰 재발급 시도")
            val tokenManager = TokenManager(context)
            val secureTokenManager = SecureTokenManager(context)
            val refreshToken = secureTokenManager.getRefreshToken()

            val reissueData = loginService.postRefreshToken( refreshToken = RefreshTokenRequest(refreshToken.toString())).execute()
            if(reissueData.code()==200){
                val newJwtToken = reissueData?.body()?.jwt
                val newRefreshToken = reissueData?.body()?.refreshToken
                tokenManager.saveAccessToken(newJwtToken?:"")
                secureTokenManager.saveRefreshToken(newRefreshToken?:"")
                Log.d("Authenticator", "authenticate: $reissueData")
                Log.e("Authorization Token", newJwtToken.toString())


                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newJwtToken")
                    .build()
            }
            tokenCheck = false
            if (context is Application) {
                val appContext = context.applicationContext
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(appContext, "세션이 만료되어 로그아웃되었습니다.", Toast.LENGTH_LONG).show()
                }
                Log.d("Authenticator", "authenticate: 로그아웃 ")
                val intent = Intent(appContext, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                appContext.startActivity(intent)
//

            }


        }


        return null


//        val newToken = runBlocking {
//            if (isTokenExpired(response)) {
//                var obtainedToken: String? = null
//                RetrofitManager.instance.postReissue(){
//                    obtainedToken = it
//                }
//                obtainedToken ?: ""
//            } else {
//                // 토큰이 만료되지 않은 경우 기존 토큰 반환
//                ""
//            }
//        }
//            if (newToken.isNotEmpty()) {
//
//                Log.e("newToken",newToken)
//                // 새로운 토큰이 성공적으로 얻어졌을 경우, 요청을 재시도
//                return response.request.newBuilder()
//                    .header("Authorization", "Bearer $newToken")
//                    .build()
//            }
//
//        return null
    }

//    private fun request(response: Response): Request {
//        Log.i("Authenticator", "토큰 재발급 성공 : $newAccessToken")
//        return response.request.newBuilder()
//            .removeHeader("Authorization").apply {
//                addHeader("Authorization", "Bearer $newAccessToken")
//            }.build() // 토큰 재발급이 성공했다면, 기존 헤더를 지우고, 새로운 해더를 단다.
//    }


    private fun isTokenExpired(response: Response): Boolean {
        Log.e("response.code",response.code.toString())
        return response.code == 401

    }

}