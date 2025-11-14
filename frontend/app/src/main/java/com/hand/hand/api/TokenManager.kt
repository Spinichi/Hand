package com.hand.hand.api

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "hand_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"

    private lateinit var prefs: SharedPreferences

    // 앱 시작 시 Application 클래스에서 한 번만 호출
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun loadToken(): String? {
        // 초기화가 되었는지 확인
        if (::prefs.isInitialized) {
            return prefs.getString(KEY_ACCESS_TOKEN, null)
        }
        return null
    }

    fun clearToken() {
        if (::prefs.isInitialized) {
            prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
        }
    }
}
