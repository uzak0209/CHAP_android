package com.example.chap.screens.event

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
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
import com.example.chap.models.Event
import com.example.chap.ui.theme.BrandRed

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    eventViewModel: EventViewModel,
    onNavigateMap: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateThread: () -> Unit,
){

    val events by eventViewModel.events.collectAsState()
    LaunchedEffect(eventViewModel) {
        eventViewModel.load()
    }
    var showCreate by remember { mutableStateOf(false) }
    // 上位 (Navigation) で共通 BottomBar を提供するためここでは純粋なコンテンツのみ
    Scaffold(
        bottomBar = {
            AppBottomBar(
                modifier = Modifier,
                onNavigate = { dest ->
                    when(dest){
                        BottomDestination.Home -> { onNavigateHome() }
                        BottomDestination.Map -> {onNavigateMap()}
                        BottomDestination.Event -> {/* already */}
                        BottomDestination.Thread -> {onNavigateThread()}
                    }
                },
                bottomIconColor = Color.White,
                bottomBackgroundColor = BrandRed
            )
        }
    ){innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppHeader(
                title = "Events",
                color = BrandRed
            )
            SearchBar(
                color = BrandRed,
                searchTarget= "event"
            )
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(events) { item ->
                        EventListRow(event = item)
                        Divider(color = Color(0xFFE8ECF0))
                    }
                }
            }
        }
    }
}

@Composable
private fun EventListRow(event: Event) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val statusIcon = Icons.Filled.EventAvailable
        val statusColor =  Color(0xFFEF4444)
        Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor)

        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(text = event.userName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1F2933))
            Text(
                text = event.content,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = event.createdAt.take(10), color = Color(0xFF9AA1A9), fontSize = 12.sp)
            Text(
                text = "開始: " + event.createdAt.replace('T', ' ').take(16),
                color = BrandRed,
                fontSize = 12.sp
            )
            Text(
                text = "場所:緯度" + "%.0f".format(event.coordinate.lat) + " ・ 経度" + "%.0f".format(event.coordinate.lng),
                color = BrandRed,
                fontSize = 12.sp
            )
        }
    }
}