package com.example.chap

import Navigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.chap.API.PostViewModel
<<<<<<< HEAD
import androidx.activity.viewModels
=======
import com.example.chap.libs.GetLocation
import com.example.chap.libs.LOCATION_PERMISSION_REQUEST_CODE
>>>>>>> master
import com.example.chap.ui.theme.CHAPTheme


class MainActivity : ComponentActivity() {
    private val postViewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GetLocation(this, LOCATION_PERMISSION_REQUEST_CODE)
        enableEdgeToEdge()
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

