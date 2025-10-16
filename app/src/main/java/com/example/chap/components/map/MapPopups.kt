package com.example.chap.components.map

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.chap.R
import com.example.chap.models.Post
import com.example.chap.models.Spot
import com.example.chap.models.Thread
import com.example.chap.models.Event
import com.example.chap.ui.theme.BrandBlue
import com.example.chap.ui.theme.BrandPurple
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.Duration


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostPopup(
    post: Post,
    onDismiss: () -> Unit
) {
    PopupCardCommon(
        userName = post.userName,
        createdAt = post.createdAt,
        contentText = post.content,
        onDismiss = onDismiss
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun relativeTimeJa(iso: String): String {
    val fallback = iso.replace('T', ' ').take(10)
    return runCatching {
        val instant: Instant? = try {
            OffsetDateTime.parse(iso).toInstant()
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(iso).atZone(ZoneId.systemDefault()).toInstant()
            } catch (_: Exception) {
                null
            }
        }
        if (instant == null) return@runCatching fallback
        val seconds = Duration.between(instant, Instant.now()).seconds
        when {
            seconds < 60 -> "たった今"
            seconds < 3600 -> "${seconds / 60}分前"
            seconds < 86400 -> "${seconds / 3600}時間前"
            seconds < 86400 * 7 -> "${seconds / 86400}日前"
            else -> fallback
        }
    }.getOrElse { fallback }
}

@Composable
private fun PopupCardCommon(
    userName: String,
    createdAt: String,
    contentText: String,
    onDismiss: () -> Unit
) {
    Box {
        Card(
            modifier = Modifier
                .padding(start = 24.dp, top = 32.dp)
                .width(250.dp)
                .clickable { onDismiss() },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
            border = BorderStroke(1.dp, BrandBlue)
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userName.ifBlank { "Back" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = relativeTimeJa(createdAt),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
                Divider(
                    modifier = Modifier.offset(y = (-6).dp),
                    color = BrandBlue.copy(alpha = 0.8f),
                    thickness = 1.dp
                )
                Text(
                    modifier = Modifier.offset(y = (-6).dp),
                    text = contentText,
                    fontSize = 18.sp,
                    color = Color.Black,
                    lineHeight = 22.sp
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.chap_android),
            contentDescription = "user avatar",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 30.dp)
                .size(50.dp)
                .border(1.dp, BrandPurple, CircleShape)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun SpeechBubble(
    bubbleColor: Color = Color.White,
    arrowWidth: androidx.compose.ui.unit.Dp = 12.dp,
    arrowHeight: androidx.compose.ui.unit.Dp = 8.dp,
    lift: androidx.compose.ui.unit.Dp = 50.dp,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
        Spacer(modifier = Modifier.height(lift))
    }
}

/**
 * スレッドの詳細ポップアップ
 */
@Composable
fun ThreadPopup(
    thread: Thread,
    onDismiss: () -> Unit
) {
    PopupCardCommon(
        userName = thread.userName,
        createdAt = thread.createdAt,
        contentText = thread.content,
        onDismiss = onDismiss
    )
}

/**
 * イベントの詳細ポップアップ
 */
@Composable
fun EventPopup(
    event: Event,
    onDismiss: () -> Unit
) {
    PopupCardCommon(
        userName = event.userName,
        createdAt = event.createdAt,
        contentText = event.content,
        onDismiss = onDismiss
    )
}

@Composable
fun SpotPopup(
    spot: Spot,
    onDismiss: () -> Unit
) {
    PopupCardCommon(
        userName = spot.title,
        createdAt = spot.createdAt,
        contentText = spot.content,
        onDismiss = onDismiss
    )
}

