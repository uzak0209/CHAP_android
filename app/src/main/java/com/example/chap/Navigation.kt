import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chap.Screens.Login.LoginScreen
import com.example.chap.Screens.Login.MapScreen

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
                        println("?????")
                        popUpTo("login") { inclusive = true } // ログイン画面を履歴から削除
                    }
                }
            )
        }
        composable("map") {
            MapScreen()
        }
    }
}