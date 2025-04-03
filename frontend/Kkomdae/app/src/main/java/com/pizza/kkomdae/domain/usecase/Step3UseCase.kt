package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.PostResponse
import com.pizza.kkomdae.domain.model.PostThirdStageRequest
import com.pizza.kkomdae.domain.repository.Step3Repository
import javax.inject.Inject

class Step3UseCase@Inject constructor(
    private val step3Repository: Step3Repository
) {

    suspend fun postThirdStage(
        postThirdStageRequest: PostThirdStageRequest
    ): Result<PostResponse> {
        return try {
            val response = step3Repository.postThirdStage(
                postThirdStageRequest= postThirdStageRequest
            )
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }

}