package com.pizza.kkomdae.data.model.dto

data class RefreshTokenResponse(
    val jwt: String,
    val refreshToken: String
)
