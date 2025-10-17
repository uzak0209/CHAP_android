package com.back.chap.screens.comment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.back.chap.models.Thread


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






