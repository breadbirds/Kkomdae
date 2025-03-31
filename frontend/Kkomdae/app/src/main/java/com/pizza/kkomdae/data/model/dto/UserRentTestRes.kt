package com.pizza.kkomdae.data.model.dto

data class UserRentTestRes(
    val modelCode : String,
    val dateTime : String,
    val release : Boolean,
    val rentPdfName: String,
    val releasePdfName : String,
    val onGoingTestId : Int,
    val stage : Int,
    val picStage : Int
)
