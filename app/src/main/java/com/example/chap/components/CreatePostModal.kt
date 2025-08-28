package com.example.chap.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.chap.components.map.PostCategory
import com.example.chap.components.map.LoadStatus
import com.example.chap.components.map.LocationState
import com.example.chap.components.map.MapEventListener
import kotlinx.coroutines.launch
import java.time.Instant
// TS由来の未変換要素を Kotlin モデルへ差し替え済み

@Composable
fun CreatePostDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    locationState: LocationState,
    selectedCategoryFilter: PostCategory?, // TSのselectedCategory相当
    postActions: PostActions
) {
    if (!isOpen) return

    val scope = rememberCoroutineScope()
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<PostCategory?>(null) }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var tagInput by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    fun reset() {
        content = ""
        category = null
        tags = emptyList()
        tagInput = ""
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
                    Text("新しい投稿を作成", style = MaterialTheme.typography.titleMedium)
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
                    placeholder = { Text("今何をしていますか？") },
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
                        value = category?.label ?: "",
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
                        PostCategory.values().forEach {
                            DropdownMenuItem(
                                text = { Text(it.label) },
                                onClick = {
                                    category = it
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // タグ入力
                Text("タグ（任意）", style = MaterialTheme.typography.labelMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        placeholder = { Text("タグを入力") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (tagInput.isNotBlank() && !tags.contains(tagInput.trim())) {
                                    tags = tags + tagInput.trim()
                                    tagInput = ""
                                }
                                focusManager.clearFocus()
                            }
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (tagInput.isNotBlank() && !tags.contains(tagInput.trim())) {
                                tags = tags + tagInput.trim()
                                tagInput = ""
                            }
                        },
                        enabled = tagInput.isNotBlank()
                    ) {
                        Text("#")
                    }
                }

                if (tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.forEach { t ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("#$t", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "×",
                                            modifier = Modifier
                                                .clickable {
                                                    tags = tags.filterNot { it == t }
                                                }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 位置情報
                if (locationState.status == LoadStatus.LOADED) {
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
                            "現在地: ${"%.4f".format(locationState.location.lat)}, ${"%.4f".format(locationState.location.lng)}",
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
                            if (category != null && locationState.status == LoadStatus.LOADED) {
                                scope.launch {
                                    loading = true
                                    try {
                                        val req = CreatePostRequest(
                                            content = content.trim(),
                                            category = category!!,
                                            tags = tags,
                                            lat = locationState.location.lat,
                                            lng = locationState.location.lng,
                                            visible = (selectedCategoryFilter == null || selectedCategoryFilter == category)
                                        )
                                        postActions.createPost(req)
                                        postActions.fetchAroundPosts(req.lat, req.lng)
                                        reset()
                                        onClose()
                                    } finally {
                                        loading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = content.isNotBlank() &&
                                category != null &&
                                !loading &&
                                locationState.status == LoadStatus.LOADED
                    ) {
                        Text(if (loading) "投稿中..." else "投稿する")
                    }
                }
            }
        }
    }
}


