package com.pizza.kkomdae.data.model.dto

data class GetStep2ResultResponseDto(
    val success: Boolean,
    val status : String,
    val message: String,
    val data : Step2ResultDataDto
)
