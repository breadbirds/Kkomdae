package com.pizza.kkomdae.domain.model.step4

data class GetTotalResultResponse(
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
    val imageUrls : List<String>
)
