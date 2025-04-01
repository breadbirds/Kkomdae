package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.PostResponse

interface Step2Repository {

    suspend fun postSecondStage(
        postSecondStageRequest: PostSecondStageRequest
    ) : PostResponse
}