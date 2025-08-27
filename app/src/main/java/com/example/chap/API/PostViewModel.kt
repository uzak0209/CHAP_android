package com.example.chap.API

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chap.Models.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object PostViewModel: ViewModel() {
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
                // TODO: 成功したら画面遷移や保存処理
                println("ログイン成功: $response")
            } catch (e: Exception) {

                println("エラー: ${e.message}")
            }
        }
    }
}