package com.example.chap

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chap.API.CommentViewModel
import com.example.chap.API.EventViewModel
import com.example.chap.API.PostViewModel
import com.example.chap.API.ThreadViewModel
import com.example.chap.Screens.Comment.CommentScreen
import com.example.chap.Screens.Event.EventScreen
import com.example.chap.Screens.Login.LoginScreen
import com.example.chap.Screens.Map.MapScreen
import com.example.chap.Screens.PostTimeline.PostTimelineTemplate
import com.example.chap.Screens.Thread.ThreadScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    postViewModel: PostViewModel,
    threadViewModel: ThreadViewModel,
    eventViewModel: EventViewModel,
    commentViewModel: CommentViewModel,
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
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } }
            )
        }
        composable("thread"){
            ThreadScreen(
                threadViewModel = threadViewModel,
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                onNavigateComment = { id -> navController.navigate("comment/$id") { launchSingleTop = true } }
            )
        }
        composable(
            route = "comment/{threadId}",
            arguments = listOf(navArgument("threadId") { type = NavType.LongType })
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: return@composable
            val threadList by threadViewModel.threads.collectAsState()
            val targetThread = threadList.firstOrNull { it.id == threadId }
            targetThread?.let { t ->
                CommentScreen(
                    commentViewModel = commentViewModel,
                    thread = t,
                    onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                    onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                    onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                    onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
                )
            }
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

