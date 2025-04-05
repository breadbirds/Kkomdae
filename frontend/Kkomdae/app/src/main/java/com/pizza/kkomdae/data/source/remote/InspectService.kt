package com.pizza.kkomdae.data.source.remote

import retrofit2.http.POST
import retrofit2.http.Query

interface InspectService {
    // 로그인 토큰 보내기
    @POST("api/test")
    suspend fun postTest( @Query("rentId") serialNum: Int?): Long

}