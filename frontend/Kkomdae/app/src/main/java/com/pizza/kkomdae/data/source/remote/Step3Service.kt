package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.PostResponseDto
import com.pizza.kkomdae.data.model.dto.PostThirdStageRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface Step3Service {
    @POST("api/thirdStage")
    suspend fun postThirdStage(
        @Body postThirdStageRequestDto: PostThirdStageRequestDto
    ): PostResponseDto
}