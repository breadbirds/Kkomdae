package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.repository.Step2Repository
import javax.inject.Inject

class Step2UseCase@Inject constructor(
    private val step2Repository: Step2Repository
) {

    suspend fun postSecondStage(
        postSecondStageRequest: PostSecondStageRequest
    ): Result<PostResponse> {
        return try {
            val response = step2Repository.postSecondStage(
                postSecondStageRequest= postSecondStageRequest
            )
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }

}