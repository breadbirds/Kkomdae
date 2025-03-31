package com.pizza.kkomdae.data.source.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    companion object {
        private const val PREFERENCES_FILE = "secure_token_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
    }

    init {
        // 마스터 키 생성
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // EncryptedSharedPreferences 인스턴스 생성
        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREFERENCES_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // 액세스 토큰 저장
    fun saveAccessToken(token: String) {
        sharedPreferences.edit()
            .putString(ACCESS_TOKEN_KEY, token)
            .apply()
    }

    // 액세스 토큰 검색
    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    // 액세스 토큰 삭제 (로그아웃 시)
    fun clearAccessToken() {
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .apply()
    }

    // 모든 저장된 데이터 삭제
    fun clearAll() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}