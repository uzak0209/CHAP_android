package com.example.chap.components.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SlidBar(
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit,
    onNavigateEvent: () -> Unit,
    onNavigateThread: () -> Unit,
) {
    ModalDrawerSheet(modifier = modifier) {
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = onNavigateHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("Events") },
            selected = false,
            onClick = onNavigateEvent,
            icon = { Icon(Icons.Default.Event, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("Threads") },
            selected = false,
            onClick = onNavigateThread,
            icon = { Icon(Icons.Default.Forum, contentDescription = null) }
        )
    }
}


