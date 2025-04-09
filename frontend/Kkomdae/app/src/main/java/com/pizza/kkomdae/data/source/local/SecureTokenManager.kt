package com.pizza.kkomdae.data.source.local
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class SecureTokenManager(private val context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }
    private val keyAlias = "RefreshTokenKey"
    private val SHARED_PREFS_FILENAME = "secure_tokens"
    private val TOKEN_PREFERENCE_KEY = "refresh_token"
    private val IV_PREFERENCE_KEY = "refresh_token_iv"

    init {
        // 키가 없으면 생성
        if (!keyStore.containsAlias(keyAlias)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false) // 사용 사례에 따라 true로 설정 가능
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    // 리프레시 토큰 저장
    fun saveRefreshToken(token: String) {
        val cipher = Cipher.getInstance(
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        )
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val encryptedBytes = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)



        // IV도 저장해야 복호화 가능
        val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)

        // SharedPreferences에 저장
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(TOKEN_PREFERENCE_KEY, encryptedBase64)
            .putString(IV_PREFERENCE_KEY, iv)
            .apply()
    }

    // 리프레시 토큰 검색
    fun getRefreshToken(): String? {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)
        val encryptedToken = sharedPrefs.getString(TOKEN_PREFERENCE_KEY, null) ?: return null
        val iv = sharedPrefs.getString(IV_PREFERENCE_KEY, null) ?: return null

        return try {
            val cipher = Cipher.getInstance(
                "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
            )

            val ivSpec = GCMParameterSpec(
                128, // 태그 길이 비트 수
                Base64.decode(iv, Base64.DEFAULT)
            )

            cipher.init(Cipher.DECRYPT_MODE, getKey(), ivSpec)
            val decodedToken = Base64.decode(encryptedToken, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedToken)

            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 리프레시 토큰 삭제
    fun deleteRefreshToken() {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .remove(TOKEN_PREFERENCE_KEY)
            .remove(IV_PREFERENCE_KEY)
            .apply()
    }

    private fun getKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }
}
