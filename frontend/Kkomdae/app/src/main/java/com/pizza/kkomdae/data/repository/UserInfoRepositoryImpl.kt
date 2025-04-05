package com.pizza.kkomdae.data.repository

import com.pizza.kkomdae.data.model.mapper.UserMapper
import com.pizza.kkomdae.data.source.remote.UserService
import com.pizza.kkomdae.domain.model.user.UserResponse
import com.pizza.kkomdae.domain.repository.UserRepository
import javax.inject.Inject

private const val TAG = "UserInfoRepositoryImpl"
class UserInfoRepositoryImpl@Inject constructor(
    private val userService: UserService
) : UserRepository {
    override suspend fun getUserInfo(): UserResponse {
        return try {
            UserMapper.toUserResponse(userService.getUserInfo())
        }catch (e: Exception){
            throw e
        }
    }


}