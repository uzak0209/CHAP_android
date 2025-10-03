package com.example.chap.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
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
import com.example.chap.models.Post
import com.example.chap.models.Spot
import com.example.chap.models.Thread
import com.example.chap.models.Event
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions


@Composable
fun PostPopup(
    post: Post,
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // カテゴリーバッジ
            Text(
                text = post.category.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when (post.category.lowercase()) {
                    "entertainment" -> Color(0xFF9C27B0)
                    "disaster" -> Color(0xFFF44336)
                    "community" -> Color(0xFF4CAF50)
                    else -> Color(0xFF2196F3)
                },
                modifier = Modifier
                    .background(
                        when (post.category.lowercase()) {
                            "entertainment" -> Color(0xFFE1BEE7)
                            "disaster" -> Color(0xFFFFCDD2)
                            "community" -> Color(0xFFC8E6C9)
                            else -> Color(0xFFBBDEFB)
                        },
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            
            // ユーザー名
            Text(
                text = "by ${post.username}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            // コンテンツ
            Text(
                text = post.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            
            // いいね数
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "❤️", fontSize = 14.sp)
                Text(
                    text = "${post.like}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
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
            
            Text(text = "by ${thread.username}", fontSize = 12.sp, color = Color.Gray)
            Text(
                text = thread.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
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
            
            Text(text = "by ${event.username}", fontSize = 12.sp, color = Color.Gray)
            Text(
                text = event.content,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SPOT: ${spot.category.uppercase()}",
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
    }
}
