package com.pizza.kkomdae.domain.model

data class UserRentTestResponse(
    val modelCode : String,
    val dateTime : String?,
    val release : Boolean,
    val rentPdfName: String?,
    val releasePdfName : String?,
    val onGoingTestId : Int,
    val stage : Int,
    val picStage : Int
)
