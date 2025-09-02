package com.example.chap.components.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chap.Models.Event
import com.example.chap.domain.model.Post
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SubmitRow(
    modifier: Modifier = Modifier,
    post: Post? = null,
    thread: Thread? = null,
    event: Event? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2F2F2F))
    ) {
        Column(Modifier.padding(16.dp)) {
            SubmitHeader(username = post?.username ?:" username" )
            SubmitContent(content = post?.content ?: "Content", images = emptyList())
            if (post?.category?.isNotBlank() == true && post.category != "entertainment") {
                SubmitCategoryDisplay(category = post.category)
            }
            SubmitFooter(createdAt = post?.created_at ?: "created_at")
        }
    }
}

@Composable
private fun SubmitHeader(username: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = username,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SubmitContent(content: String, images: List<String>?) {
    Column {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = if (images.isNullOrEmpty()) 12.dp else 12.dp)
        )
    }
}

@Composable
private fun SubmitCategoryDisplay(category: String) {
    val (bg, fg) = when (category) {
        "community" -> Color(0xFFE3F2FD) to Color(0xFF0D47A1)
        "disaster" -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
        "entertainment" -> Color(0xFFFFE4F1) to Color(0xFFAD1457)
        else -> Color(0xFFF3F4F6) to Color(0xFF374151)
    }
    val label = when (category) {
        "community" -> "地域住民コミュニケーション"
        "disaster" -> "災害情報"
        "entertainment" -> "エンターテイメント"
        else -> category
    }
    Row(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SubmitFooter(createdAt: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = formatTime(createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(isoString: String): String {
    return try {
    // isoString は各 Post の created_at 値
    val odt = OffsetDateTime.parse(isoString)
        val now = OffsetDateTime.now(ZoneId.systemDefault())
        val minutes = Duration.between(odt, now).toMinutes()
        when {
            minutes < 1 -> "たった今"
            minutes < 60 -> "${minutes}分前"
            minutes < 60 * 24 -> "${minutes / 60}時間前"
            minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}日前"
            else -> odt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        }
    } catch (e: Exception) {
        isoString
    }
}
