package com.pizza.kkomdae.data.source.remote

import com.pizza.kkomdae.data.model.dto.step2.GetStep2ResultResponseDto
import com.pizza.kkomdae.data.model.dto.step2.PostRandomKeyResponseDto
import com.pizza.kkomdae.data.model.dto.step2.PostSecondStageRequestDto
import com.pizza.kkomdae.data.model.dto.step2.PostResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Step2Service {

    @POST("api/secondStage")
    suspend fun postSecondStage(
        @Body postSecondStageRequestDto: PostSecondStageRequestDto
    ): PostResponseDto

    @POST("api/random-key/{testId}")
    suspend fun postRandomKey(
        @Path("testId") testId: Long
    ): PostRandomKeyResponseDto

    @GET("api/test-result/{testId}")
    suspend fun getStep2Result(
        @Path("testId") testId: Long
    ): GetStep2ResultResponseDto

    @POST("api/secondToThird/{testId}")
    suspend fun postSecondToThird(
        @Path("testId") testId: Long
    ): PostResponseDto

}