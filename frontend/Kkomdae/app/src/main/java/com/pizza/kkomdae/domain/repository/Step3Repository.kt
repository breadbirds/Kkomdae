package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.PostSecondStageResponse
import com.pizza.kkomdae.domain.model.PostThirdStageRequest

interface Step3Repository {

    suspend fun postThirdStage(
        postThirdStageRequest: PostThirdStageRequest
    ) : Boolean
}