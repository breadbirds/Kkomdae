package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.FourthStageRequest
import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.GetPdfUrlResponse
import com.pizza.kkomdae.domain.model.GetTotalResultResponse
import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.model.PostRePhotoResponse
import com.pizza.kkomdae.domain.model.PostResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface FinalRepository {

    suspend fun postPdf(testId: Long) : PostResponse

    suspend fun getAiPhoto(testId: Long) : GetAiPhotoResponse

    suspend fun postFourthStage(fourthStageRequest: FourthStageRequest) : PostResponse

    suspend fun getLaptopTotalResult(testId: Long): GetTotalResultResponse

    suspend fun getPdfUrl(name: String): GetPdfUrlResponse

   fun postRePhoto(photoType: Int, testId: Long, file: MultipartBody.Part): Flow<PostRePhotoResponse>
}