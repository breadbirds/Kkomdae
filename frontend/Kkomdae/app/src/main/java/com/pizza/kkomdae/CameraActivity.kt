package com.pizza.kkomdae

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.pizza.kkomdae.base.BaseActivity
import com.pizza.kkomdae.databinding.ActivityCameraBinding
import com.pizza.kkomdae.presenter.viewmodel.CameraViewModel
import com.pizza.kkomdae.ui.guide.BackShotGuideFragment
import com.pizza.kkomdae.ui.guide.FrontShotGuideFragment
import com.pizza.kkomdae.ui.guide.KeypadGuideFragment
import com.pizza.kkomdae.ui.guide.LeftGuideFragment
import com.pizza.kkomdae.ui.guide.RightGuideFragment
import com.pizza.kkomdae.ui.guide.ScreenShotGuideFragment
import com.pizza.kkomdae.ui.step1.ResultFragment
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraActivity : BaseActivity() {


    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }
    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()
        val stage = intent.getIntExtra("stage", -1) + 1
        viewModel.setReCameraStage(stage)


        val type = viewModel.getPhotoStage()
        // ✅ 상태바 제거 (전체 화면 모드)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        var step = type + 1


        Log.d("Post", "onCreate: $step")
        if (stage != 0) {
            changeFragment(stage)
        } else {
            changeFragment(step)
        }


        // 상태바 뒤로가기 처리를 위한 콜백 등록 로직
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showStopCameraDialog()
            }
        })

    }

    fun moveToBackReCamera(uri: Uri) {
        Log.d("TAG", "moveToBackReCamera: ")
        val resultIntent = Intent().apply {
            putExtra("RE_PHOTO_URI", uri.toString())  // ✅ URI 값을 전달
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun moveToBack() {
        finish()
    }

    fun changeFragment(type: Int) {
        when (type) {
            0 -> { // 촬영 확인
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_camera, ResultFragment())
                    .commit()
            }

            1 -> { // 전면부 촬영 가이드
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_camera, FrontShotGuideFragment())
                    .addToBackStack("sadfa")
                    .commit()
            }

            2 -> { // 후면부 촬영 가이드
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_camera, BackShotGuideFragment())
                    .addToBackStack("sadfa")
                    .commit()
            }

            3 -> { // 좌측 촬영 가이드
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_camera, LeftGuideFragment())
                    .addToBackStack("sadfa")
                    .commit()
            }

            4 -> { // 우측 촬영 가이드
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_camera, RightGuideFragment())
                    .addToBackStack("sadfa")
                    .commit()
            }

            5 -> { // 화면 촬영 가이드
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_camera, ScreenShotGuideFragment())
                    .addToBackStack("sadfa")
                    .commit()
            }

            6 -> { // 키패드 촬영 가이드
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_camera, KeypadGuideFragment())
                    .addToBackStack("sadfa")
                    .commit()
            }

            7 -> { // 이전버튼
                Log.d("TAG", "changeFragment: ")
                val resultIntent = Intent().apply {
                    putExtra("PHOTO_URI", 1)  // ✅ URI 값을 전달
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }

        }
    }

    // 다이얼로그 표시
    fun showStopCameraDialog() {
        // 다이얼로그 생성
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.layout_stop_camera_dialog)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val width = (resources.displayMetrics.widthPixels * 0.5).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 취소 버튼
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 그만하기 버튼
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_confirm)
        btnConfirm.setOnClickListener {
            // 다이얼로그 닫기
            dialog.dismiss()
            moveToBack()
        }

        dialog.show()
    }


}