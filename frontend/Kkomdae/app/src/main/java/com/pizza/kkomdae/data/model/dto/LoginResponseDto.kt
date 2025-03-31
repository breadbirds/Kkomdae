package com.pizza.kkomdae.data.model.dto

data class LoginResponseDto(
    val jwt : String,
    val refreshToken : String
)
