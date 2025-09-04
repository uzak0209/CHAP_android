package com.example.chap.Screens.Comment

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun CommentScreen(
    threadId: String,
    threadViewModel: ThreadViewModel,
    onNavigateHome: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateThread: () -> Unit,
    onNavigateEvent: () -> Unit,
) {

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
            when {
                ui.loading -> LoadingView()
                ui.thread == null -> EmptyView(onBack)
                else -> CommentContent(
                    thread = ui.thread,
                    replies = ui.replies,
                    posting = ui.posting,
                    error = ui.error,
                    onRetry = controller.reload,
                    onPost = controller.postReply
                )
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyView(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("スレッドが見つかりません", color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) { Text("戻る") }
    }
}

@Composable
private fun CommentContent(
    thread: Thread,
    replies: List<Post>,
    posting: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onPost: (String, String?) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        ThreadHeader(thread = thread, replyCount = replies.size)
        if (error != null) {
            AssistChip(
                onClick = onRetry,
                label = { Text("エラー: $error (再試行)") },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        Divider()
        // リスト + フォーム
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // スレッド本体 (番号1)
            item {
                ThreadResponseRow(
                    number = 1,
                    content = thread.content,
                    userId = thread.userId,
                    createdAt = thread.createdAt,
                    isOP = true
                )
            }
            // レス
            itemsIndexed(replies) { index, post ->
                ThreadResponseRow(
                    number = index + 2,
                    content = post.content,
                    userId = post.userId,
                    createdAt = post.createdAt,
                    isOP = false
                )
            }
            if (replies.isEmpty()) {
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
                        onPost(replyText, name.ifBlank { null })
                        replyText = ""
                        name = ""
                    },
                    posting = posting
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ====== 個別 UI パーツ ======
@Composable
private fun ThreadHeader(thread: Thread, replyCount: Int) {
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
            Text("レス数: $replyCount", fontSize = MaterialTheme.typography.labelSmall.fontSize, color = Color(0xFF1955A6))
        }
    }
}

@Composable
private fun ThreadResponseRow(
    number: Int,
    content: String,
    userId: String,
    createdAt: String,
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
    posting: Boolean
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
                enabled = content.isNotBlank() && !posting
            ) {
                Text(if (posting) "投稿中..." else "投稿する")
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
