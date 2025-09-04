package com.example.chap.Screens.Comment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.AppTopBar
import com.example.chap.components.ui.BottomDestination
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.chap.API.CommentViewModel
import com.example.chap.API.ThreadViewModel
import com.example.chap.Models.Comment


@Composable
fun CommentScreen(
    commentViewModel: CommentViewModel,
    threadViewModel: ThreadViewModel,
    onNavigateHome: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateThread: () -> Unit,
    onNavigateEvent: () -> Unit,
) {

    val comments by commentViewModel.comments.collectAsState()
    LaunchedEffect(commentViewModel) {
        commentViewModel.load()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Threads",
            )
        },
        bottomBar = {
            AppBottomBar { dest ->
                when(dest){
                    BottomDestination.Home -> { onNavigateHome() }
                    BottomDestination.Map -> {onNavigateMap()}
                    BottomDestination.Event -> {onNavigateEvent()}
                    BottomDestination.Thread -> {onNavigateThread()}
                }
            }
        }
    ){ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CommentContent(
                threadViewModel = threadViewModel,
                commentViewModel = commentViewModel,
                comments = comments,
                reRoad = { commentViewModel.load() }
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CommentContent(
    commentViewModel: CommentViewModel,
    threadViewModel: ThreadViewModel,
    reRoad: () -> Unit,
    comments: List<Comment>
) {
    var replyText by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        ThreadHeader(
            comment = comments
        )
        AssistChip(
            onClick = {reRoad()},
            label = { Text("エラーが発生しました。リロードします。", color = Color.Red) },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Divider()
        // リスト + フォーム
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // スレッド本体 (番号1)
            items(comments) {item ->
                CommentRow(comment = item)
            }
            if (comments.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("まだレスがありません。最初のレスを投稿しましょう。", color = Color.Gray)
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                ReplyForm(
                    name = name,
                    onNameChange = { name = it },
                    content = replyText,
                    onContentChange = { replyText = it },
                    onSubmit = {
                        commentViewModel.createComment(replyText, name.ifBlank { null })
                        replyText = ""
                        name = ""
                    },
                    commenting = commenting
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ====== 個別 UI パーツ ======
@Composable
private fun ThreadHeader(comment: List<Comment>) {
    Column(
        Modifier
            .padding(16.dp)
            .background(Color(0xFFE6F0FF), shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            comment.content,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF0A2F66)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("レス数: $replyCount", fontSize = MaterialTheme.typography.labelSmall.fontSize, color = Color(0xFF1955A6))
        }
    }
}

@Composable
private fun CommentRow(
    comment: Comment
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                number.toString(),
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
            Text(
                (if (isOP) "★" else "") + "名無しさん@" + userId.take(8),
                fontFamily = FontFamily.Monospace,
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.width(8.dp))
            Text(formatDateTime(createdAt), color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            if (isOP) {
                Spacer(Modifier.width(8.dp))
                Text("[スレ主]", color = Color(0xFFB80000), style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Divider(thickness = 0.6.dp, color = Color(0xFFE0E0E0))
}

@Composable
private fun ReplyForm(
    name: String,
    onNameChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    commenting: Boolean
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color(0xFFF8F8F8), shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text("レスを書く", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("名前（省略可）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.5f)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("内容 *") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Row {
            Button(
                onClick = onSubmit,
                enabled = content.isNotBlank() && !commenting
            ) {
                Text(if (commenting) "投稿中..." else "投稿する")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = {
                onContentChange("")
                onNameChange("")
            }) {
                Text("クリア")
            }
        }
    }
}
