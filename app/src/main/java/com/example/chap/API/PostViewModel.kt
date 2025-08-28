package com.example.chap.API

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PostViewModel: ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: MutableStateFlow<List<Post>> = _posts

    fun getAllPosts(){
        viewModelScope.launch {
            try {
                val response = ApiClient.request(
                    url = ApiEndpoints.Posts.LIST,
                    method = "POST",
                    body = mapOf("lat" to LocationViewModel.location?.lat.toString(), "lng" to LocationViewModel.location?.lng.toString())
                )
                println("取得成功: $response")
            } catch (e: Exception) {
                println("エラー: ${e.message}")
            }
        }
    }

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
        }
    }
}

