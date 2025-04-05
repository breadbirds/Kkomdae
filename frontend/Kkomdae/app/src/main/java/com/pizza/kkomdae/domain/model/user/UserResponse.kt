package com.pizza.kkomdae.domain.model.user

import com.pizza.kkomdae.data.model.dto.user.UserRentTestRes

data class UserResponse(
    val onGoingTestId : Int,
    val stage : Int,
    val picStage : Int,
    val name : String,
    val userRentTestRes : List<UserRentTestRes>
)
