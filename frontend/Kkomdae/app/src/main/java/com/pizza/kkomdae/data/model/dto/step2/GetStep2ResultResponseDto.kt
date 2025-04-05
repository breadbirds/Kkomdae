package com.pizza.kkomdae.data.model.dto.step2

data class GetStep2ResultResponseDto(
    val success: Boolean,
    val status : String,
    val message: String,
    val data : Step2ResultDataDto
)
