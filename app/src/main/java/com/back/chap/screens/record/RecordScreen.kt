package com.back.chap.screens.record

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.back.chap.components.EventItem
import com.back.chap.components.PostItem
import com.back.chap.components.SpotItem
import com.back.chap.components.ThreadItem
import com.back.chap.R


@Composable
fun RecordScreen(
    recordViewModel: RecordViewModel,
    onNavigateMap: () -> Unit,
    onNavigateComment: (String) -> Unit,
    onNavigateMapFocus: (String, String) -> Unit = { _, _ -> },
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Threads", "Events", "Spots")
    
    val posts by recordViewModel.posts.collectAsState()
    val threads by recordViewModel.threads.collectAsState()
    val events by recordViewModel.events.collectAsState()
    val spots by recordViewModel.spots.collectAsState()

    val currentUserId by recordViewModel.currentUserId.collectAsState()
    val currentUser by recordViewModel.currentUser.collectAsState()

    Log.i("名前", currentUser?.name?: "なにもない")
    Log.i("id", currentUser?.id?: "なにもない")


    LaunchedEffect(recordViewModel) {
        recordViewModel.load()
    }

    Scaffold{ padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            RecordTopBar(
                onNavigateMap = onNavigateMap,
            )
            // プロフィールセクション
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chap_android),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentUser?.name ?: "",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
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
                    0 -> items(posts.filter { it.userId == currentUserId.orEmpty() }) { post ->
                        PostItem(
                            post = post,
                            onClick = { p -> onNavigateMapFocus("post", p.id.toString()) }
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    1 -> items(threads.filter { it.userId == currentUserId.orEmpty() }) { thread ->
                        ThreadItem(
                            thread = thread,
                            onClick = { t -> onNavigateMapFocus("thread", t.id) },
                            onClickForum = {onNavigateComment(thread.id.toString()) }
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    2 -> items(events.filter { it.userId == currentUserId.orEmpty() }) { event ->
                        EventItem(
                            event = event,
                            onClick = { e -> onNavigateMapFocus("event", e.id.toString()) }
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    3 -> items(spots.filter { it.userId == currentUserId.orEmpty() }) { spot ->
                        SpotItem(
                            spot = spot,
                            onClick = { s -> onNavigateMapFocus("spot", s.id.toString()) }
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
