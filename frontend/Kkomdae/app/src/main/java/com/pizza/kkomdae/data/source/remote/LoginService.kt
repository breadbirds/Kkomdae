package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.LoginResponseDto
import com.pizza.kkomdae.data.model.dto.RefreshTokenRequest
import com.pizza.kkomdae.data.model.dto.RefreshTokenResponse
import retrofit2.Call
import retrofit2.Response
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