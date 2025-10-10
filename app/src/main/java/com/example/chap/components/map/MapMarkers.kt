package com.example.chap.components.map

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
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
    Card(
        modifier = Modifier
            .width(250.dp)
            .clickable { onDismiss() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9))
    ) {
        Box {
            Column(
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ヘッダ左: ユーザー名（空なら Figma の例に合わせて "Back"）
                    Text(
                        text = post.userName.ifBlank { "Back" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // ヘッダ右: 相対日時風のラベル（簡易表示）
                    Text(
                        text = relativeTimeJa(post.createdAt),
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.8f), thickness = 1.dp)

                // 本文
                Text(
                    text = post.content,
                    fontSize = 18.sp,
                    color = Color.Black,
                    lineHeight = 22.sp
                )
            }

            // 左上に重ねるアバター（外側にはみ出す）
            Image(
                painter = painterResource(id = R.drawable.chap_android),
                contentDescription = "user avatar",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-24).dp, y = (-24).dp)
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // 右上のピル（タグ or カテゴリ）
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(color = Color(0xFF9E9E9E), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = (post.category).lowercase(),
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
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
    Card(
        modifier = Modifier
            .width(250.dp)
            .clickable { onDismiss() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            Text(
                text = "THREAD: ${thread.category.uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when (thread.category.lowercase()) {
                    "entertainment" -> Color(0xFF9C27B0)
                    "disaster" -> Color(0xFFF44336)
                    "community" -> Color(0xFF4CAF50)
                    else -> Color(0xFF2196F3)
                },
                modifier = Modifier
                    .background(
                        when (thread.category.lowercase()) {
                            "entertainment" -> Color(0xFFE1BEE7)
                            "disaster" -> Color(0xFFFFCDD2)
                            "community" -> Color(0xFFC8E6C9)
                            else -> Color(0xFFBBDEFB)
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            
            Text(text = "by ${thread.userName}", fontSize = 12.sp, color = Color.Gray)
            Text(
                text = thread.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            }
            Image(
                painter = painterResource(id = R.drawable.chap_android),
                contentDescription = "user avatar",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * イベントの詳細ポップアップ
 */
@Composable
fun EventPopup(
    event: Event,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .clickable { onDismiss() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            Text(
                text = "EVENT: ${event.category.uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when (event.category.lowercase()) {
                    "entertainment" -> Color(0xFF9C27B0)
                    "disaster" -> Color(0xFFF44336)
                    "community" -> Color(0xFF4CAF50)
                    else -> Color(0xFF2196F3)
                },
                modifier = Modifier
                    .background(
                        when (event.category.lowercase()) {
                            "entertainment" -> Color(0xFFE1BEE7)
                            "disaster" -> Color(0xFFFFCDD2)
                            "community" -> Color(0xFFC8E6C9)
                            else -> Color(0xFFBBDEFB)
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            
            Text(text = "by ${event.userName}", fontSize = 12.sp, color = Color.Gray)
            Text(
                text = event.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            }
            Image(
                painter = painterResource(id = R.drawable.chap_android),
                contentDescription = "user avatar",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun SpotPopup(
    spot: Spot,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .clickable { onDismiss() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            Text(
                text = "SPOT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0),
                modifier = Modifier
                    .background(
                        Color(0xFFE1BEE7),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Text(
                text = spot.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            }
            Image(
                painter = painterResource(id = R.drawable.chap_android),
                contentDescription = "user avatar",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

