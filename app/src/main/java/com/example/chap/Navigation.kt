package com.example.chap

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chap.API.CommentViewModel
import com.example.chap.API.EventViewModel
import com.example.chap.Screens.Login.LoginScreen
import com.example.chap.Screens.Map.MapScreen
import com.example.chap.Screens.PostTimeline.PostTimelineTemplate
import com.example.chap.API.PostViewModel
import com.example.chap.API.ThreadViewModel
import com.example.chap.Models.Coordinate
import com.example.chap.Models.Event
import com.example.chap.Models.Thread
import com.example.chap.Screens.Comment.CommentScreen
import com.example.chap.Screens.Event.EventScreen
import com.example.chap.Screens.Thread.ThreadScreen



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    postViewModel: PostViewModel,
    threadViewModel: ThreadViewModel,
    eventViewModel: EventViewModel,
    commentViewModel: CommentViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("map") {
                        popUpTo("login") { inclusive = true } // ログイン画面を履歴から削除
                    }
                }
            )
        }
        composable("map") {
            MapScreen(
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
                postViewModel = postViewModel,
                threadViewModel = threadViewModel,
                eventViewModel = eventViewModel
            )
        }
    composable("home"){
            PostTimelineTemplate(
                postViewModel = postViewModel,
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
            )
        }
        composable("thread"){
            ThreadScreen(
                threadViewModel = threadViewModel,
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
            )
        }

        composable("comment"){
            CommentScreen(
                commentViewModel = commentViewModel,
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
            )
        }
        composable("event"){
            EventScreen(
                eventViewModel = eventViewModel,
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
            )
        }

    }
}

//threadList = listOf(
//Thread( // Create an instance of your data class
//id = 1, // Provide actual values
//type = "discussion",
//created_at = "2023-01-01T12:00:00Z",
//updated_at = "2023-01-01T12:00:00Z",
//deleted_at = null,
//username = "User1",
//user_id = "userId1",
//coordinate = Coordinate(0.0, 0.0), // Assuming Coordinate structure
//category = "general",
//content = "This is the first thread content.",
//valid = true,   // 'valid' is a property of ThreadItem
//like = 10,
//tags = listOf("kotlin", "android")
//)
//)
//eventList = listOf(
//Event( // Create an instance of your data class
//id = 1, // Provide actual values
//type = "discussion",
//created_at = "2023-01-01T12:00:00Z",
//updated_at = "2023-01-01T12:00:00Z",
//deleted_at = null,
//username = "User1",
//user_id = "userId1",
//coordinate = Coordinate(0.0, 0.0), // Assuming Coordinate structure
//category = "general",
//content = "This is the first thread content.",
//valid = true,   // 'valid' is a property of ThreadItem
//like = 10,
//tags = listOf("kotlin", "android")
//)
//)