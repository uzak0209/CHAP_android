package com.back.chap

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.back.chap.screens.event.EventViewModel
import com.back.chap.screens.post.PostViewModel
import com.back.chap.screens.thread.ThreadViewModel
import com.back.chap.screens.comment.CommentViewModel
import com.back.chap.screens.login.LoginViewModel
import com.back.chap.screens.map.LocationViewModel
import com.back.chap.screens.record.RecordViewModel
import com.back.chap.screens.setting.SettingViewModel
import com.back.chap.screens.timeline.TimelineViewModel
import com.back.chap.ui.theme.CHAPTheme
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
            val settingViewModel: SettingViewModel = hiltViewModel()
            val timelineViewModel: TimelineViewModel = hiltViewModel()
            val recordViewModel : RecordViewModel = hiltViewModel()
            CHAPTheme {
                postViewModel.load()
                threadViewModel.load()
                eventViewModel.load()
                locationViewModel.load()
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Navigation(
                        postViewModel = postViewModel,
                        threadViewModel = threadViewModel,
                        eventViewModel = eventViewModel,
                        commentViewModel = commentViewModel,
                        locationViewModel = locationViewModel,
                        loginViewModel = loginViewModel,
                        settingViewModel = settingViewModel,
                        timelineViewModel = timelineViewModel,
                        recordViewModel = recordViewModel,
                    )
                }
            }
        }
    }
}

