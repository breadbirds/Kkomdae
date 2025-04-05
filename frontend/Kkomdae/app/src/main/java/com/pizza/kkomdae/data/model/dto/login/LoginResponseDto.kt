package com.pizza.kkomdae.data.model.dto.login

data class LoginResponseDto(
    val jwt : String,
    val refreshToken : String
)
