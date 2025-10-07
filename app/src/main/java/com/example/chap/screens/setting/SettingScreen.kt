package com.example.chap.screens.setting

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.R
import kotlinx.coroutines.launch

@Composable
fun SettingScreen(
    settingViewModel: SettingViewModel,
    onNavigateLogin: () -> Unit,
    onNavigateMap: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("遠藤裕人") }
    var showNameDialog by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SettingTopBar(
                    onNavigateMap
                )
                // 波模様の背景エリア
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                ) {
                    // ball_frame.pngを背景として使用
                    Image(
                        painter = painterResource(id = R.drawable.ball_frame),
                        contentDescription = "Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // プロフィール画像
                        Image(
                            painter = painterResource(id = R.drawable.chap_android),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 画像をアップロードボタン
                        Button(
                            onClick = { /* TODO: 画像アップロード処理 */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E0E0),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .padding(horizontal = 32.dp)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "画像をアップロード",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // 名前表示と変更ボタン
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 32.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 名前表示ボタン
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE0E0E0),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = userName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            // 名前の変更ボタン
                            Button(
                                onClick = { showNameDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE0E0E0),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "名前の変更",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // 白い背景エリア
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .background(Color.White),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // ログアウトボタン
                    Button(
                        onClick = {
                            scope.launch {
                                logOut(context, settingViewModel)
                                onNavigateLogin()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "ログアウト",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // 名前変更ダイアログ
    if (showNameDialog) {
        NameChangeDialog(
            currentName = userName,
            onDismiss = { showNameDialog = false },
            onConfirm = { newName ->
                userName = newName
                showNameDialog = false
            }
        )
    }
}

@Composable
fun SettingTopBar(
    onNavigateMap: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        ) {
            // 左側：メニューアイコン
            IconButton(
                modifier = Modifier
					.align(Alignment.CenterStart),
				onClick = onNavigateMap
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }

			Text(
				modifier = Modifier
					.align(Alignment.Center),
				text = "設定",
				fontSize = 18.sp,
				fontWeight = FontWeight.Bold,
				color = Color.Black
			)
        }
    }
}

@Composable
fun NameChangeDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "名前の変更",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名前") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text("変更")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

suspend fun logOut(
    context: Context,
    settingViewModel: SettingViewModel,
) {
    settingViewModel.logOut(context)
}