package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.user.UserResponse

interface UserRepository {
    suspend fun getUserInfo() : UserResponse
}