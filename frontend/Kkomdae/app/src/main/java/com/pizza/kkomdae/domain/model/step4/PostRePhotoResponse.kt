package com.pizza.kkomdae.domain.model.step4


data class PostRePhotoResponse(
    val success : Boolean,
    val message : String,
    val status : String,
    val data : RePhotoData
)
