package com.example.chap.Screens.PostTimeline

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.setValue
import com.example.chap.R
import com.example.chap.API.PostViewModel
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.AppTopBar
import com.example.chap.components.ui.BottomDestination
import com.example.chap.components.ui.SubmitRow
import com.example.chap.Models.Post
import com.example.chap.domain.repository.PostRepository
import com.example.chap.domain.repository.PostRepositoryImpl

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTimelineTemplate(
    postViewModel: PostViewModel,
    onNavigateThread: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateEvent: () -> Unit,
){
    val posts by postViewModel.posts.collectAsState()
    LaunchedEffect(postViewModel, posts.isEmpty()) {
        if (posts.isEmpty()) {
            postViewModel.load()
        }
    }

    // 上位 (Navigation) で共通 BottomBar を提供するためここでは純粋なコンテンツのみ
    Scaffold(
        topBar = {
            HomeHeader()
        },
        bottomBar = {
            AppBottomBar { dest ->
                when(dest){
                    BottomDestination.Home -> { /* already */ }
                    BottomDestination.Map -> {onNavigateMap()}
                    BottomDestination.Event -> {onNavigateEvent()}
                    BottomDestination.Thread -> { /* Thread一覧への遷移など別用途ならここで処理 */ }
                }
            }
        }
    ){innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(posts) { item ->
                    MessageRow(
                        title = item.username,
                        subtitle = item.content,
                        dateText = item.created_at.take(10)
                    )
                    Divider(color = Color(0xFFE8ECF0))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF9DC4FF))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.chap_android),
                contentDescription = "App",
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Text(
                text = "Home",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(modifier: Modifier = Modifier) {
    var keyword by remember { mutableStateOf("") }
    TextField(
        value = keyword,
        onValueChange = { keyword = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp)),
        placeholder = { Text("Search for the message") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF7D8790)) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE9EEF3),
            unfocusedContainerColor = Color(0xFFE9EEF3),
            disabledContainerColor = Color(0xFFE9EEF3),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun MessageRow(
    title: String,
    subtitle: String,
    dateText: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1F2933))
            Text(
                text = subtitle,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(text = dateText, color = Color(0xFF9AA1A9), fontSize = 12.sp)
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