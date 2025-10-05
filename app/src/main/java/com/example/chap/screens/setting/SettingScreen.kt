package com.example.chap.screens.setting

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontVariation
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.AppHeader
import com.example.chap.components.ui.BottomDestination
import com.example.chap.ui.theme.BrandBlue
import kotlinx.coroutines.launch

@Composable
fun SettingScreen(
    settingViewModel: SettingViewModel,
    onNavigateLogin: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Scaffold{ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppHeader(
                title = "Home",
                color = BrandBlue
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    scope.launch {
                        logOut(
                            context,
                            settingViewModel
                        )
                        onNavigateLogin()
                    }
                }
            ){
                Text(text = "ログアウト")
            }
        }
    }
}

suspend fun logOut(
    context: Context,
    settingViewModel: SettingViewModel,
){
    settingViewModel.logOut(context)
}