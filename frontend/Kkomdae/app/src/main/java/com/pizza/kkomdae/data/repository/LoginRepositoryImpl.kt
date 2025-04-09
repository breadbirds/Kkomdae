package com.pizza.kkomdae.data.repository

import android.util.Log
import com.pizza.kkomdae.data.model.mapper.LoginMapper
import com.pizza.kkomdae.data.source.remote.LoginService
import com.pizza.kkomdae.domain.model.login.LoginResponse
import com.pizza.kkomdae.domain.repository.LoginRepository
import javax.inject.Inject

private const val TAG = "LoginRepositoryImpl"
class LoginRepositoryImpl @Inject constructor(
    private val loginService: LoginService
) : LoginRepository {
    override suspend fun login(code: String): LoginResponse {
        return try {
            Log.d(TAG, "login: $code")
            LoginMapper.toLoginResponse(loginService.getLogin(code))
        }catch (e: Exception){
            throw e
        }
//        return LoginResponse("askjdfalkasdf","")
    }


}