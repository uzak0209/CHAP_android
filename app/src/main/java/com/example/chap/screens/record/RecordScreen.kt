package com.example.chap.screens.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    recordViewModel: RecordViewModel,
    onNavigateTimeline: () -> Unit,
    onNavigateSetting: () -> Unit,
    onNavigateMap: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "履歴",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "履歴画面",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Button(onClick = onNavigateTimeline) {
                    Text("タイムラインに戻る")
                }
                
                Button(onClick = onNavigateMap) {
                    Text("マップに戻る")
                }
                
                Button(onClick = onNavigateSetting) {
                    Text("設定へ")
                }
            }
        }
    }
}