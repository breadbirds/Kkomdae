package com.pizza.kkomdae.data.model.dto.step4

data class GetAiPhotoResponseDto(
    val success : Boolean,
    val status : String,
    val message : String,
    val data: AiPhotoDataDto
)
