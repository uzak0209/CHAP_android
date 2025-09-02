package com.example.chap.Screens.PostTimeline

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.AppTopBar
import com.example.chap.components.ui.BottomDestination
import com.example.chap.components.ui.SubmitRow
import com.example.chap.domain.model.Post

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTimelineTemplate(
    postList: List<Post>,
    onNavigateThread: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateEvent: () -> Unit,
    iLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
){
    // 上位 (Navigation) で共通 BottomBar を提供するためここでは純粋なコンテンツのみ
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Home",
            )
        },
        bottomBar = {
            AppBottomBar { dest ->
                when(dest){
                    BottomDestination.Home -> { /* already */ }
                    BottomDestination.Map -> {onNavigateMap()}
                    BottomDestination.Event -> {onNavigateEvent()}
                    BottomDestination.Thread -> {onNavigateThread()}
                }
            }
        }
    ){innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
            ) {
                items(postList) { item ->
                    SubmitRow(post = item)
                }
            }
        }
    }
}

//@Preview
//@Composable
//private fun PostTimelineTemplatePreview() {
//    ChapTheme {
//        Surface {
//            PostTimelineTemplate(
//                postList = listOf(
//                    PostBindingModel(
//                        id = "id1",
//                        displayName = "display name1",
//                        username = "username1",
//                        avatar = null,
//                        content = "preview content1",
//                        attachmentImageList = listOf()
//                    ),
//                    PostBindingModel(
//                        id = "id2",
//                        displayName = "display name2",
//                        username = "username2",
//                        avatar = null,
//                        content = "preview content2",
//                        attachmentImageList = listOf()
//                    ),
//                ),
//                isLoading = true,
//                isRefreshing = false,
//                onRefresh = {}
//            )
//        }
//    }
//}