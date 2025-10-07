package com.example.chap.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chap.R
import com.example.chap.ui.theme.BrandBlue

/**
 * マップヘッダー
 */
@Composable
fun MapHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
    ) {
        // 左上のアイコン
        LeftHeaderIcons()
        
        // 中央ロゴ
        CenterLogo()
    }
}

@Composable
private fun BoxScope.LeftHeaderIcons() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 地図アイコン
        FloatingActionButton(
            modifier = Modifier
                .size(48.dp),
            shape = CircleShape,
            containerColor = BrandBlue,
            onClick = { /* TODO: 地図関連の機能 */ }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ViewSidebar,
                contentDescription = "Map",
                tint = Color.White
            )
        }
        
        // 地球アイコン
        FloatingActionButton(
            modifier = Modifier.size(48.dp),
            containerColor = Color(0xFF7E57C2),
            shape = CircleShape,
            onClick = { /* TODO: グローバル機能 */ }
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Global",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun BoxScope.CenterLogo() {
    Image(
        painter = painterResource(id = R.drawable.map_header_background),
        contentDescription = "CHAP logo",
        modifier = Modifier
            .align(Alignment.TopCenter)
            .size(150.dp)
//        contentScale= ContentScale.Fit
    )
}


