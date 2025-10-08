package com.example.chap.screens.record

import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.R
import com.example.chap.auth.TokenManager
import com.example.chap.models.Post
import com.example.chap.models.Thread
import com.example.chap.models.Event
import com.example.chap.models.Spot
import org.json.JSONObject

@Composable
fun RecordScreen(
    recordViewModel: RecordViewModel,
    onNavigateMap: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Threads", "Events", "Spots")
    
    val posts by recordViewModel.posts.collectAsState()
    val threads by recordViewModel.threads.collectAsState()
    val events by recordViewModel.events.collectAsState()
    val spots by recordViewModel.spots.collectAsState()

    val context = LocalContext.current
    var currentUserId by remember { mutableStateOf<String?>(null) }


    //あとでUserRepositoryを作る
    LaunchedEffect(Unit) {
        val token = TokenManager(context).getToken()
        if (!token.isNullOrBlank()) {
            val parts = token.split('.')
            if (parts.size >= 2) {
                val b64 = parts[1].replace('-', '+').replace('_', '/')
                val pad = (4 - b64.length % 4) % 4
                val padded = b64 + "=".repeat(pad)
                runCatching {
                    val json = String(Base64.decode(padded, Base64.DEFAULT))
                    val obj = JSONObject(json)
                    obj.optString("user_id", null)
                }.onSuccess { uid -> currentUserId = uid }
            }
        }
    }

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
                    text = "Stas Neprokin",
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
                    0 -> items(posts.filter { it.user_id == currentUserId }) { post ->
                        RecordPostItem(post = post)
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    1 -> items(threads.filter { it.user_id == currentUserId }) { thread ->
                        RecordThreadItem(thread = thread)
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    2 -> items(events.filter { it.user_id == currentUserId }) { event ->
                        RecordEventItem(event = event)
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                    3 -> items(spots.filter { it.user_id == currentUserId }) { spot ->
                        RecordSpotItem(spot = spot)
                        Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordTopBar(
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
                .padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = onNavigateMap,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "map",
                    tint = Color.Black
                )
            }

            Text(
                text = "履歴",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun RecordPostItem(post: Post) {
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
fun RecordThreadItem(thread: Thread) {
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
fun RecordEventItem(event: Event) {
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

@Composable
fun RecordSpotItem(spot: Spot) {
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
                        text = spot.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@... · ${spot.created_at}",
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
                text = spot.content,
                fontSize = 15.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        }
    }
}