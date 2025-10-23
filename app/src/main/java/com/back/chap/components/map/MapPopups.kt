package com.back.chap.components.map

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.back.chap.R
import com.back.chap.models.Event
import com.back.chap.models.Post
import com.back.chap.models.Spot
import com.back.chap.models.Thread
import com.back.chap.ui.theme.BrandBlue
import com.back.chap.ui.theme.BrandPurple
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

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
        imageUrl = post.image,
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PopupCardCommon(
    userName: String,
    createdAt: String,
    contentText: String,
    imageUrl: String,
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
                        text = userName.ifBlank { "Unknown" },
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
                if(imageUrl != ""){
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }else{
                    Log.i("画像がありません", "画像がありません")
                }
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
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ThreadPopup(
    thread: Thread,
    onDismiss: () -> Unit
) {
    PopupCardCommon(
        userName = thread.userName,
        createdAt = thread.createdAt,
        contentText = thread.content,
        imageUrl = thread.image,
        onDismiss = onDismiss
    )
}

/**
 * イベントの詳細ポップアップ
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventPopup(
    event: Event,
    onDismiss: () -> Unit
) {
    PopupCardCommon(
        userName = event.userName,
        createdAt = event.createdAt,
        contentText = event.content,
        imageUrl = event.image,
        onDismiss = onDismiss
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SpotPopup(
    spot: Spot,
    onDismiss: () -> Unit
) {
    PopupCardCommon(
        userName = spot.title,
        createdAt = spot.createdAt,
        contentText = spot.content,
        imageUrl = "",
        onDismiss = onDismiss
    )
}

