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
import com.example.chap.screens.comment.CommentScreen
import com.example.chap.screens.comment.CommentViewModel
import com.example.chap.screens.event.EventViewModel
import com.example.chap.screens.post.PostViewModel
import com.example.chap.screens.thread.ThreadViewModel
import com.example.chap.screens.event.EventScreen
import com.example.chap.screens.login.LoginScreen
import com.example.chap.screens.login.LoginViewModel
import com.example.chap.screens.map.LocationViewModel
import com.example.chap.screens.map.MapScreen
import com.example.chap.screens.post.PostTimelineTemplate
import com.example.chap.screens.record.RecordScreen
import com.example.chap.screens.record.RecordViewModel
import com.example.chap.screens.setting.SettingScreen
import com.example.chap.screens.setting.SettingViewModel
import com.example.chap.screens.thread.ThreadScreen
import com.example.chap.screens.timeline.TimelineViewModel
import com.example.chap.screens.timeline.TimelineScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    loginViewModel: LoginViewModel,
    postViewModel: PostViewModel,
    threadViewModel: ThreadViewModel,
    eventViewModel: EventViewModel,
    commentViewModel: CommentViewModel,
    locationViewModel: LocationViewModel,
    settingViewModel: SettingViewModel,
    timelineViewModel: TimelineViewModel,
    recordViewModel : RecordViewModel,
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
                },
                loginViewModel = loginViewModel
            )
        }
        composable("map") {
            MapScreen(
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
                onNavigateSetting = { navController.navigate("setting"){ launchSingleTop = true } },
                onNavigateTimeline = { navController.navigate("timeline"){ launchSingleTop = true } },
                onNavigateRecord = { navController.navigate("record"){ launchSingleTop = true } },
                locationViewModel = locationViewModel
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

        composable("setting"){
            SettingScreen(
                settingViewModel = settingViewModel,
                onNavigateLogin = { navController.navigate("login") { launchSingleTop = true } },
            )
        }

        composable("timeline"){
            TimelineScreen(
                timelineViewModel = timelineViewModel,
                onNavigateRecord = { navController.navigate("record") { launchSingleTop = true } },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                onNavigateSetting = { navController.navigate("setting") { launchSingleTop = true } },
            )
        }
        composable("record"){
            RecordScreen(
                recordViewModel = recordViewModel,
                onNavigateTimeline = { navController.navigate("timeline") { launchSingleTop = true } },
                onNavigateSetting = { navController.navigate("setting") { launchSingleTop = true } },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
            )
        }

    }
}

