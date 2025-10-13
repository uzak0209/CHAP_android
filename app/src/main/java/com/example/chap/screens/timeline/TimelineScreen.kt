package com.example.chap.screens.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.R
import com.example.chap.components.EventItem
import com.example.chap.components.PostItem
import com.example.chap.components.ThreadItem
import com.example.chap.models.Post
import com.example.chap.models.Thread
import com.example.chap.models.Event

@Composable
fun TimelineScreen(
    timelineViewModel: TimelineViewModel,
    onNavigateMap: () -> Unit,
    onNavigateComment: (String) -> Unit,
    onNavigateMapFocus: (String, String) -> Unit = { _, _ -> },
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Threads", "Events")

    val posts by timelineViewModel.posts.collectAsState()
    val threads by timelineViewModel.threads.collectAsState()
    val events by timelineViewModel.events.collectAsState()

    LaunchedEffect(
        timelineViewModel
    ) {
        timelineViewModel.load()
      }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            TimelineTopBar(
                onNavigateMap = onNavigateMap,
            )
            
            // タブセクション
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color.Black,
                divider = {
                    Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            )
                        }
                    )
                }
            }
            
            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
            
            // コンテンツリスト
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    0 -> items(posts) { post ->
                        PostItem(
                            post = post,
                            onClick = { p -> onNavigateMapFocus("post", p.id.toString()) }
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    1 -> items(threads) { thread ->
                        ThreadItem(
                            thread = thread,
                            onClick = { t -> onNavigateMapFocus("thread", t.id) },
                            onClickForum = { onNavigateComment(thread.id.toString())}
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    2 -> items(events) { event ->
                        EventItem(
                            event = event,
                            onClick = { e -> onNavigateMapFocus("event", e.id.toString()) },
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

