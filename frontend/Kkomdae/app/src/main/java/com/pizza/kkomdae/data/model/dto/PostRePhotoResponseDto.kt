package com.pizza.kkomdae.data.model.dto

data class PostRePhotoResponseDto(
    val success : Boolean,
    val message : String,
    val status : String,
    val data : RePhotoDataDto
)
