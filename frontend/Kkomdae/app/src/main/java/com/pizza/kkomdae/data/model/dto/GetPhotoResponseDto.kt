package com.pizza.kkomdae.data.model.dto

data class GetPhotoResponseDto(
    val success: Boolean,
    val status: String,
    val message: String,
    val data: Map<String, String>
)
