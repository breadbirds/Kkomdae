package com.pizza.kkomdae.domain.model

data class LoginResponse(
    val jwt : String,
    val refreshToken : String
)
