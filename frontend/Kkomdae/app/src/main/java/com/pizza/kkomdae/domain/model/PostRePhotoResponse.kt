package com.pizza.kkomdae.domain.model


data class PostRePhotoResponse(
    val success : Boolean,
    val message : String,
    val status : String,
    val data : RePhotoData
)
