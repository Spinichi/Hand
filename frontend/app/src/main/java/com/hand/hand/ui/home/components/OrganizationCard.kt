package com.hand.hand.ui.home.components

import androidx.compose.material3.Icon
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hand.hand.R
import com.hand.hand.ui.theme.*


@Composable
fun OrganizationCard(
    @DrawableRes moodIconRes: Int,
    moodBg: Color,
    title: String,
    count: Int,
    titleColor: Color,
    metaColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 감정 아이콘
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(moodBg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = moodIconRes),
                    contentDescription = null,
                    modifier = Modifier.size(45.dp),
                    contentScale = ContentScale.Fit     // OK
                    // tint 주지 말기
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(text = title, color = titleColor, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = BrandFontFamily)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_meta_human_gray),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = Color.Unspecified
                    )

                    Spacer(Modifier.width(6.dp))
                    Text("${count}명", fontSize = 15.sp, color = metaColor,fontWeight = FontWeight.Medium, fontFamily = BrandFontFamily)
                }
            }

            Icon(
                painter = painterResource(R.drawable.chevron_right),
                contentDescription = "이동",
                tint = Color.Unspecified,
                modifier = Modifier.size(30.dp)   // 피그마 30
            )
        }
    }


}

