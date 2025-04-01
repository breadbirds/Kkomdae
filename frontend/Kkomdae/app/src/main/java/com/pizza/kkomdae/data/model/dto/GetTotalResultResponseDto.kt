package com.pizza.kkomdae.data.model.dto

data class GetTotalResultResponseDto(
    val keyboardStatus : Boolean,
    val useStatus : Boolean,
    val cameraStatus : Boolean,
    val batteryStatus: Boolean,
    val chargerStatus: Boolean,
    val modelCode: String,
    val serialNum: String,
    val barcodeNum: String,
    val date: String,
    val laptopCount: Int,
    val mouseCount: Int,
    val powerCableCount: Int,
    val bagCount:Int,
    val adapterCount: Int,
    val mousepadCount:Int,
    val description : String?,
    val imageNames : List<String>
)
