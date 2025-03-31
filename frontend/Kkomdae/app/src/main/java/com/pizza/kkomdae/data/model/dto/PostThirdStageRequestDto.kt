package com.pizza.kkomdae.data.model.dto

data class PostThirdStageRequestDto(
    val testId : Long,
    val release : Boolean,
    val modelCode : String,
    val serialNum: String,
    val barcodeNum: String,
    val localDate: String,
    val laptop: Int,
    val powerCable: Int,
    val adapter: Int,
    val mouse: Int,
    val bag: Int,
    val mousePad: Int,
)
