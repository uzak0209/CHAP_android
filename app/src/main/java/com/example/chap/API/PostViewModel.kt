package com.example.chap.API


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.Post
import com.example.chap.domain.repository.PostRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

<<<<<<< HEAD
class PostViewModel: ViewModel() {
=======
class PostViewModel(
    private val postRepository: PostRepositoryImpl = PostRepositoryImpl()
) : ViewModel() {
>>>>>>> master
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: MutableStateFlow<List<Post>> = _posts

    fun getAllPosts() {
        viewModelScope.launch {
<<<<<<< HEAD
            try {
                val response = ApiClient.request(
                    url = ApiEndpoints.Posts.LIST,
                    method = "POST",
                    body = mapOf("lat" to LocationViewModel.location?.lat.toString(), "lng" to LocationViewModel.location?.lng.toString())
                )
                println("取得成功: $response")
            } catch (e: Exception) {
=======
            val result = postRepository.getAllPostsApi()
            result.onSuccess { response ->
                println("投稿一覧取得成功: $response")
                // 必要に応じて_postの更新など
            }.onFailure { e ->
>>>>>>> master
                println("エラー: ${e.message}")
            }
        }
    }

<<<<<<< HEAD
    fun createPost(post: Post){
        viewModelScope.launch {
            // TODO: create 実装
            println("createPost() 呼び出し (未実装): $post")
        }
    }

    fun fetchAround(lat: Double, lng: Double){
        viewModelScope.launch {
            // TODO: fetchAround 実装
            println("fetchAround() 呼び出し (未実装): $lat,$lng")
=======
    @RequiresApi(Build.VERSION_CODES.O)
    fun createPost(post: Post) {
        viewModelScope.launch {
            val result = postRepository.createPostApi(post)
            result.onSuccess { response ->
                println("投稿成功: $response")
            }.onFailure { e ->
                println("エラー: ${e.message}")
            }
>>>>>>> master
        }
    }
}

