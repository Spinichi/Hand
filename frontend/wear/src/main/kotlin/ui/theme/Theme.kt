
package ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import com.example.hand.wear.ui.theme.Typography


@Composable
fun HandTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = Typography, // ✅ 폰트 연결
        content = content
    )
}
