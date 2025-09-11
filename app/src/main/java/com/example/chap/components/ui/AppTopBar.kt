package com.example.chap.components.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chap.R

/**
 * アプリ共通の BottomBar。
 * onNavigate: (destination) -> Unit 形式でボタン押下を親へ通知。
 */
@Composable
public fun AppHeader(
    modifier: Modifier = Modifier,
    title: String = "Home",
    color: Color = Color(0xFF9DC4FF)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.chap_android),
                contentDescription = "App",
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
