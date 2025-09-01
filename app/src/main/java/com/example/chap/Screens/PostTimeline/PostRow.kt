package com.example.chap.Screens.PostTimeline

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chap.Screens.PostTimeline.bindingmodel.PostBindingModel
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PostRow(postBindingModel: PostBindingModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            PostHeader(username = postBindingModel.username)
            PostContent(content = postBindingModel.content, images = emptyList())
            if (postBindingModel.category.isNotBlank() && postBindingModel.category != "entertainment") {
                PostCategoryDisplay(category = postBindingModel.category)
            }
            PostFooter(createdAt = postBindingModel.created_at)
        }
    }
}

@Composable
private fun PostHeader(username: String) {
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
private fun PostContent(content: String, images: List<String>?) {
    Column {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = if (images.isNullOrEmpty()) 12.dp else 12.dp)
        )
    }
}

@Composable
private fun PostCategoryDisplay(category: String) {
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

@Composable
private fun PostFooter(createdAt: String) {
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

private fun formatTime(isoString: String): String {
    return try {
    // isoString は各 PostBindingModel の created_at 値
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
