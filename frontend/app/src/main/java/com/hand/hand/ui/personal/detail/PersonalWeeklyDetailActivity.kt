package com.hand.hand.ui.personal.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hand.hand.ui.home.HomeActivity

class PersonalWeeklyDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val year  = intent.getIntExtra(EXTRA_YEAR, 2025)
        val month = intent.getIntExtra(EXTRA_MONTH, 10)
        val week  = intent.getIntExtra(EXTRA_WEEK, 1)

        setContent {
            PersonalWeeklyDetailScreen(
                year = year, month = month, weekIndex = week,
                onBack = { finish() },
                onHomeClick = {
                    // 홈으로
                    val i = Intent(this, HomeActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(i)
                },
                // 필요 시 다른 탭 라우팅도 넣어주면 됨
                onDiaryClick = { /* TODO */ },
                onDocumentClick = { /* TODO */ },
                onProfileClick = { /* TODO */ },
                onCareClick = { /* 현재 화면 유지 */ }
            )
        }
    }

    companion object {
        private const val EXTRA_YEAR = "year"
        private const val EXTRA_MONTH = "month"
        private const val EXTRA_WEEK = "week"

        fun intent(context: Context, year: Int, month: Int, weekIndex: Int): Intent =
            Intent(context, PersonalWeeklyDetailActivity::class.java).apply {
                putExtra(EXTRA_YEAR, year)
                putExtra(EXTRA_MONTH, month)
                putExtra(EXTRA_WEEK, weekIndex)
            }
    }
}
