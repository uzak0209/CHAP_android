package com.example.chap

import Navigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import com.example.chap.API.PostViewModel
import com.example.chap.ui.theme.CHAPTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GetLocation(this,  LOCATION_PERMISSION_REQUEST_CODE)
        enableEdgeToEdge()
        PostViewModel.getAllPosts()
        setContent {
            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        actions = {
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(painterResource(id = R.drawable.outline_home_24), contentDescription = "Home")
                            }
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(painterResource(id = R.drawable.outline_map_24), contentDescription = "Map")
                            }
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(painterResource(id = R.drawable.outline_flag_24), contentDescription = "Event")
                            }
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(painterResource(id = R.drawable.outline_mode_comment_24), contentDescription = "Thread")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                // innerPadding を適用して画面本体を表示
                Surface(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()) {
                    Navigation()
                }
            }
//            CHAPTheme {
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    Navigation()
//                }
//            }
        }
    }
}

