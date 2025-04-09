package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.step2.GetStep2ResultResponse
import com.pizza.kkomdae.domain.model.step2.PostRandomKeyResponse
import com.pizza.kkomdae.domain.model.step2.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.step2.PostResponse

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