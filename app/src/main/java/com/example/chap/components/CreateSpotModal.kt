package com.example.chap.components

// TS由来の未変換要素を Kotlin モデルへ差し替え済み

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.chap.models.Coordinate
import kotlinx.coroutines.launch
import com.example.chap.models.CreateKind
import com.example.chap.models.PostCategory
import com.example.chap.models.PostCreateRequest
import com.example.chap.models.Status
import com.example.chap.screens.map.LocationViewModel
import androidx.compose.runtime.collectAsState
import com.example.chap.models.SpotCreateRequest

// TS由来の未変換要素を Kotlin モデルへ差し替え済み


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpotModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    selectedKind: CreateKind,
    locationViewModel: LocationViewModel,
    coordinate: Coordinate?,
    onRequestMakeLocation: ((title: String, description: String) -> Unit)? = null
) {

    if (!isOpen) return
    val scope = rememberCoroutineScope()
    // 未定義だった ViewModel をローカルで取得
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PostCategory.ENTERTAINMENT) }
    var loading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    fun reset() {
        title = ""
        description = ""
        image = ""
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

                //タイトル
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                         title = it
                    },
                    label = { Text("タイトル") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    minLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )

                Spacer(Modifier.height(12.dp))

                //登録名
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        if (it.length <= 30) description = it
                    },
                    label = { Text("登録名") },
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
                    Text("${description.length}/280", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                            if (selectedKind == CreateKind.SPOT && onRequestMakeLocation != null) {
                                onRequestMakeLocation(title.trim(), description.trim())
                                reset()
                                onClose()
                                return@Button
                            }
                            if (locationViewModel.locationState.value.status == Status.LOADED) {
                                val createSpotObject = SpotCreateRequest(
                                    coordinate = locationViewModel.locationState.value.location!!,
                                    title = title.trim(),
                                    description = description.trim(),
                                    image = "",
                                )
                                
                                scope.launch {
                                    loading = true
                                    try {

                                        locationViewModel.createSpot(createSpotObject)

                                        // 投稿成功時の処理
                                        loading = false
                                        reset()
                                        onClose()
                                    } catch (e: Exception) {
                                        loading = false
                                        e.printStackTrace()

                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank() &&
                                !loading &&
                                locationViewModel.locationState.collectAsState().value.status == Status.LOADED
                    ) {
                        Text(if (loading) "登録中..." else "登録する")
                    }
                }
            }
        }
    }
}


