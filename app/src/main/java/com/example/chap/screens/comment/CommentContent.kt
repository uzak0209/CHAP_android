package com.example.chap.screens.comment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chap.models.Comment
import com.example.chap.models.RequestComment
import com.example.chap.models.Thread

@RequiresApi(Build.VERSION_CODES.O)
@Composable
public fun CommentContent(
    commentViewModel: CommentViewModel,
    thread: Thread,
    reRoad: () -> Unit,
    comments: List<Comment>,
    errorMessage: String?
) {
    var replyText by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val posting by commentViewModel.isPosting.collectAsState()

    Column(Modifier.fillMaxSize()) {
        ThreadHeader(
            comment = comments,
            thread = thread
        )
        if (errorMessage != null) {
            AssistChip(
                onClick = { reRoad() },
                label = { Text(errorMessage, color = Color.Red) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        androidx.compose.material3.HorizontalDivider()
        // リスト + フォーム
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // スレッド本体 (番号1)
            items(comments) { item ->
                val isOP = item.userId == thread.userId
                CommentRow(comment = item, isOP = isOP)
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
                        val comment = RequestComment(
                            content = replyText.trim(),
                            threadId = thread.id,
                        )
                        commentViewModel.createComment(comment)
                        replyText = ""
                        name = ""
                    },
                    commenting = posting

                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}