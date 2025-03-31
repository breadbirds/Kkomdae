package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.TestResponse
import com.pizza.kkomdae.domain.model.UserResponse
import com.pizza.kkomdae.domain.repository.InspectRepository
import com.pizza.kkomdae.domain.repository.UserRepository
import javax.inject.Inject

class InspectUseCase@Inject constructor(
    private val inspectRepository: InspectRepository
)  {
    suspend fun postTest(serialNum: String?): Result<Long> {
        return try {
            val response = inspectRepository.postText(serialNum =serialNum )
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }
}