package com.pizza.kkomdae.data.model

import com.pizza.kkomdae.data.model.dto.GetPhotoResponseDto
import com.pizza.kkomdae.data.model.dto.PhotoResponseDto
import com.pizza.kkomdae.data.model.dto.PostSecondStageRequestDto
import com.pizza.kkomdae.data.model.dto.PostSecondStageResponseDto
import com.pizza.kkomdae.domain.model.GetPhotoResponse
import com.pizza.kkomdae.domain.model.PhotoResponse
import com.pizza.kkomdae.domain.model.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.PostSecondStageResponse

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

    fun toPostSecondStageResponse(postSecondStageResponseDto: PostSecondStageResponseDto)= PostSecondStageResponse(
        success= postSecondStageResponseDto.success,
        message = postSecondStageResponseDto.message,
        status = postSecondStageResponseDto.status,

    )

}