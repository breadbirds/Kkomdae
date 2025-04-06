package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.step2.PostResponse
import com.pizza.kkomdae.domain.model.step3.PostThirdStageRequest

interface Step3Repository {

    suspend fun postThirdStage(
        postThirdStageRequest: PostThirdStageRequest
    ) : PostResponse
}