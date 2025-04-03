package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.GetPhotoResponse
import com.pizza.kkomdae.domain.model.PhotoResponse
import com.pizza.kkomdae.domain.repository.LoginRepository
import com.pizza.kkomdae.domain.repository.Step1Repository
import okhttp3.MultipartBody
import javax.inject.Inject

class Step1UseCase@Inject constructor(
    private val step1Repository: Step1Repository
) {
    suspend fun postPhoto(
        testId:Long,
        photoType: Int,
        file: MultipartBody.Part,
    ): Result<PhotoResponse> {
        return try {
            val response = step1Repository.postPhoto(
                photoType = photoType,
                testId = testId,
                file = file
            )
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }

    suspend fun getPhoto(
        testId:Long,
    ): Result<GetPhotoResponse> {
        return try {
            val response = step1Repository.getPhoto(
                testId = testId,
            )
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }
}