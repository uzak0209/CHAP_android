import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chap.Screens.Login.LoginScreen
import com.example.chap.Screens.Map.MapScreen
import com.example.chap.Screens.PostTimeline.PostTimelineTemplate
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chap.Screens.PostTimeline.PostTimelineViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chap.Models.Coordinate
import com.example.chap.Models.Event
import com.example.chap.Models.Thread
import com.example.chap.Screens.Event.EventScreen
import com.example.chap.Screens.Thread.ThreadScreen
import com.example.chap.domain.repository.PostRepositoryImpl


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation() {
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
            )
        }
        composable("home"){
            val repository = remember { PostRepositoryImpl() }
            val vm: PostTimelineViewModel = viewModel(factory = object: ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return PostTimelineViewModel(repository) as T
                }
            })
            val postList by vm.posts.collectAsState()
            val loading by vm.isLoading.collectAsState()
            LaunchedEffect(Unit){ vm.load() }
            PostTimelineTemplate(
                postList = postList,
                iLoading = loading,
                isRefreshing = false,
                onRefresh = { vm.load() },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
            )
    
        }
        composable("thread"){
            ThreadScreen(
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateEvent = { navController.navigate("event") { launchSingleTop = true } },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                threadList = listOf(
                    Thread( // Create an instance of your data class
                        id = 1, // Provide actual values
                        type = "discussion",
                        created_at = "2023-01-01T12:00:00Z",
                        updated_at = "2023-01-01T12:00:00Z",
                        deleted_at = null,
                        username = "User1",
                        user_id = "userId1",
                        coordinate = Coordinate(0.0, 0.0), // Assuming Coordinate structure
                        category = "general",
                        content = "This is the first thread content.",
                        valid = true,   // 'valid' is a property of ThreadItem
                        like = 10,
                        tags = listOf("kotlin", "android")
                    )
                )
            )
        }
        composable("event"){
            EventScreen(
                onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                onNavigateMap = { navController.navigate("map") { launchSingleTop = true } },
                onNavigateThread = { navController.navigate("thread") { launchSingleTop = true } },
                eventList = listOf(
                    Event( // Create an instance of your data class
                        id = 1, // Provide actual values
                        type = "discussion",
                        created_at = "2023-01-01T12:00:00Z",
                        updated_at = "2023-01-01T12:00:00Z",
                        deleted_at = null,
                        username = "User1",
                        user_id = "userId1",
                        coordinate = Coordinate(0.0, 0.0), // Assuming Coordinate structure
                        category = "general",
                        content = "This is the first thread content.",
                        valid = true,   // 'valid' is a property of ThreadItem
                        like = 10,
                        tags = listOf("kotlin", "android")
                    )
                )
            )
        }

    }
}