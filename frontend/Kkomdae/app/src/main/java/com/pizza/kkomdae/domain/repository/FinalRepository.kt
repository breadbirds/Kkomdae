package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.LoginResponse

interface FinalRepository {

    suspend fun postPdf(testId: Long) : Boolean

    suspend fun getAiPhoto(testId: Long) : GetAiPhotoResponse
}