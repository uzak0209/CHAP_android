package com.back.chap.components.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.back.chap.ui.theme.BrandBlue
import com.back.chap.R

/**
 * アプリ共通の BottomBar。
 * onNavigate: (destination) -> Unit 形式でボタン押下を親へ通知。
 */
@Composable
fun AppBottomBar(
    modifier: Modifier = Modifier,
    onNavigate: (BottomDestination) -> Unit,
    bottomIconColor: Color = Color.White,
    bottomBackgroundColor: Color = BrandBlue
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = bottomBackgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.CenterVertically)
                .padding(horizontal = 55.dp)
        ) {
            IconButton(onClick = { onNavigate(BottomDestination.Home) }) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_home_24),
                    contentDescription = "Home",
                    modifier = Modifier.size(64.dp),
                    tint = bottomIconColor
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { onNavigate(BottomDestination.Map) }) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_map_24),
                    contentDescription = "Map",
                    modifier = Modifier.size(64.dp),
                    tint = bottomIconColor
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { onNavigate(BottomDestination.Event) }) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_flag_24),
                    contentDescription = "Event",
                    modifier = Modifier.size(64.dp),
                    tint = bottomIconColor
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { onNavigate(BottomDestination.Thread) }) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_mode_comment_24),
                    contentDescription = "Thread",
                    modifier = Modifier.size(64.dp),
                    tint = bottomIconColor
                )
            }
        }
    }
}

enum class BottomDestination { Home, Map, Event, Thread }
