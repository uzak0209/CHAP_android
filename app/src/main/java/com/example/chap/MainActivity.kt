package com.example.chap

import Navigation
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.chap.API.PostViewModel
import com.example.chap.API.PostViewModelFactory
import com.example.chap.domain.repository.PostRepositoryImpl
import com.example.chap.libs.GetLocation
import com.example.chap.libs.LOCATION_PERMISSION_REQUEST_CODE
import com.example.chap.ui.theme.CHAPTheme

object AppContextHolder {
    lateinit var appContext: Context
    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

class MainActivity : ComponentActivity() {
    private val postViewModel: PostViewModel by viewModels {
        PostViewModelFactory(PostRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GetLocation(this, LOCATION_PERMISSION_REQUEST_CODE)
        enableEdgeToEdge()
        AppContextHolder.init(this)
    postViewModel.getAllPosts()
        setContent {
            CHAPTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Navigation()
                }
            }
        }
    }
}

