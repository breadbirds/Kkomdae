package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.model.PostThirdStageRequest

interface Step3Repository {

    suspend fun postThirdStage(
        postThirdStageRequest: PostThirdStageRequest
    ) : PostResponse
}