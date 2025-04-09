package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.login.LoginResponseDto
import com.pizza.kkomdae.data.model.dto.login.RefreshTokenRequest
import com.pizza.kkomdae.data.model.dto.login.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginService {

    // 로그인 토큰 보내기
    @GET("api/sso/login")
    suspend fun getLogin( @Query("code") code: String): LoginResponseDto

    @POST("api/sso/refresh")
    fun postRefreshToken(
        @Body refreshToken: RefreshTokenRequest,
    ): Call<RefreshTokenResponse>


}