package com.example.chap.screens.thread

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.R
import com.example.chap.components.CreateDialog
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.AppHeader
import com.example.chap.components.ui.BottomDestination
import com.example.chap.components.ui.SearchBar
import com.example.chap.models.CreateKind
import com.example.chap.ui.theme.BrandYellow
import com.example.chap.models.Thread

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    threadViewModel: ThreadViewModel,
    onNavigateHome: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateEvent: () -> Unit,
    onNavigateComment: (String) -> Unit,
){

    val threads by threadViewModel.threads.collectAsState()
    LaunchedEffect(threadViewModel) {
        threadViewModel.load()
    }
    var showCreate by remember { mutableStateOf(false) }
    // 上位 (Navigation) で共通 BottomBar を提供するためここでは純粋なコンテンツのみ
    Scaffold(
        bottomBar = {
            AppBottomBar(
                modifier = Modifier,
                onNavigate = {dest ->
                    when(dest){
                        BottomDestination.Home -> { onNavigateHome() }
                        BottomDestination.Map -> {onNavigateMap()}
                        BottomDestination.Event -> {onNavigateEvent()}
                        BottomDestination.Thread -> {/* already */}
                    }
                },
                bottomIconColor = Color.Black,
                bottomBackgroundColor = BrandYellow
            )
        }
    ){innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppHeader(
                title = "Thread",
                color = BrandYellow,
                headerTextColor = Color.Black
            )
            SearchBar(
                color = BrandYellow,
                searchTarget= "thread",
                searchTextColor = Color.Black
            )
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(threads) { item ->
                        ThreadListRow(
                            thread = item,
                            onClick = { onNavigateComment(item.id.toString()) }
                        )
                        Divider(color = Color(0xFFE8ECF0))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadListRow(
    thread: Thread,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.outline_comment_24),
            contentDescription = null,
            tint = Color(0xFF6B7280)
        )
        Text(
            text = " ${thread.likes}",
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(start = 4.dp, end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = thread.userName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF1F2933)
            )
            Text(
                text = thread.content,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "タップでコメントを表示",
                color = Color(0xFF9AA1A9),
                fontSize = 12.sp
            )
        }
        Text(
            text = thread.createdAt.take(10),
            color = Color(0xFF9AA1A9),
            fontSize = 12.sp
        )
    }
}