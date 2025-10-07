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
import com.example.chap.models.Post
import com.example.chap.models.Thread
import com.example.chap.models.Event

@Composable
fun TimelineScreen(
    timelineViewModel: TimelineViewModel,
    onNavigateMap: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Threads", "Events")

    val posts by timelineViewModel.posts.collectAsState()
    val threads by timelineViewModel.threads.collectAsState()
    val events by timelineViewModel.events.collectAsState()

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
                        PostItem(post = post)
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    1 -> items(threads) { thread ->
                        ThreadItem(thread = thread)
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    2 -> items(events) { event ->
                        EventItem(event = event)
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineTopBar(
    onNavigateMap: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        ) {
            // 左側：ホームアイコン
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterStart),
                onClick = onNavigateMap
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.Black
                )
            }
            
            // 中央：履歴ボタン
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "タイムライン",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(16.dp)
    ) {
        // プロフィール画像
        Image(
            painter = painterResource(id = R.drawable.chap_android),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // コンテンツ部分
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // ユーザー名、ハンドル、日付
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@Mo... · ${post.created_at}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                IconButton(
                    onClick = { /* Show menu */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 投稿内容
            Text(
                text = post.content,
                fontSize = 15.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ThreadItem(thread: Thread) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chap_android),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@... · ${thread.created_at}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = thread.content,
                fontSize = 15.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chap_android),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@... · ${event.created_at}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.content,
                fontSize = 15.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}
