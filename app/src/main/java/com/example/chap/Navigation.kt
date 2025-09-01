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
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.BottomDestination
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import com.example.chap.Screens.PostTimeline.PostTimelineViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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
            Scaffold(
                bottomBar = {
                    AppBottomBar { dest ->
                        when(dest){
                            BottomDestination.Home -> navController.navigate("home") { launchSingleTop = true }
                            BottomDestination.Map -> { /* already */ }
                            BottomDestination.Event -> { /* TODO */ }
                            BottomDestination.Thread -> { /* TODO */ }
                        }
                    }
                }
            ){ inner ->
                MapScreen(
                    onNavigateHome = { navController.navigate("home") { launchSingleTop = true } },
                )
            }
        }
        composable("home"){
            val repository = remember { PostRepositoryImpl() }
            val vm: PostTimelineViewModel = viewModel(factory = object: androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return PostTimelineViewModel(repository) as T
                }
            })
            val postList by vm.posts.collectAsState()
            val loading by vm.isLoading.collectAsState()
            LaunchedEffect(Unit){ vm.load() }
            Scaffold(
                bottomBar = {
                    AppBottomBar { dest ->
                        when(dest){
                            BottomDestination.Home -> { /* already */ }
                            BottomDestination.Map -> navController.navigate("map") { launchSingleTop = true }
                            BottomDestination.Event -> { /* TODO */ }
                            BottomDestination.Thread -> { /* TODO */ }
                        }
                    }
                }
            ){ inner ->
                PostTimelineTemplate(
                    postList = postList,
                    iLoading = loading,
                    isRefreshing = false,
                    onRefresh = { vm.refresh() },
                )
            }
        }
        composable("ThreadScreen"){

        }
        composable("EventScreen"){

        }

    }
}