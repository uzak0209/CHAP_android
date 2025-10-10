package com.example.chap.components

// TS由来の未変換要素を Kotlin モデルへ差し替え済み

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.chap.models.Coordinate
import com.example.chap.screens.event.EventViewModel
import kotlinx.coroutines.launch
import com.example.chap.screens.post.PostViewModel
import com.example.chap.screens.thread.ThreadViewModel
import com.example.chap.models.CreateKind
import com.example.chap.models.PostCategory
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Status
import com.example.chap.screens.map.LocationViewModel
import androidx.compose.runtime.collectAsState

// TS由来の未変換要素を Kotlin モデルへ差し替え済み


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    selectedKind: CreateKind,
    locationViewModel: LocationViewModel,
    coordinate: Coordinate?,
    onRequestEventLocation: ((content: String, category: PostCategory) -> Unit)? = null
) {

    if (!isOpen) return
    val scope = rememberCoroutineScope()
    // 未定義だった ViewModel をローカルで取得
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PostCategory.ENTERTAINMENT) }
    var loading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    fun reset() {
        content = ""
        category = PostCategory.ENTERTAINMENT
    }

    Dialog(onDismissRequest = {
        if (!loading) {
            reset()
            onClose()
        }
    }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // ヘッダー
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("新しい${selectedKind}作成", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        enabled = !loading,
                        onClick = {
                            reset()
                            onClose()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 投稿内容
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        if (it.length <= 280) content = it
                    },
                    label = { Text("投稿内容") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("${content.length}/280", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                Spacer(Modifier.height(16.dp))

                // カテゴリ選択 (ExposedDropdownMenu)
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = category.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("カテゴリ") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
            PostCategory.entries.forEach {
                            DropdownMenuItem(
                text = { Text(it.toString()) },
                                onClick = {
                                    category = it
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // 位置情報（選択済みの座標を優先して表示）
                if (coordinate != null || locationViewModel.locationState.collectAsState().value.status == Status.LOADED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF666666))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "現在地: ${"%.4f".format(coordinate?.lat)}, ${"%.4f".format(coordinate?.lng)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF555555)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            reset()
                            onClose()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !loading
                    ) {
                        Text("キャンセル")
                    }
                    Button(
                        onClick = {
                            if (selectedKind == CreateKind.EVENT && onRequestEventLocation != null) {
                                onRequestEventLocation(content.trim(), category)
                                reset()
                                onClose()
                                return@Button
                            }
                            if (locationViewModel.locationState.value.status == Status.LOADED) {
                                val createObject = PostCreateRequest(
                                    coordinate = locationViewModel.locationState.value.location!!,
                                    content = content.trim(),
                                    category = category.toString(),
                                    visible = true,
                                )
                                
                                scope.launch {
                                    loading = true
                                    try {
                                        when(selectedKind) {
                                            CreateKind.POST -> {
                                                locationViewModel.createPost(createObject)
                                            }
                                            CreateKind.EVENT -> {
                                                locationViewModel.createEvent(createObject)
                                            }
                                            CreateKind.THREAD -> {
                                                locationViewModel.createThread(createObject)
                                            }
                                            CreateKind.SPOT -> {
                                                locationViewModel.createSpot(createObject)
                                            }
                                        }
                                        // 投稿成功時の処理
                                        loading = false
                                        reset()
                                        onClose()
                                    } catch (e: Exception) {
                                        loading = false
                                        e.printStackTrace()
                                        // エラー時はダイアログを閉じない
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = content.isNotBlank() &&
                                !loading &&
                                locationViewModel.locationState.collectAsState().value.status == Status.LOADED
                    ) {
                        Text(if (loading) "投稿中..." else "投稿する")
                    }
                }
            }
        }
    }
}


