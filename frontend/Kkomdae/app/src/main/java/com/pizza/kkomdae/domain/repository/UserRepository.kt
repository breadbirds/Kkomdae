package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.model.UserResponse

interface UserRepository {
    suspend fun getUserInfo() : UserResponse
}