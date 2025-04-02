package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.FourthStageRequest
import com.pizza.kkomdae.domain.model.GetAiPhotoResponse
import com.pizza.kkomdae.domain.model.GetPdfUrlResponse
import com.pizza.kkomdae.domain.model.GetTotalResultResponse
import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.repository.FinalRepository
import com.pizza.kkomdae.domain.repository.InspectRepository
import javax.inject.Inject

class FinalUseCase@Inject constructor(
    private val finalRepository: FinalRepository
) {
    suspend fun postPdf(testId: Long): Result<PostResponse> {
        return try {
            val response = finalRepository.postPdf(testId)
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }

    suspend fun getAiPhoto(testId: Long): Result<GetAiPhotoResponse>{
        return try {
            val response = finalRepository.getAiPhoto(testId)
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }

    suspend fun getPdfUrl(name:String): Result<GetPdfUrlResponse>{
        return try {
            val response = finalRepository.getPdfUrl(name)
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }

    suspend fun postFourthStage(fourthStageRequest: FourthStageRequest): Result<PostResponse>{
        return try {
            val response = finalRepository.postFourthStage(fourthStageRequest)
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }

    suspend fun getLaptopTotalResult(testId: Long): Result<GetTotalResultResponse>{
        return try {
            val response = finalRepository.getLaptopTotalResult(testId)
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }
}