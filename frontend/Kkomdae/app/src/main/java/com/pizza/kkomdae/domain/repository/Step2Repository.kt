package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.GetStep2ResultResponse
import com.pizza.kkomdae.domain.model.PostRandomKeyResponse
import com.pizza.kkomdae.domain.model.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.PostResponse

interface Step2Repository {

    suspend fun postSecondStage(
        postSecondStageRequest: PostSecondStageRequest
    ) : PostResponse

    suspend fun postRandomKey(
        testId:Long
    ): PostRandomKeyResponse

    suspend fun getStep2Result(
        testId:Long
    ): GetStep2ResultResponse

    suspend fun postSecondToThird(
        testId:Long
    ): PostResponse
}