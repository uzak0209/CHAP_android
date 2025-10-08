package com.example.chap

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chap.auth.TokenManager
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
    val context = LocalContext.current
    val startDestinationState = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = TokenManager(context).getToken()
        startDestinationState.value = if (!token.isNullOrBlank()) "map" else "login"
    }

    val startDestination = startDestinationState.value
    if (startDestination == null) {
        // ローディング中は何も表示しない（瞬間的）
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
            )
        }

        composable("timeline"){
            TimelineScreen(
                timelineViewModel = timelineViewModel,
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                onNavigateMapFocus = { type, id ->
                    navController.navigate("mapFocus/$type/$id") { launchSingleTop = true }
                }
            )
        }

        composable("record"){
            RecordScreen(
                recordViewModel = recordViewModel,
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
            )
        }

        composable(
            route = "mapFocus/{type}/{id}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type")
            val id = backStackEntry.arguments?.getLong("id")
            MapScreen(
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
                onNavigateSetting = { navController.navigate("setting"){ launchSingleTop = true } },
                onNavigateTimeline = { navController.navigate("timeline"){ launchSingleTop = true } },
                onNavigateRecord = { navController.navigate("record"){ launchSingleTop = true } },
                locationViewModel = locationViewModel,
                focusType = type,
                focusId = id
            )
        }
    }
}

