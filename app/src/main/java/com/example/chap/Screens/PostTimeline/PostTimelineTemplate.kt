package com.example.chap.Screens.PostTimeline

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
import com.example.chap.Screens.PostTimeline.bindingmodel.PostBindingModel
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.BottomDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTimelineTemplate(
    postList: List<PostBindingModel>,
    iLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
){
    Scaffold(
        bottomBar = {
            AppBottomBar { dest ->
                when(dest){
                    BottomDestination.Home -> { /* TODO */ }
                    BottomDestination.Map -> { /* TODO navigate map */ }
                    BottomDestination.Event -> { /* TODO */ }
                    BottomDestination.Thread -> { /* Already here or TODO */ }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "タイムライン")
                },
            )
        }
    ){ innerPadding ->
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ){
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
            ) {
                items(postList) { item ->
                    PostRow(postBindingModel = item)
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