package com.pizza.kkomdae.domain.model

data class PostSecondStageRequest(
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

