package com.back.chap.screens.record

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.back.chap.location.LocationProvider
import com.back.chap.models.Event
import com.back.chap.models.Post
import com.back.chap.models.Spot
import com.back.chap.models.Thread
import com.back.chap.models.User
import com.back.chap.repository.EventRepositoryImpl
import com.back.chap.repository.MapRepositoryImpl
import com.back.chap.repository.PostRepositoryImpl
import com.back.chap.repository.ThreadRepositoryImpl
import com.back.chap.repository.UserRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import javax.inject.Inject
import okhttp3.Request
import okhttp3.RequestBody

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val postRepository: PostRepositoryImpl,
    private val threadRepository: ThreadRepositoryImpl,
    private val eventRepository: EventRepositoryImpl,
    private val mapRepository : MapRepositoryImpl,
    private val locationProvider: LocationProvider,
    private val userRepository: UserRepositoryImpl,
) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts
    private val _threads = MutableStateFlow<List<com.back.chap.models.Thread>>(emptyList())
    val threads: StateFlow<List<Thread>> = _threads
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _spots = MutableStateFlow<List<Spot>>(emptyList())
    val spots: StateFlow<List<Spot>> = _spots

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _ownerPhotoUrl = MutableStateFlow<String>("")
    val ownerPhotoUrl: StateFlow<String> = _ownerPhotoUrl

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage


        fun load() {
            if (_isLoading.value) return
            viewModelScope.launch {
                _isLoading.value = true
                // load current user info first
                runCatching { userRepository.getCurrentUser() }.onSuccess { user ->
                    _currentUser.value = user
                    _currentUserId.value = user?.id
                }
                mapRepository.getAllSpots().onSuccess { list ->
                    // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                    val current = _spots.value.associateBy { it.id }
                    val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                    _spots.value = merged.sortedByDescending { it.createdAt }
                }.onFailure {
                    // TODO: error handling (log/report)
                }

                val coord = locationProvider.current()
                if (coord != null) {
                    postRepository.getAllPosts().onSuccess { list ->
                    // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                    val current = _posts.value.associateBy { it.id }
                    val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                    _posts.value = merged.sortedByDescending { it.createdAt }
                }.onFailure {
                    // TODO: error handling (log/report)
                }
                }
                threadRepository.getAllThreads().onSuccess { list ->
                    // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                    val current = _threads.value.associateBy { it.id }
                    val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                    _threads.value = merged.sortedByDescending { it.createdAt }
                }.onFailure {
                    // TODO: error handling (log/report)
                }
                eventRepository.getAllEvents().onSuccess { list ->
                    // 既存のローカル追加分とマージ（新規投稿が API 反映前でも残す）
                    val current = _events.value.associateBy { it.id }
                    val merged = list + current.values.filter { existing -> list.none { it.id == existing.id } }
                    _events.value = merged.sortedByDescending { it.createdAt }
                }.onFailure {
                    // TODO: error handling (log/report)
                }
                _isLoading.value = false
            }
        }

        fun refreshCurrentUser() {
            viewModelScope.launch {
                runCatching { userRepository.getCurrentUser() }.onSuccess { user ->
                    _currentUser.value = user
                    _currentUserId.value = user?.id
                }
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun uploadProfileImage(uri: Uri, context: Context) {
            viewModelScope.launch {
                try {
                    _isUploadingImage.value = true
                    val uid = auth.currentUser?.uid ?: return@launch

                    // Firebase ID トークンを取得
                    val token = Firebase.auth.currentUser?.getIdToken(false)
                        ?.await()?.token

                    if (token == null) {
                        Log.e("SettingViewModel", "Failed to get Firebase ID token")
                        _isUploadingImage.value = false
                        return@launch
                    }

                    // URIからバイト配列を読み込む
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes == null) {
                        _isUploadingImage.value = false
                        return@launch
                    }

                    withContext(Dispatchers.IO) {
                        try {
                            // Base64エンコード（Data URI形式）
                            val base64Image = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)

                            // Edge Functionを呼び出してアップロード（既存のupload-profileを使用）
                            val publicUrl = callUploadImageEdgeFunction(
                                endpoint = "https://jaemimxpboicrxbpaycq.functions.supabase.co/upload-profile",
                                userId = uid,
                                imageBase64 = base64Image,
                                token = token
                            )

                            if (publicUrl == null) {
                                return@withContext
                            }

                            val urlWithVersion = "$publicUrl?v=${System.currentTimeMillis()}"
                            Log.i("SettingViewModel", "Uploaded image to: $urlWithVersion")

                            // Firestoreを更新
                            userRepository.updateUserToFirestore(
                                userId = uid,
                                updates = mapOf("photoUrl" to urlWithVersion)
                            )

                            // Firebase Authenticationのプロフィールを更新
                            auth.currentUser?.let { user ->
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setPhotoUri(Uri.parse(urlWithVersion))
                                    .build()
                                user.updateProfile(profileUpdates).await()
                            }

                            // UIを更新
                            withContext(Dispatchers.Main) {
                                _ownerPhotoUrl.value = urlWithVersion
                            }

                        } catch (e: Exception) {
                            Log.e("SettingViewModel", "Failed to upload image", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SettingViewModel", "Error reading image", e)
                } finally {
                    _isUploadingImage.value = false
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private suspend fun callUploadImageEdgeFunction(
            endpoint: String,
            userId: String,
            imageBase64: String,
            token: String
        ): String? = withContext(Dispatchers.IO) {
            try {
                // Edge FunctionはimageUrlパラメータでData URIを受け取る
                val jsonBody = """{"userId":"$userId","imageUrl":"$imageBase64"}"""
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = RequestBody.create(mediaType, jsonBody)
                val request = Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val client = OkHttpClient.Builder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .readTimeout(java.time.Duration.ofSeconds(30))
                    .writeTimeout(java.time.Duration.ofSeconds(30))
                    .build()

                val response = client.newCall(request).execute()
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        Log.w("RecordViewModel", "Edge function http ${resp.code}")
                        return@withContext null
                    }
                    val responseStr = resp.body?.string() ?: return@withContext null
                    try {
                        val obj = org.json.JSONObject(responseStr)
                        obj.optString("publicUrl", null)
                    } catch (e: Exception) {
                        Log.w("RecordViewModel", "Edge function parse error", e)
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e("RecordViewModel", "Edge function call failed", e)
                null
            }
        }
}
