package com.pizza.kkomdae.presenter.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizza.kkomdae.domain.model.LoginResponse
import com.pizza.kkomdae.domain.usecase.FinalUseCase
import com.pizza.kkomdae.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FinalViewModel"
@HiltViewModel
class FinalViewModel @Inject constructor(
    private val finalUseCase: FinalUseCase
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>>
        get() = _loginResult

    fun postPdf(testId: Long){
        viewModelScope.launch {
            val result = finalUseCase.postPdf(testId)
            Log.d(TAG, "login: $result")
        }
    }


}