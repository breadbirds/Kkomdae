package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.PostSecondStageRequestDto
import com.pizza.kkomdae.data.model.dto.PostResponseDto
import com.pizza.kkomdae.domain.model.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.PostResponse

object Step2Mapper {
    fun toPostSecondStageRequestDto(postSecondStageRequest: PostSecondStageRequest):PostSecondStageRequestDto=
        PostSecondStageRequestDto(
            testId = postSecondStageRequest.testId,
            keyboardStatus = postSecondStageRequest.keyboardStatus,
            failedKeys = postSecondStageRequest.failedKeys,
            usbStatus = postSecondStageRequest.usbStatus,
            failedPorts = postSecondStageRequest.failedPorts,
            cameraStatus = postSecondStageRequest.cameraStatus,
            chargerStatus = postSecondStageRequest.chargerStatus,
            batteryReport = postSecondStageRequest.batteryReport,
            batteryReportUrl = postSecondStageRequest.batteryReportUrl
    )

    fun toPostStageResponse(postResponseDto: PostResponseDto)= PostResponse(
        success= postResponseDto.success,
        message = postResponseDto.message,
        status = postResponseDto.status,

    )

}