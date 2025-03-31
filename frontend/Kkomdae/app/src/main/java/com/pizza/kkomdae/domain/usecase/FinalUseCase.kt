package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.repository.FinalRepository
import com.pizza.kkomdae.domain.repository.InspectRepository
import javax.inject.Inject

class FinalUseCase@Inject constructor(
    private val finalRepository: FinalRepository
) {
    suspend fun postPdf(testId: Long): Boolean {
        return try {
            val response = finalRepository.postPdf(testId)
            response  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
           false  // ✅ 실패 시 Result.failure 반환
        }
    }
}