package com.pizza.kkomdae.data.source.remote

import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FinalService {

    // 로그인 토큰 보내기
    @POST("api/pdf/{testId}")
    suspend fun postPdf(@Path("testId") testId: Long ) : Boolean
}