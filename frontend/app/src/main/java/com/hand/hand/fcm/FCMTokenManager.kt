package com.hand.hand.fcm

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * FCM 토큰 저장 및 관리
 */
object FCMTokenManager {
    private const val TAG = "FCMTokenManager"
    private const val PREF_NAME = "fcm_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"

    /**
     * FCM 토큰 저장
     */
    fun saveToken(context: Context, token: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        Log.d(TAG, "FCM 토큰 저장됨: ${token.take(20)}...")
    }

    /**
     * 저장된 FCM 토큰 가져오기
     */
    fun getToken(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    /**
     * FCM 토큰 삭제 (로그아웃 시 사용)
     */
    fun clearToken(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_FCM_TOKEN).apply()
        Log.d(TAG, "FCM 토큰 삭제됨")
    }
}
