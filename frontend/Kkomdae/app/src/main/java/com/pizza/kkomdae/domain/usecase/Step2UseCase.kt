package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.step2.GetStep2ResultResponse
import com.pizza.kkomdae.domain.model.step2.PostRandomKeyResponse
import com.pizza.kkomdae.domain.model.step2.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.step2.PostResponse
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

    suspend fun postRandomKey(
        testId: Long
    ): Result<PostRandomKeyResponse>{
        return try {
            val response = step2Repository.postRandomKey(testId)
            Result.success(response)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun getStep2Result(
        testId: Long
    ): Result<GetStep2ResultResponse>{
        return try {
            val response = step2Repository.getStep2Result(testId)
            Result.success(response)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun postSecondToThird(
        testId: Long
    ): Result<PostResponse>{
        return try {
            val response = step2Repository.postSecondToThird(testId)
            Result.success(response)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

}