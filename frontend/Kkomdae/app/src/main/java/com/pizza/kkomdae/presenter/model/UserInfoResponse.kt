package com.pizza.kkomdae.presenter.model


data class UserInfoResponse(
    val onGoingTestId : Int,
    val stage : Int,
    val picStage : Int,
    val name : String,
    val userRentTestRes : List<UserRentTestResponse>
)
