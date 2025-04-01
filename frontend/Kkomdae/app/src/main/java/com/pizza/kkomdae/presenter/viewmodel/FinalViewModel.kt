package com.pizza.kkomdae.presenter.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
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

    private val _frontUri = MutableLiveData<String?>()
    val frontUri: LiveData<String?>
        get() = _frontUri

    private val _backUri = MutableLiveData<String?>()
    val backUri: LiveData<String?>
        get() = _backUri

    private val _leftUri = MutableLiveData<String?>()
    val leftUri: LiveData<String?>
        get() = _leftUri

    private val _rightUri = MutableLiveData<String?>()
    val rightUri: LiveData<String?>
        get() = _rightUri

    private val _screenUri = MutableLiveData<String?>()
    val screenUri: LiveData<String?>
        get() = _screenUri

    private val _keypadUri = MutableLiveData<String?>()
    val keypadUri: LiveData<String?>
        get() = _keypadUri

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


    fun getAiPhoto(){
        val testId = sharedPreferences.getLong("test_id",0)
        viewModelScope.launch {
            val result = finalUseCase.getAiPhoto(testId)
            Log.d(TAG, "getAiPhoto: $result")
            result.onSuccess {
                _frontUri.postValue(it.data.Picture1_ai_url)
                _backUri.postValue(it.data.Picture2_ai_url)
                _leftUri.postValue(it.data.Picture3_ai_url)
                _rightUri.postValue(it.data.Picture4_ai_url)
                _screenUri.postValue(it.data.Picture5_ai_url)
                _keypadUri.postValue(it.data.Picture6_ai_url)
            }


        }
    }



}