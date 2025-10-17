package com.back.chap.screens.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.back.chap.models.Comment
import com.back.chap.models.Thread

@Composable
public fun ThreadHeader(comment: List<Comment>, thread: Thread) {
    Column(
        Modifier
            .padding(16.dp)
            .background(Color(0xFFE6F0FF), shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            thread.content,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF0A2F66)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "レス数: ${comment.size}",
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                color = Color(0xFF1955A6)
            )
        }
    }
}