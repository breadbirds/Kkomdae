package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.PostSecondStageResponse
import okhttp3.MultipartBody

interface Step2Repository {

    suspend fun postSecondStage(
        postSecondStageRequest: PostSecondStageRequest
    ) : PostSecondStageResponse
}