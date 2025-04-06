package com.pizza.kkomdae.data.model.mapper

import com.pizza.kkomdae.data.model.dto.step3.PostThirdStageRequestDto
import com.pizza.kkomdae.domain.model.step3.PostThirdStageRequest

object Step3Mapper {
    fun toPostThirdStageRequestDto(postThirdStageRequest: PostThirdStageRequest): PostThirdStageRequestDto =
        PostThirdStageRequestDto(
            testId = postThirdStageRequest.testId,
            release= postThirdStageRequest.release,
            modelCode = postThirdStageRequest.modelCode,
            serialNum = postThirdStageRequest.serialNum,
            barcodeNum= postThirdStageRequest.barcodeNum,
            localDate = postThirdStageRequest.localDate,
            laptop = postThirdStageRequest.laptop,
            powerCable= postThirdStageRequest.powerCable,
            adapter = postThirdStageRequest.adapter,
            mouse = postThirdStageRequest.mouse,
            bag = postThirdStageRequest.bag,
            mousePad = postThirdStageRequest.mousePad

        )
}
