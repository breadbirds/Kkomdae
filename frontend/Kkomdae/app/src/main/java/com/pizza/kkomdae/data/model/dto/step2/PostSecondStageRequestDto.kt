package com.pizza.kkomdae.data.model.dto.step2

data class PostSecondStageRequestDto(
    val testId :Long,
    val keyboardStatus: Boolean,
    var failedKeys : String="",
    val usbStatus: Boolean,
    var failedPorts : String="",
    val cameraStatus: Boolean,
    val chargerStatus: Boolean,
    val batteryReport: Boolean,
    val batteryReportUrl: String,
)
