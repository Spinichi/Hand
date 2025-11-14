package com.hand.hand

import android.app.Application
import com.hand.hand.api.TokenManager

class HandApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 앱 시작 시 TokenManager 초기화
        TokenManager.initialize(this)
    }
}
