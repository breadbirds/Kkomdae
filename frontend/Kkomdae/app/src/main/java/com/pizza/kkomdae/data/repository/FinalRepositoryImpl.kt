package com.pizza.kkomdae.data.repository

import android.util.Log
import com.pizza.kkomdae.data.model.FinalMapper
import com.pizza.kkomdae.data.model.LoginMapper
import com.pizza.kkomdae.data.model.Step2Mapper
import com.pizza.kkomdae.data.source.remote.FinalService
import com.pizza.kkomdae.data.source.remote.LoginService
import com.pizza.kkomdae.domain.model.FourthStageRequest
import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.GetPdfUrlResponse
import com.pizza.kkomdae.domain.model.GetTotalResultResponse
import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.repository.FinalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject

class FinalRepositoryImpl@Inject constructor(
    private val finalService: FinalService
):FinalRepository {
    override suspend fun postPdf(testId: Long): PostResponse {
        return try {
            Step2Mapper.toPostStageResponse(finalService.postPdf(testId))
        }catch (e: Exception){
            throw e
        }
    }

    override suspend fun getAiPhoto(testId: Long): GetAiPhotoResponse {
        return try {
            FinalMapper.toGetAiPhotoResponse(finalService.getAiPhoto(testId))
        }catch (e: Exception){
            throw e
        }
    }

    override suspend fun postFourthStage(fourthStageRequest: FourthStageRequest): PostResponse {
        return try {
            Step2Mapper.toPostStageResponse(finalService.postFourthStage(FinalMapper.toFourthStageRequestDto(fourthStageRequest)))
        }catch (e: Exception){
            throw e
        }
    }

    override suspend fun getLaptopTotalResult(testId: Long): GetTotalResultResponse {
        return try {
            FinalMapper.toGetTotalResultResponse(finalService.getLaptopTotalResult(testId))
        }catch (e: Exception){
            throw e
        }
    }

    override suspend fun getPdfUrl(name: String): GetPdfUrlResponse {
        return try {
            FinalMapper.toGetPdfUrlDto(finalService.getPdfUrl(name))
        }catch (e: Exception){
            throw e
        }
    }

    override fun postRePhoto(photoType: Int, testId: Long, file: MultipartBody.Part,): Flow<PostResponse> {
        return flow {
            try {
                val response = Step2Mapper.toPostStageResponse(finalService.postRePhoto(photoType,testId,file=file))
                emit(response)
            }catch (e:Exception){
                throw e
            }
        }
    }


}