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
import com.example.chap.screens.event.EventViewModel
import com.example.chap.screens.event.EventViewModelFactory
import com.example.chap.screens.post.PostViewModel
import com.example.chap.screens.post.PostViewModelFactory
import com.example.chap.screens.thread.ThreadViewModel
import com.example.chap.screens.thread.ThreadViewModelFactory
import com.example.chap.screens.comment.CommentViewModel
import com.example.chap.screens.comment.CommentViewModelFactory
import com.example.chap.domain.repository.CommentRepositoryImpl
import com.example.chap.domain.repository.EventRepositoryImpl
import com.example.chap.domain.repository.PostRepositoryImpl
import com.example.chap.domain.repository.ThreadRepositoryImpl
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
    private val threadViewModel: ThreadViewModel by viewModels {
        ThreadViewModelFactory(ThreadRepositoryImpl())
    }
    private val eventViewModel: EventViewModel by viewModels {
        EventViewModelFactory(EventRepositoryImpl())
    }
    private val commentViewModel: CommentViewModel by viewModels {
        CommentViewModelFactory(CommentRepositoryImpl())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GetLocation(this, LOCATION_PERMISSION_REQUEST_CODE)
        enableEdgeToEdge()
        AppContextHolder.init(this)
        postViewModel.getAllPosts()
        threadViewModel.getAllThreads()
        eventViewModel.getAllEvents()
        setContent {
            CHAPTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Navigation(
                        postViewModel = postViewModel,
                        threadViewModel = threadViewModel,
                        eventViewModel = eventViewModel,
                        commentViewModel = commentViewModel
                    )
                }
            }
        }
    }
}

