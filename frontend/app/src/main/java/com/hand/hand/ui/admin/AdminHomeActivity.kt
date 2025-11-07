package com.hand.hand.ui.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*        // Column/Row/Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hand.hand.ui.admin.header.AdminGreetingHeader   // 헤더 임포트!

class AdminHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orgName = intent.getStringExtra("org_name") ?: "멀티캠퍼스"

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                AdminHomeScreen(orgName = orgName)
            }
        }
    }
}

@Composable
private fun AdminHomeScreen(orgName: String) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ⬇ AdminGreetingHeader(홈 헤더 시그니처) 에 맞춘 호출
        AdminGreetingHeader(
            dateText = "2025. 10. 27",
            onModeToggle = { /* TODO: 모드 전환 */ },
            userName = orgName.ifBlank { "관리자" },
            isWritten = false,
            heartRateBpm = 72,
            moodLabel = "Happy",
            recommendation = "깊고 천천히 숨쉬기",
            modifier = Modifier.fillMaxWidth()
        )

        // 이하 본문(섹션들 추가 예정)
        Text(
            text = "관리자 홈 본문 영역",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}
