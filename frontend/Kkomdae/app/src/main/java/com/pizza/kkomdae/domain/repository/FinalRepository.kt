package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.FourthStageRequest
import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.model.PostResponse

interface FinalRepository {

    suspend fun postPdf(testId: Long) : PostResponse

    suspend fun getAiPhoto(testId: Long) : GetAiPhotoResponse

    suspend fun postFourthStage(fourthStageRequest: FourthStageRequest) : PostResponse
}