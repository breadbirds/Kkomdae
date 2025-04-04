package com.pizza.kkomdae

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.pizza.kkomdae.data.source.local.SecureTokenManager
import com.pizza.kkomdae.data.source.local.TokenManager
import com.pizza.kkomdae.databinding.ActivityLoginBinding
import com.pizza.kkomdae.presenter.viewmodel.LoginViewModel
import com.pizza.kkomdae.util.RetrofitUtil.Companion.loginService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "LoginActivity"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel : LoginViewModel by viewModels()
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        // 토큰 확인 로직 추가
        val secureTokenManager = SecureTokenManager(this)
        val refreshToken = secureTokenManager.getRefreshToken()
        val tokenManager = TokenManager(this)
        val accessToken = tokenManager.getAccessToken()

        // 토큰 존재 여부 로그 출력
        Log.d(TAG, "onCreate: refreshToken=$refreshToken, accessToken=$accessToken")

        val loginButton = findViewById<Button>(R.id.btn_login)

        loginButton.setOnClickListener {

//            openCustomTab("https://project.ssafy.com/oauth/sso-check?client_id=811342aa-58e7-4430-9b48-18ef1108d783&response_type=code&redirect_uri=http://localhost:8080/api/sso/login")
            binding.wvLogin.visibility= View.VISIBLE
            binding.vLogin.visibility = View.VISIBLE
            binding.wvLogin.loadUrl("https://project.ssafy.com/oauth/sso-check?client_id=811342aa-58e7-4430-9b48-18ef1108d783&response_type=code&redirect_uri=https://j12d101.p.ssafy.io/api/sso/login")
        }

//        handleIntent(intent)

        //binding.wvLogin.webViewClient= WebViewClient()
        // 웹뷰 로드 시 페이지 너비에 맞추기
        binding.wvLogin.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // 페이지 로드 완료 후 화면에 맞추기
                view?.loadUrl("javascript:document.body.style.margin='0'; document.body.style.padding='0';")
                view?.loadUrl("javascript:var meta = document.createElement('meta'); meta.setAttribute('name', 'viewport'); meta.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'); document.getElementsByTagName('head')[0].appendChild(meta);")
            }
        }
        //  binding.wvLogin.settings.javaScriptEnabled = true
        binding.wvLogin.settings.apply {
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptEnabled = true
            domStorageEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
        }
        binding.wvLogin.setInitialScale(1)

        // SSO 로그인 페이지 로드
        // 커스텀 WebViewClient 설정
        binding.wvLogin.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                Log.d("WebView", "URL: $url")

                // 리다이렉트 URL을 확인하여 인증 코드 추출
                if (url.startsWith("https://j12d101.p.ssafy.io/api/sso/login")) {
                    val uri = Uri.parse(url)
                    val code = uri.getQueryParameter("code")
                    if (code != null) {
                        Log.d("SSO_CODE", "인증 코드: $code")

                        // WebView 숨기고 로딩 화면 표시
                        binding.wvLogin.isVisible = false
                        binding.vLogin.isVisible = false
                        binding.clLoading.isVisible = true

                        // 로그인 처리
                        getLogin(code)
                        return true // URL 로딩 중단
                    }
                }

                // 기본적으로 WebView에서 URL 로드 계속
                return false
            }
        }


        if(tokenManager.getAccessToken()!=null){
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.wvLogin.settings.javaScriptEnabled = true
        binding.wvLogin.settings.domStorageEnabled = true // 로컬 스토리지 지원
        binding.wvLogin.settings.userAgentString = "Mozilla/5.0 (Android)" // 유저 에이전트 설정

        viewModel.loginResult.observe(this){ result->
            result.onSuccess { loginResponse ->
                // 로그인 성공 시 실제 데이터 처리
                loginResponse?.let {
                        saveRefreshToken(it.jwt, it.refreshToken)
                    }

                    // 로딩 후 메인 액티비티로 이동
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
            }.onFailure { exception ->
                // 로그인 실패 시 오류 처리
                binding.clLoading.isVisible = false
                    Toast.makeText(this@LoginActivity, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 로그인
    fun getLogin(code: String){

        viewModel.login(code)
//        lifecycleScope.launch {
//            try {
//                val response = loginService.getLogin(code)
//                if (response.isSuccessful){
//                    Log.d(TAG, "getLogin: ${response.body()}")
//                    val data = response.body()
//                    data?.let {
//                        saveRefreshToken(it.jwt, it.refreshToken)
//                    }
//
//                    // 로딩 후 메인 액티비티로 이동
//                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    // 로그인 실패 시 로딩 화면 숨기기
//                    binding.clLoading.isVisible = false
//                    Log.d(TAG, "getLogin: $response")
//                    Toast.makeText(this@LoginActivity, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                // 예외 발생 시 로딩 화면 숨기기
//                binding.clLoading.isVisible = false
//                Log.d(TAG, "getLogin: $e")
//                Toast.makeText(this@LoginActivity, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    fun saveRefreshToken(jwt:String, refreshToken: String){
        val secureTokenManager = SecureTokenManager(this)
        secureTokenManager.saveRefreshToken(refreshToken)

        // TokenManager 초기화
        val tokenManager = TokenManager(this)
        // 액세스 토큰 저장
        tokenManager.saveAccessToken(jwt)

    }



    // 새 인텐트가 도착했을 때 호출됨 (앱이 이미 실행 중일 때)
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        handleIntent(intent)
//    }

//    private fun handleIntent(intent: Intent) {
//        val data: Uri? = intent.data
//        if (data != null && data.scheme == "com.pizza.kkomdae" && data.host == "callback") {
//            val code = data.getQueryParameter("code")
//            if (code != null) {
//                Log.d("SSO_CODE", "인증 코드: $code")
//
//                // 코드를 사용하여 메인 액티비티로 이동
//                val mainIntent = Intent(this, MainActivity::class.java)
//                mainIntent.putExtra("auth_code", code)
//                startActivity(mainIntent)
//                finish()
//            }
//        }
//    }
//
//    private fun openCustomTab(url: String) {
//        val customTabsIntent = CustomTabsIntent.Builder().build()
//        customTabsIntent.launchUrl(this, Uri.parse(url))
//    }

}