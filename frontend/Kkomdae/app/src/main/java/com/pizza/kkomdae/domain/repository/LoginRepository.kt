package com.pizza.kkomdae.domain.repository

import com.pizza.kkomdae.domain.model.LoginResponse

interface LoginRepository {

    suspend fun login(code: String) : LoginResponse
}