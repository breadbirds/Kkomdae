package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.login.LoginResponse
import com.pizza.kkomdae.domain.repository.LoginRepository
import javax.inject.Inject

class LoginUseCase@Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend fun login(code: String): Result<LoginResponse> {
        return try {
            val response = loginRepository.login(code)
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }
}