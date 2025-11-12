// file: com/hand/hand/ui/personal/PersonalCareActivity.kt
package com.hand.hand.ui.personal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hand.hand.ui.home.HomeActivity

class PersonalCareActivity : ComponentActivity() {

    private fun navigateHome() {
        // 기존 HomeActivity 인스턴스가 있으면 그걸 살리고 위의 스택을 정리
        val intent = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        // 옵션: 현재 화면은 닫아 깔끔하게
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalCareScreen(
                onHomeClick = { navigateHome() },
                onDocumentClick = { /* 현재 화면이 Document(개인 심리 분석)이면 noop */ },
                onDiaryClick = { /* TODO: 필요 시 연결 */ },
                onProfileClick = { /* TODO: 필요 시 연결 */ },
                onCareClick = { /* TODO: 중앙 버튼 */ }
            )
        }
    }
}
