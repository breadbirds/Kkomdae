package com.pizza.kkomdae.presenter.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizza.kkomdae.domain.usecase.InspectUseCase
import com.pizza.kkomdae.domain.usecase.MainUseCase
import com.pizza.kkomdae.domain.usecase.Step1UseCase
import com.pizza.kkomdae.presenter.model.UserInfoResponse
import com.pizza.kkomdae.presenter.model.UserRentTestResponse
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


private const val TAG = "MainViewModel"
@HiltViewModel
class MainViewModel@Inject constructor(
    private val application: Application,
    private val mainUseCase: MainUseCase,
    private val step1UseCase: Step1UseCase,
    private val inspectUseCase: InspectUseCase
): ViewModel() {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _testId = MutableLiveData<Long>()
    val testId: LiveData<Long>
        get() = _testId

    private val _Fail = MutableLiveData<Boolean>()
    val Fail: LiveData<Boolean>
        get() = _Fail

    private val _picStage = MutableLiveData<Int>()
    val picStage: LiveData<Int>
        get() = _picStage

    private val _stage = MutableLiveData<Int>()
    val stage: LiveData<Int>
        get() = _stage

    private val _pdfUrl = MutableLiveData<String>()
    val pdfUrl: LiveData<String>
        get() = _pdfUrl

    private val _releasePicStage = MutableLiveData<Int>()
    val releasePicStage: LiveData<Int>
        get() = _releasePicStage

    private val _userInfoResult = MutableLiveData<UserInfoResponse>()
    val userInfoResult: LiveData<UserInfoResponse>
        get() = _userInfoResult

    private val _resultImage = MutableLiveData<List<String>>()
    val resultImage: LiveData<List<String>>
        get() = _resultImage

    private val _release = MutableLiveData<Boolean>()
    val release: LiveData<Boolean>
        get() = _release


    fun setPdfUrl(url: String){
        _pdfUrl.postValue(url)
    }

    fun getPdfUrl():String{
        return pdfUrl.value?:""
    }

    fun setReleasePicStage(stage: Int){
        _releasePicStage.postValue(stage)
        savePhotoStage(stage)
    }

    fun setRelease(release:Boolean){
        _release.postValue(release)
    }

    suspend fun postTest(rentId: Int?): Result<Long>{
      return try {
            val result = inspectUseCase.postTest(rentId = rentId)
            Log.d(TAG, "getUserInfo: $result")
           result
        }catch (e:Exception){
           Result.failure(e)
        }
    }

    suspend fun postReleaseTest(rentId: Int):Result<Long>{


        return try {
            val response = inspectUseCase.postTest(rentId =rentId )
            response // ✅ 성공 시 Result.success 반환
        } catch (e: Exception) {
            Result.failure(e)  // ✅ 실패 시 Result.failure 반환
        }




    }
    
    // 사진 정보 불러오기
    fun getPhoto(testId: Long){
        viewModelScope.launch {
            val result = step1UseCase.getPhoto(testId)
            result.onSuccess { testResponse ->
                // 로그인 성공 시 실제 데이터 처리
                val sortedMap = testResponse.data.toSortedMap()
                // URL만 리스트에 담기
                val urlList = sortedMap.values.toList()
                _resultImage.postValue(urlList)

                Log.d(TAG, "getPhoto: ${testResponse.data}")
                Log.d(TAG, "getPhoto: ${urlList}")


            }.onFailure { exception ->
                // 로그인 정보 불러오기 실패

            }
        }
    }
    

    // 유저 정보 불러오기
    fun getUserInfo(){
        viewModelScope.launch {
            val result = mainUseCase.getUserInfo()
            Log.d(TAG, "getUserInfo: $result")

            result.onSuccess { userInfoResponse ->
                // 로그인 성공 시 실제 데이터 처리
                userInfoResponse?.let {
                    if(it.onGoingTestId!=0){
                        saveTestId(it.onGoingTestId.toLong())
                    }

                    _testId.postValue(it.onGoingTestId.toLong())
                    _picStage.postValue(it.picStage)
                    _stage.postValue(it.stage)

                    savePhotoStage(it.picStage)

                    val data = UserInfoResponse(
                        onGoingTestId = it.onGoingTestId,
                        stage = it.stage,
                        picStage = it.picStage,
                        name = it.name,
                        userRentTestRes = it.userRentTestRes.map {
                            UserRentTestResponse(
                                modelCode = it.modelCode,
                                dateTime = it.dateTime,
                                release = it.release,
                                rentPdfName = it.rentPdfName,
                                releasePdfName = it.releasePdfName,
                                onGoingTestId = it.onGoingTestId,
                                stage = it.stage,
                                picStage = it.picStage,
                                serialNum = it.serialNum,
                                rentId = it.rentId
                            )
                        }
                        )
                    _userInfoResult.postValue(data)


                }


            }.onFailure { exception ->
                // 로그인 정보 불러오기 실패
//                _Fail.postValue(false)

            }

        }
    }

    fun getPhotoStage():Int{
        return sharedPreferences.getInt("photoStage",0)
    }

    fun saveTestId(testId: Long) {
        sharedPreferences.edit().putLong("test_id", testId).apply()
    }


   fun savePhotoStage(step: Int) {
        sharedPreferences.edit().putInt("photoStage", step).apply()
    }

    fun downloadPdf(pdfUrl: String) {
        val request = DownloadManager.Request(Uri.parse(pdfUrl))
            .setTitle("PDF 다운로드")
            .setDescription("PDF 파일을 다운로드 중입니다")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_file.pdf")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }


}