package com.pizza.kkomdae.data.model.dto

data class PostRandomKeyResponseDto(
    val success: Boolean,
    val status: String,
    val message: String,
    val data : RandomKeyDataDto
)
