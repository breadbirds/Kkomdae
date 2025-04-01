package com.pizza.kkomdae.presenter.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
    application: Application,
    private val finalUseCase: FinalUseCase
) : ViewModel() {

    private val _pdfName = MutableLiveData<String>()
    val pdfName: LiveData<String>
        get() = _pdfName

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun postPdf(){
        val testId = sharedPreferences.getLong("test_id",0)
        viewModelScope.launch {
            val result = finalUseCase.postPdf(testId)
            Log.d(TAG, "postPdf: $result")
            result.onSuccess {
                _pdfName.postValue(it.message)
            }
        }
    }

    fun getAiPhoto(testId:Long){
        viewModelScope.launch {
            val result = finalUseCase.getAiPhoto(testId)
            Log.d(TAG, "getAiPhoto: $result")
        }
    }



}