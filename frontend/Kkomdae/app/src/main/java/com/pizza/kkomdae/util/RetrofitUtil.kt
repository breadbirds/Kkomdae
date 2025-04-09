package com.pizza.kkomdae.util

import com.pizza.kkomdae.base.ApplicationClass
import com.pizza.kkomdae.data.source.remote.LoginService

class RetrofitUtil {
    companion object{
        val loginService = ApplicationClass.retrofit.create(LoginService::class.java)
    }
}