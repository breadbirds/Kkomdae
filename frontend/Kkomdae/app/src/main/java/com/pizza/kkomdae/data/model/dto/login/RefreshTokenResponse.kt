package com.pizza.kkomdae.data.model.dto.login

data class RefreshTokenResponse(
    val jwt: String,
    val refreshToken: String
)
