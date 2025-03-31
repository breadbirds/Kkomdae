package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.LoginResponseDto
import com.pizza.kkomdae.data.model.dto.TestResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface InspectService {
    // 로그인 토큰 보내기
    @POST("api/test")
    suspend fun postTest( @Query("serialNum") serialNum: String?): Long

}