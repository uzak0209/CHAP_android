package com.example.chap.Screens.PostTimeline

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.API.EventViewModel
import com.example.chap.API.PostViewModel
import com.example.chap.API.ThreadViewModel
import com.example.chap.Models.CreateKind
import com.example.chap.R
import com.example.chap.components.CreateDialog
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.BottomDestination
import com.example.chap.components.ui.AppHeader
import com.example.chap.components.ui.SearchBar

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
    var showCreate by remember { mutableStateOf(false) }


    // 上位 (Navigation) で共通 BottomBar を提供するためここでは純粋なコンテンツのみ
    Scaffold(
        bottomBar = {
            AppBottomBar { dest ->
                when(dest){
                    BottomDestination.Home -> { /* already */ }
                    BottomDestination.Map -> {onNavigateMap()}
                    BottomDestination.Event -> {onNavigateEvent()}
                    BottomDestination.Thread -> { onNavigateThread()}
                }
            }
        }
    ){innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppHeader(
                title = "Home",
                color = Color(0xFF9DC4FF)
            )
            SearchBar(
                color = Color(0xFF9DC4FF),
                searchTarget= "message"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ){
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
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    onClick = { showCreate = true },
                    containerColor = Color(0xFFFF7272)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_calendar_today_24),
                        contentDescription = "Create post",
                        tint = Color.White
                    )
                }
            }
        }
    }
    CreateDialog(
        isOpen = showCreate,
        onClose = { showCreate = false },
        selectedKind = CreateKind.EVENT,
        postViewModel = postViewModel,
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


