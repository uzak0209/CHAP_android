package com.example.chap.components.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chap.R

/**
 * アプリ共通の BottomBar。
 * onNavigate: (destination) -> Unit 形式でボタン押下を親へ通知。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actionIcons: List<Pair<ImageVector, () -> Unit>> = emptyList()
) {
    CenterAlignedTopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(navigationIcon, contentDescription = "Back")
                }
            }
        },
        actions = {
            actionIcons.forEach { (icon, onClick) ->
                IconButton(onClick = onClick) {
                    Icon(icon, contentDescription = null)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
    )
}
