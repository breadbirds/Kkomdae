package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.user.UserResponseDto
import retrofit2.http.GET

interface UserService {

    // 로그인 토큰 보내기
    @GET("api/user-info")
    suspend fun getUserInfo(): UserResponseDto

    @GET("api/test-file/{file-name}")
    suspend fun getTestFile(fileName:String)

}