package com.example.chap.components.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import com.example.chap.R
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.RadioButton
import com.example.chap.models.Spot
import com.example.chap.screens.map.moveViewPoint
import com.example.chap.ui.theme.BrandBlue

@Composable
fun SlidBar(
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit,
    onNavigateEvent: () -> Unit,
    onNavigateThread: () -> Unit,
    isChatChecked: Boolean = true,
    isCommunityChecked: Boolean = false,
    isDisasterChecked: Boolean = false,
    onToggleChat: () -> Unit = {},
    onToggleCommunity: () -> Unit = {},
    onToggleDisaster: () -> Unit = {},
    spots: List<Spot>,
    onSpotClick: (Spot) -> Unit = {}
) {
    ModalDrawerSheet(modifier = modifier) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandBlue)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.chap_android),
                    contentDescription = "App",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "CHAP",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }

        Divider()

        // Menu section
        Text(
            text = "メニュー",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("ホーム") },
            selected = false,
            onClick = onNavigateHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("スレッド") },
            selected = false,
            onClick = onNavigateThread,
            icon = { Icon(Icons.Default.Forum, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("イベント") },
            selected = false,
            onClick = onNavigateEvent,
            icon = { Icon(Icons.Default.Event, contentDescription = null) }
        )

        Divider(modifier = Modifier.padding(top = 8.dp))

        Text(
            text = "投稿カテゴリ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = isChatChecked, onClick = { onToggleChat() })
                Spacer(modifier = Modifier.size(8.dp))
                Text("雑談")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = isCommunityChecked, onClick = { onToggleCommunity() })
                Spacer(modifier = Modifier.size(8.dp))
                Text("地域住民コミュニケーション")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = isDisasterChecked, onClick = { onToggleDisaster() })
                Spacer(modifier = Modifier.size(8.dp))
                Text("災害情報")
            }
        }
        Text(
            text = "地点登録",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(spots){spot ->
                NavigationDrawerItem(
                    label =  {Text(spot.content)},
                    selected = false,
                    onClick = { onSpotClick(spot) },
                    icon = { Icon(Icons.Default.AddLocationAlt, contentDescription = null) }
                )
            }
        }
    }
}


