package com.pizza.kkomdae.domain.model

import com.pizza.kkomdae.data.model.dto.GetAiPhotoDataDto

data class GetAiPhotoResponse(
    val success : Boolean,
    val status : String,
    val message : String,
    val data: AiPhotoData
)
