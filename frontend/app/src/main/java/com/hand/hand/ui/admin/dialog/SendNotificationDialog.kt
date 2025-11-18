package com.hand.hand.ui.admin.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.ui.theme.BrandFontFamily
import com.hand.hand.ui.theme.Brown80

@Composable
fun SendNotificationDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onSend: (title: String, body: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "$memberName 님에게 알림 보내기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BrandFontFamily,
                color = Brown80
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목", fontFamily = BrandFontFamily) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("내용", fontFamily = BrandFontFamily) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onSend(title, body)
                    }
                },
                enabled = title.isNotBlank() && body.isNotBlank()
            ) {
                Text(
                    "전송",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "취소",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
