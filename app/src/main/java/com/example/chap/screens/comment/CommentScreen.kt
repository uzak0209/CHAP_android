package com.example.chap.screens.comment

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
import com.example.chap.components.ui.BottomDestination
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.models.Comment
import com.example.chap.models.RequestComment
import com.example.chap.models.Thread
import com.example.chap.screens.record.RecordTopBar


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentScreen(
    commentViewModel: CommentViewModel,
    thread: Thread,
    onNavigateBack: () -> Unit,
) {

    val comments by commentViewModel.comments.collectAsState()
    LaunchedEffect(commentViewModel) {
        commentViewModel.load(thread.id)
    }

    Scaffold{ innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val errorMessage by commentViewModel.errorMessage.collectAsState()
            CommentTopBar(
                onNavigateBack = onNavigateBack,
            )

            CommentContent(
                commentViewModel = commentViewModel,
                thread = thread,
                comments = comments,
                reRoad = { commentViewModel.load(thread.id) },
                errorMessage = errorMessage
            )
        }
    }
}






