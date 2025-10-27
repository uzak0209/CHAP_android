package com.back.chap.screens.setting

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.back.chap.R
import com.back.chap.repository.UserRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingScreenEntryPoint {
    fun userRepository(): UserRepository
}

@Composable
fun SettingScreen(
    settingViewModel: SettingViewModel,
    onNavigateLogin: () -> Unit,
    onNavigateMap: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("Back") }
    var userImageUrl by remember { mutableStateOf<String?>(null) }
    var showNameDialog by remember { mutableStateOf(false) }

    // UserRepositoryからユーザー画像を取得
    LaunchedEffect(Unit) {
        try {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SettingScreenEntryPoint::class.java
            )
            val userRepository = hiltEntryPoint.userRepository()
            
            android.util.Log.d("SettingScreen", "Fetching user data...")
            val user = userRepository.getCurrentUser()
            android.util.Log.d("SettingScreen", "User data: $user")
            user?.let {
                android.util.Log.d("SettingScreen", "User name: ${it.name}, image: ${it.image}")
                userImageUrl = it.image?.takeIf { img -> img.isNotBlank() }
                userName = it.name.takeIf { name -> name.isNotBlank() } ?: "Unknown"
            } ?: run {
                android.util.Log.e("SettingScreen", "User is null")
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingScreen", "Failed to get user data", e)
        }
    }

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
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // プロフィール画像
                        if (userImageUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(userImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.chap_android),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = userName,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // 名前の変更ボタン
                        Button(
                            onClick = { showNameDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE0E0E0),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
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
                            containerColor = Color.Transparent,
                            contentColor = Color.Red
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp)
                    ) {
                        Text(
                            text = "ログアウト",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
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

suspend fun logOut(
    context: Context,
    settingViewModel: SettingViewModel,
) {
    settingViewModel.logOut(context)
}