package com.example.chap.screens.map

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.components.CreateDialog
import com.example.chap.components.SelectPopupOverlay
import com.example.chap.components.ToggleDimension
import com.example.chap.components.map.moveViewPoint
import com.example.chap.models.*
import com.example.chap.ui.theme.BrandBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * すべてのオーバーレイUIをまとめて管理
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapOverlays(
    state: MapState,
    locationState: LocationState,
    scope: CoroutineScope,
    locationViewModel: LocationViewModel,
    is3D: Boolean,
    onToggleDimension: () -> Unit,
    onLocationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 位置選択ガイド
        if(state.pendingEventDraft == null){
            // 右上のボタン群
            RightHeaderButtons(
                is3D = is3D,
                onToggleDimension = onToggleDimension,
                onLocationClick = onLocationClick
            )
        }
        if (state.pendingEventDraft != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            ) {
                LocationPickerGuide()
            }
        }

        // 投稿作成ボタン
        if (state.pendingEventDraft == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                CreatePostButton(
                    onClick = { state.showPopup = true }
                )
            }
        }

        // 決定ボタン
        if (state.pendingEventDraft != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                ConfirmLocationButton(
                    enabled = state.pendingTapCoordinate != null,
                    onClick = {
                        createEvent(
                            state = state,
                            scope = scope,
                            locationViewModel = locationViewModel
                        )
                    }
                )
            }
        }

        // ダイアログ
        CreateDialog(
            isOpen = state.showCreate,
            onClose = { state.showCreate = false },
            selectedKind = state.createKind,
            locationViewModel = locationViewModel,
            coordinate = if (state.createKind == CreateKind.EVENT) 
                state.pendingTapCoordinate else locationState.location,
            onRequestEventLocation = { content, category, tags ->
                state.pendingEventDraft = Triple(content, category, tags)
            }
        )

        SelectPopupOverlay(
            visible = state.showPopup,
            onDismiss = { state.showPopup = false },
            onPostCreated = {
                state.showPopup = false
                state.createKind = CreateKind.POST
                state.showCreate = true
            },
            onThreadCreated = {
                state.showPopup = false
                state.createKind = CreateKind.THREAD
                state.showCreate = true
            },
            onEventCreated = {
                state.showPopup = false
                state.createKind = CreateKind.EVENT
                state.showCreate = true
            },
            registerLocation = {
                state.showPopup = false
                state.createKind = CreateKind.SPOT
                state.showCreate = true
            }
        )

        // ローディング表示
        if (!state.styleLoaded) {
            LoadingIndicator()
        }
    }
}

@Composable
private fun LocationPickerGuide() {
    ExtendedFloatingActionButton(
        onClick = {},
        containerColor = BrandBlue,
        modifier = Modifier
            .padding(top = 12.dp)
    ) {
        Text(text = "タップして位置を決めてください", color = Color.White)
    }
}

@Composable
private fun CreatePostButton(onClick: () -> Unit) {
    FloatingActionButton(
        modifier = Modifier.padding(12.dp),
        onClick = onClick,
        shape = CircleShape,
        containerColor = BrandBlue
    ) {
        Text(
            text = "+",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ConfirmLocationButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = if (enabled) BrandBlue else Color.Gray,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(text = "この位置で作成", color = Color.White)
    }
}

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("地図スタイル読み込み中…", color = Color.DarkGray)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun createEvent(
    state: MapState,
    scope: CoroutineScope,
    locationViewModel: LocationViewModel
) {
    val draft = state.pendingEventDraft
    val coord = state.pendingTapCoordinate
    if (draft != null && coord != null) {
        val (content, category, tags) = draft
        val createObject = PostCreateRequest(
            coordinate = coord,
            content = content,
            category = category.toString(),
            valid = true,
            tags = tags,
            visible = true
        )
        scope.launch {
            try {
                locationViewModel.createEvent(createObject)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                state.pendingEventDraft = null
                state.pendingTapCoordinate = null
            }
        }
    }
}
@Composable
private fun BoxScope.RightHeaderButtons(
    is3D: Boolean,
    onToggleDimension: () -> Unit,
    onLocationClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 6.dp, bottom = 6.dp, start = 6.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // トップのトグルボタン（タップで展開/折りたたみ）
        FloatingActionButton(
            modifier = Modifier.size(56.dp),
            containerColor = BrandBlue,
            shape = CircleShape,
            onClick = { expanded = !expanded }
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                contentDescription = if (expanded) "Close" else "Menu",
                tint = Color.White
            )
        }

        // 展開メニュー（下にアニメーション表示）
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // サイドドロワーを開くボタン
//                FloatingActionButton(
//                    modifier = Modifier.size(56.dp),
//                    containerColor = BrandBlue,
//                    onClick = onMenuClick,
//                    shape = CircleShape,
//                ) {
//                    Icon(
//                        Icons.Default.MenuOpen,
//                        contentDescription = "Open Menu",
//                        tint = Color.White
//                    )
//                }

                // 3D/2Dトグルボタン
                FloatingActionButton(
                    modifier = Modifier.size(56.dp),
                    containerColor = BrandBlue,
                    shape = CircleShape,
                    onClick = onToggleDimension
                ) {
                    Text(
                        text = if (is3D) "3D" else "2D",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // 現在地に戻るボタン
                FloatingActionButton(
                    modifier = Modifier.size(56.dp),
                    containerColor = BrandBlue,
                    shape = CircleShape,
                    onClick = onLocationClick
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

