package com.pizza.kkomdae.domain.usecase

import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.model.UserResponse
import com.pizza.kkomdae.domain.repository.LoginRepository
import com.pizza.kkomdae.domain.repository.UserRepository
import javax.inject.Inject

class MainUseCase@Inject constructor(
    private val userRepository: UserRepository
)  {
    suspend fun getUserInfo(): Result<UserResponse> {
        return try {
            val response = userRepository.getUserInfo()
            Result.success(response)  // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }
    }
}