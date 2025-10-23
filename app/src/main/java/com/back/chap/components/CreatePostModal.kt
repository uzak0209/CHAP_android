package com.back.chap.components

// TS由来の未変換要素を Kotlin モデルへ差し替え済み

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.back.chap.api.ApiClient
import com.back.chap.api.ApiEndpoints
import com.back.chap.models.Coordinate
import com.back.chap.models.CreateKind
import com.back.chap.models.PostCategory
import com.back.chap.models.PostCreateRequest
import com.back.chap.models.SpotCreateRequest
import com.back.chap.models.Status
import com.back.chap.screens.map.LocationViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// TS由来の未変換要素を Kotlin モデルへ差し替え済み
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun CreatePostModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    selectedKind: CreateKind,
    locationViewModel: LocationViewModel,
    coordinate: Coordinate?,
    onRequestMakeLocation: ((content: String, category: PostCategory) -> Unit)? = null
) {

    if (!isOpen) return
    val scope = rememberCoroutineScope()
    // 未定義だった ViewModel をローカルで取得
    val context = LocalContext.current
    var ownerPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PostCategory.ENTERTAINMENT) }
    var loading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    // 画像クロッパーのランチャー
    val imageCropperLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            if (uri != null) {
                scope.launch {
                    try {
                        // 1) 端末からバイト列を取得し MIME を判定
                        val originalMime = context.contentResolver.getType(uri) ?: "image/jpeg"
                        val originalBytes = withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }
                        if (originalBytes == null) throw IllegalStateException("Failed to read cropped image bytes")

                        // 2) Lambda経由で圧縮（TS の uploadImage 相当）
                        val client = OkHttpClient()
                        val compressionUrl = "https://${ApiEndpoints.Image.COMPRESSION}"
                        val compressionRequest = Request.Builder()
                            .url(compressionUrl)
                            .post(originalBytes.toRequestBody(originalMime.toMediaTypeOrNull()))
                            .addHeader("Content-Type", originalMime)
                            .build()
                        
                        val compressionResp = withContext(Dispatchers.IO) { client.newCall(compressionRequest).execute() }
                        if (!compressionResp.isSuccessful) {
                            throw IllegalStateException("Image compression failed (${compressionResp.code})")
                        }
                        
                        val compressedBytes = withContext(Dispatchers.IO) { compressionResp.body.bytes() }
                        val compressedMime = compressionResp.header("Content-Type") ?: originalMime
                        
                        println("Processed image: originalSize=${originalBytes.size}, compressedSize=${compressedBytes.size}, compressedType=$compressedMime")

                        // 3) 一時アップロード用URLを取得（TS の getUploadURLMutation 相当）
                        val filename = "cropped_${System.currentTimeMillis()}.jpg"
                        val getUrlResponse = ApiClient.request(
                            url = ApiEndpoints.Image.GETUPLOADURL,
                            method = "POST",
                            body = mapOf("filename" to filename)
                        ) ?: throw IllegalStateException("Empty response from get upload url")

                        val json = JSONObject(getUrlResponse)
                        val uploadUrl = json.optString("imageUrl", json.optString("uploadUrl", json.optString("url", "")))
                        if (uploadUrl.isBlank()) throw IllegalStateException("Upload URL not provided")

                        // 4) 取得した URL に圧縮済みバイナリを PUT でアップロード
                        val uploadRequest = Request.Builder()
                            .url(uploadUrl)
                            .put(compressedBytes.toRequestBody(compressedMime.toMediaTypeOrNull()))
                            .addHeader("Content-Type", compressedMime)
                            .build()
                        val uploadResp = withContext(Dispatchers.IO) { client.newCall(uploadRequest).execute() }
                        if (!uploadResp.isSuccessful) {
                            val bodyStr = withContext(Dispatchers.IO) { uploadResp.body.string() }
                            throw IllegalStateException("Image upload failed (${uploadResp.code}): $bodyStr")
                        }

                        // 5) 公開URLを構築（TS の pathname から https://r2.chap-app.jp${pathname} を作る）
                        val url = java.net.URL(uploadUrl)
                        val pathname = url.path
                        val publicImageUrl = "https://r2.chap-app.jp$pathname"
                        
                        println("Image successfully uploaded to: $publicImageUrl")
                        ownerPhotoUrl = publicImageUrl
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isUploadingImage = false
                    }
                }
            } else {
                isUploadingImage = false
            }
        } else {
            isUploadingImage = false
        }
    }

    fun reset() {
        content = ""
        category = PostCategory.ENTERTAINMENT
        ownerPhotoUrl = null
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
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,

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

                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ownerPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(CircleShape)
                    )

                    if (isUploadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center),
                            color = Color(0xFF446E36)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        // 画像クロッパーを起動（円形クロップ設定）
                        isUploadingImage = true
                        val cropOptions = CropImageContractOptions(
                            uri = null,
                            cropImageOptions = CropImageOptions(
                                guidelines = CropImageView.Guidelines.ON,
                                aspectRatioX = 1, // 1:1のアスペクト比
                                aspectRatioY = 1,
                                fixAspectRatio = true, // アスペクト比を固定
                                allowRotation = true, // 回転を許可
                                allowFlipping = true, // 反転を許可
                                imageSourceIncludeGallery = true,
                                imageSourceIncludeCamera = true
                            )
                        )
                        imageCropperLauncher.launch(cropOptions)
                    },
                    enabled = !isUploadingImage,
                    modifier = Modifier
                        .width(180.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF48B3D3),
                        disabledContainerColor = Color(0xFFCCCCCC)
                    )
                ){
                    Text(
                        text = if (isUploadingImage) "アップロード中..." else "画像をアップロード",
                        fontSize = 14.sp,
                        color = if (isUploadingImage) Color.Gray else Color.White,
                    )
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
                            if ((selectedKind == CreateKind.EVENT || selectedKind == CreateKind.SPOT) && onRequestMakeLocation != null) {
                                onRequestMakeLocation(content.trim(), category)
                                reset()
                                onClose()
                                return@Button
                            }
                            if (locationViewModel.locationState.value.status == Status.LOADED) {
                                val createPostObject = PostCreateRequest(
                                    coordinate = locationViewModel.locationState.value.location!!,
                                    content = content.trim(),
                                    category = category.toString(),
                                    visible = true,
                                    image = ownerPhotoUrl ?: "",
                                )
                                val createSpotObject = SpotCreateRequest(
                                    coordinate = locationViewModel.locationState.value.location!!,
                                    title = content.trim(),
                                    description = content.trim(),
                                    image = ownerPhotoUrl ?: "",
                                )
                                
                                scope.launch {
                                    loading = true
                                    try {
                                        when(selectedKind) {
                                            CreateKind.POST -> {
                                                locationViewModel.createPost(createPostObject)
                                            }
                                            CreateKind.EVENT -> {
                                                locationViewModel.createEvent(createPostObject)
                                            }
                                            CreateKind.THREAD -> {
                                                locationViewModel.createThread(createPostObject)
                                            }
                                            CreateKind.SPOT -> {
                                                locationViewModel.createSpot(createSpotObject)
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


