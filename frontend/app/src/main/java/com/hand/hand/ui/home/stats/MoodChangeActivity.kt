// file: com/hand/hand/ui/home/stats/MoodChangeActivity.kt
package com.hand.hand.ui.home.stats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MoodChangeActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_COUNT = "extra_mood_change_count"

        // 기존 시그니처 유지
        fun intent(ctx: Context): Intent =
            Intent(ctx, MoodChangeActivity::class.java)

        // 👇 count까지 넘기는 오버로드 추가
        fun intent(ctx: Context, count: Int): Intent =
            Intent(ctx, MoodChangeActivity::class.java)
                .putExtra(EXTRA_COUNT, count)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val count = intent.getIntExtra(EXTRA_COUNT, /*default*/ 0)

        setContent {
            MoodChangeScreen(
                moodChangeCount = count,  // 전달된 값 사용
                onBack = { finish() }
            )
        }
    }
}
