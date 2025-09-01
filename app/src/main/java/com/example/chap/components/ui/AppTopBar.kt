package com.example.chap.components.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chap.R

/**
 * アプリ共通の BottomBar。
 * onNavigate: (destination) -> Unit 形式でボタン押下を親へ通知。
 */
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    onNavigate: (BottomDestination) -> Unit,
) {
    TopAppBar(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {

        }
    }
}

