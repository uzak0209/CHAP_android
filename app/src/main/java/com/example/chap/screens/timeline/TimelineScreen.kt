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
import com.example.chap.models.Spot

@Composable
fun TimelineScreen(
    timelineViewModel: TimelineViewModel,
    onNavigateRecord: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateSetting: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Threads", "Events", "Spots")
    
    // サンプルデータ（後でViewModelから取得）
    val samplePosts = remember {
        List(6) { index ->
            Post(
                id = index.toLong(),
                type = "post",
                created_at = "Nov 20, 2023",
                updated_at = "Nov 20, 2023",
                deleted_at = null,
                user_id = "1",
                username = "Modest Mitkus",
                coordinate = com.example.chap.models.Coordinate(0.0, 0.0),
                content = "Everyone should own products that earn \$10,000/month.",
                category = "community",
                valid = true,
                like = 0,
                tags = emptyList()
            )
        }
    }

    Scaffold(
        topBar = {
            TimelineTopBar(
                onNavigateRecord = onNavigateRecord,
                onNavigateMap = onNavigateMap,
                onNavigateSetting = onNavigateSetting
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // プロフィールセクション
            ProfileSection(
                name = "Stas Neprokin",
                imageRes = R.drawable.chap_android
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
                    0 -> items(samplePosts) { post ->
                        PostItem(post = post)
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    1 -> items(emptyList<Thread>()) { thread ->
                        // Thread items
                    }
                    2 -> items(emptyList<Event>()) { event ->
                        // Event items
                    }
                    3 -> items(emptyList<Spot>()) { spot ->
                        // Spot items
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineTopBar(
    onNavigateRecord: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateSetting: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左側：ホームアイコン
            IconButton(onClick = onNavigateMap) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.Black
                )
            }
            
            // 中央：履歴ボタン
            TextButton(onClick = onNavigateRecord) {
                Text(
                    text = "履歴",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            // 右側：設定アイコン
            IconButton(onClick = onNavigateSetting) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun ProfileSection(
    name: String,
    imageRes: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
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