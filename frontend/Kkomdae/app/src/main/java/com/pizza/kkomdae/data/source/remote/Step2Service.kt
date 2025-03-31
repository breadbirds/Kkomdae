package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.PostSecondStageRequestDto
import com.pizza.kkomdae.data.model.dto.PostSecondStageResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface Step2Service {

    @POST("api/secondStage")
    suspend fun postSecondStage(
        @Body postSecondStageRequestDto: PostSecondStageRequestDto
    ): PostSecondStageResponseDto
}