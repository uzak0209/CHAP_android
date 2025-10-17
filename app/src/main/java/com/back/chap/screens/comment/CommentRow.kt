package com.back.chap.screens.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.back.chap.models.Comment

@Composable
public fun CommentRow(
    comment: Comment,
    isOP: Boolean
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                comment.userName,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .background(
                        if (isOP) Color(0xFFFFE5E5) else Color(0xFFF0F0F0),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = if (isOP) Color(0xFFB80000) else Color(0xFF555555),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.width(8.dp))
            Text(comment.createdAt, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            if (isOP) {
                Spacer(Modifier.width(8.dp))
                Text("[スレ主]", color = Color(0xFFB80000), style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            comment.content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    androidx.compose.material3.HorizontalDivider(thickness = 0.6.dp, color = Color(0xFFE0E0E0))
}