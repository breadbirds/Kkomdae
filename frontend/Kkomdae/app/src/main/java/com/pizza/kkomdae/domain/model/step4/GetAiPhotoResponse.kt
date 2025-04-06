package com.pizza.kkomdae.domain.model.step4

data class GetAiPhotoResponse(
    val success : Boolean,
    val status : String,
    val message : String,
    val data: AiPhotoData
)
