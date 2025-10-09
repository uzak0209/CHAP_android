package com.example.chap.screens.login

import Logo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.screens.login.LoginViewModel
import com.example.chap.components.TextInput
import com.example.chap.auth.TokenManager
import com.example.chap.models.LoginTab

private val PrimaryColor = Color(0xFF4A4AFF)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    loginViewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(LoginTab.Login) }
    var displayName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val token = TokenManager(context).getToken()
        if (!token.isNullOrBlank()) {
            onLoginSuccess()
            return@LaunchedEffect
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Logo()
            Spacer(modifier = Modifier.height(50.dp))

            // 🔹 タブバー部分を別Composableに
            LoginTabBar(selectedTab = selectedTab, onTabChange = {selectedTab = it})
            if (selectedTab == LoginTab.SignUp) {
                Spacer(modifier = Modifier.height(24.dp))
                TextInput(
                    title = "表示名",
                    value = displayName,
                    onChange = {displayName = it},
                    placeholder = "表示名を入力してください"
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextInput(
                title = "メールアドレス",
                value = email,
                onChange = { email = it },
                placeholder = "example@example.com"
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextInput(
                title = "パスワード",
                value = password,
                onChange = {password  = it},
                placeholder = "パスワードを入力してください"
            )
            if (selectedTab == LoginTab.Login) {
                Spacer(modifier = Modifier.height(60.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
            val context = LocalContext.current
            Button(
                onClick = {
                    when (selectedTab) {
                        LoginTab.Login -> {
                            loginViewModel.login(
                                email = email,
                                password = password,
                                context = context,
                                onSuccess = { onLoginSuccess() },
                                onError = { it.message ?: "ログインに失敗しました" }
                            )
                        }
                        LoginTab.SignUp -> {
                            loginViewModel.register(
                                email = email,
                                password = password,
                                displayName = displayName,
                                context = context,
                                onSuccess = { onLoginSuccess() },
                                onError = { it.message ?: "登録に失敗しました" }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                val text = when (selectedTab) {
                    LoginTab.Login -> "ログイン"
                    LoginTab.SignUp -> "新規登録"
                }
                Text(text, color = Color.White, fontSize = 16.sp)
            }
            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = msg,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            if (selectedTab == LoginTab.Login) {
                Spacer(modifier = Modifier.height(40.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
            AgreementSection()
        }
    }
}






