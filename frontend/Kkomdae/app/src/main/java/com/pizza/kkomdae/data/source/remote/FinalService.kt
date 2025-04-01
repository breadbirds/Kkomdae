package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.GetAiPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.PostResponseDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FinalService {

    // 로그인 토큰 보내기
    @POST("api/pdf/{testId}")
    suspend fun postPdf(@Path("testId") testId: Long ) : PostResponseDto

    @GET("api/ai-photo")
    suspend fun getAiPhoto( @Query("testId") testId: Long): GetAiPhotoResponseDto
}