package com.pizza.kkomdae

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pizza.kkomdae.databinding.ActivityMainBinding
import com.pizza.kkomdae.ui.MainFragment
import com.pizza.kkomdae.ui.step4.Step4AiResultFragment
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.pizza.kkomdae.data.source.local.SecureTokenManager
import com.pizza.kkomdae.data.source.local.TokenManager
import com.pizza.kkomdae.databinding.LayoutLogoutDialogBinding
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.pizza.kkomdae.presenter.viewmodel.CameraViewModel
import com.pizza.kkomdae.presenter.viewmodel.FinalViewModel
import com.pizza.kkomdae.presenter.viewmodel.MainViewModel
import com.pizza.kkomdae.ui.guide.Step2GuideFragment
import dagger.hilt.android.AndroidEntryPoint
private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel : FinalViewModel by viewModels()

    private val REQUEST_CAMERA_PERMISSION = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.popBackStack()
        transaction.replace(R.id.fl_main, MainFragment())

        transaction.commit()
        checkCameraPermission()



        val secureTokenManager = SecureTokenManager(this)
        val refreshToken = secureTokenManager.getRefreshToken()
        Log.d(TAG, "onCreate: $refreshToken")

        val tokenManager = TokenManager(this)
        val accessToken = tokenManager.getAccessToken()
        Log.d(TAG, "onCreate: $accessToken")
    }

    fun logout() {
        val dialogBinding = LayoutLogoutDialogBinding.inflate(layoutInflater)

        // 커스텀 다이얼로그 생성
        val customDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        customDialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.75).toInt()
            customDialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        // 취소 버튼 클릭 리스너
        dialogBinding.btnCancel.setOnClickListener {
            customDialog.dismiss()
        }

        // 확인 버튼 클릭 리스너
        dialogBinding.btnConfirm.setOnClickListener {
            customDialog.dismiss()
            performLogout()
        }

        customDialog.show()
    }

    private fun performLogout() {
        // 저장된 토큰 삭제
        val secureTokenManager = SecureTokenManager(this)
        secureTokenManager.deleteRefreshToken()

        val tokenManager = TokenManager(this)
        tokenManager.clearAccessToken()

        // 토큰이 삭제되었는지 확인
        val refreshTokenAfterLogout = secureTokenManager.getRefreshToken()
        val accessTokenAfterLogout = tokenManager.getAccessToken()
        Log.d("MainActivity", "로그아웃 후 리프레시 토큰: $refreshTokenAfterLogout")
        Log.d("MainActivity", "로그아웃 후 액세스 토큰: $accessTokenAfterLogout")

        // WebView 캐시 및 쿠키 삭제
        android.webkit.WebStorage.getInstance().deleteAllData()
        android.webkit.CookieManager.getInstance().removeAllCookies(null)
        android.webkit.CookieManager.getInstance().flush()

        // 로그인 화면으로 이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        showToast("로그아웃 되었습니다")
    }

    fun next(){
       val intent = Intent(this, CameraActivity::class.java)
        cameraResultLauncher.launch(intent)
    }

    fun reCamera(stage: Int){
        val intent = Intent(this, CameraActivity::class.java)
            .putExtra("stage",stage)
        reCameraResultLauncher.launch(intent)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            // ✅ 카메라 권한이 이미 허용된 경우, CameraActivity 실행

        } else {
            // ❌ 권한이 없으면 사용자에게 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // ✅ 사용자가 권한을 허용했을 경우, CameraActivity 실행

            } else {
                // ❌ 사용자가 권한을 거부했을 경우
                showToast("카메라 권한이 필요합니다.")
            }
        }
    }

    private val cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val photoUri = result.data?.getIntExtra("PHOTO_URI",0)
            if(photoUri==1){
                val transaction = supportFragmentManager.beginTransaction()
                supportFragmentManager.popBackStack()
                transaction.replace(R.id.fl_main, Step2GuideFragment())
                    .addToBackStack("")
                transaction.commit()
                checkCameraPermission()
            }
        }
    }


    private val reCameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val photoUri= result.data?.getStringExtra("RE_PHOTO_URI")
            if (photoUri != null) {
                Log.d(TAG, "recameraaa $photoUri: ")

                viewModel.setReCameraUri(Uri.parse(photoUri))
            }

        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



}