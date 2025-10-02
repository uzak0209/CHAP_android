package com.example.chap

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chap.screens.event.EventViewModel
import com.example.chap.screens.post.PostViewModel
import com.example.chap.screens.thread.ThreadViewModel
import com.example.chap.screens.comment.CommentViewModel
import com.example.chap.repository.CommentRepositoryImpl
import com.example.chap.repository.EventRepositoryImpl
import com.example.chap.repository.PostRepositoryImpl
import com.example.chap.repository.ThreadRepositoryImpl
import com.example.chap.location.DefaultLocationProvider
import com.example.chap.location.LOCATION_PERMISSION_REQUEST_CODE
import com.example.chap.screens.login.LoginViewModel
import com.example.chap.screens.map.LocationViewModel
import com.example.chap.ui.theme.CHAPTheme
import dagger.hilt.android.AndroidEntryPoint

object AppContextHolder {
    lateinit var appContext: Context
    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppContextHolder.init(this)
        setContent {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val postViewModel: PostViewModel = hiltViewModel()
            val threadViewModel: ThreadViewModel = hiltViewModel()
            val eventViewModel: EventViewModel = hiltViewModel()
            val commentViewModel: CommentViewModel = hiltViewModel()
            val locationViewModel: LocationViewModel = hiltViewModel()
            CHAPTheme {
                postViewModel.getPosts()
                threadViewModel.getThreads()
                eventViewModel.getEvents()
                locationViewModel.getSpots()
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Navigation(
                        postViewModel = postViewModel,
                        threadViewModel = threadViewModel,
                        eventViewModel = eventViewModel,
                        commentViewModel = commentViewModel,
                        locationViewModel = locationViewModel,
                        loginViewModel = loginViewModel
                    )
                }
            }
        }
    }
}

