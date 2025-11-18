package com.hand.hand.ui.admin.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFFFFFBF8),
        tonalElevation = 8.dp,
        title = {
            Text(
                text = "$memberName 님에게 알림 보내기",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BrandFontFamily,
                color = Brown80
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            "제목",
                            fontFamily = BrandFontFamily,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF8834),
                        unfocusedBorderColor = Color(0xFFE0DDD9),
                        focusedLabelColor = Color(0xFFEF8834),
                        cursorColor = Color(0xFFEF8834)
                    )
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = {
                        Text(
                            "내용",
                            fontFamily = BrandFontFamily,
                            fontSize = 14.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF8834),
                        unfocusedBorderColor = Color(0xFFE0DDD9),
                        focusedLabelColor = Color(0xFFEF8834),
                        cursorColor = Color(0xFFEF8834)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onSend(title, body)
                    }
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF8834),
                    disabledContainerColor = Color(0xFFE0DDD9),
                    contentColor = Color.White,
                    disabledContentColor = Color(0xFFB0A8A4)
                )
            ) {
                Text(
                    "전송",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF867E7A)
                )
            ) {
                Text(
                    "취소",
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }
    )
}
