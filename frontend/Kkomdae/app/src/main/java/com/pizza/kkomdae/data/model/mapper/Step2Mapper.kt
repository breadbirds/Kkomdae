package com.pizza.kkomdae.data.model.mapper

import com.pizza.kkomdae.data.model.dto.step2.GetStep2ResultResponseDto
import com.pizza.kkomdae.data.model.dto.step2.PostRandomKeyResponseDto
import com.pizza.kkomdae.data.model.dto.step2.PostSecondStageRequestDto
import com.pizza.kkomdae.data.model.dto.step2.PostResponseDto
import com.pizza.kkomdae.data.model.dto.step2.RandomKeyDataDto
import com.pizza.kkomdae.data.model.dto.step2.Step2ResultDataDto
import com.pizza.kkomdae.domain.model.step2.GetStep2ResultResponse
import com.pizza.kkomdae.domain.model.step2.PostRandomKeyResponse
import com.pizza.kkomdae.domain.model.step2.PostSecondStageRequest
import com.pizza.kkomdae.domain.model.step2.PostResponse
import com.pizza.kkomdae.domain.model.step2.RandomKeyData
import com.pizza.kkomdae.domain.model.step2.Step2ResultData

object Step2Mapper {
    fun toPostSecondStageRequestDto(postSecondStageRequest: PostSecondStageRequest): PostSecondStageRequestDto =
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

    fun toPostRandomKeyResponse(postRandomKeyResponseDto: PostRandomKeyResponseDto)= PostRandomKeyResponse(
        success= postRandomKeyResponseDto.success,
        message = postRandomKeyResponseDto.message,
        status = postRandomKeyResponseDto.status,
        data = toRandomKeyData(postRandomKeyResponseDto.data)
        )

    fun toRandomKeyData(randomKeyDataDto: RandomKeyDataDto) = RandomKeyData(
        randomKey = randomKeyDataDto.randomKey
    )

    fun toGetStep2ResultResponse(getStep2ResultResponseDto: GetStep2ResultResponseDto)= GetStep2ResultResponse(
        success= getStep2ResultResponseDto.success,
        message = getStep2ResultResponseDto.message,
        status = getStep2ResultResponseDto.status,
        data = toStep2ResultData(getStep2ResultResponseDto.data)
    )

    fun toStep2ResultData(step2ResultDataDto: Step2ResultDataDto) = Step2ResultData(
        usb_status = step2ResultDataDto.usb_status,
        camera_status = step2ResultDataDto.camera_status,
       battery_report =  step2ResultDataDto.battery_report,
        charging_status = step2ResultDataDto.charging_status,
        keyboard_status = step2ResultDataDto.keyboard_status,
        failed_keys = step2ResultDataDto.failed_keys,
        failed_ports = step2ResultDataDto.failed_ports,
        battery_report_url = step2ResultDataDto.battery_report_url,
        success = step2ResultDataDto.success
    )

}