package com.example.chap.Screens.Login

import Logo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.example.chap.API.LoginViewModel
import com.example.chap.components.TextInput

private val PrimaryColor = Color(0xFF4A4AFF)
private val BackgroundGray = Color(0xFFF8F8F8)

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(LoginTab.Login) }
    var displayName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    MainBody(
        email = email,
        password = password,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        selectedTab = selectedTab,
        onTabChange = { selectedTab = it },
        displayName = displayName,
        onDisplayNameChange = { displayName = it },
    onLoginSuccess = onLoginSuccess,
    errorMessage = errorMessage,
    onErrorMessageChange = { errorMessage = it }
    )
}

enum class LoginTab {
    Login, SignUp
}

@Composable
fun MainBody(
    selectedTab: LoginTab,
    email: String,
    password: String,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTabChange: (LoginTab) -> Unit,
    onLoginSuccess: () -> Unit,
    errorMessage: String?,
    onErrorMessageChange: (String?) -> Unit

) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
            LoginTabBar(selectedTab = selectedTab, onTabChange = onTabChange)
            if (selectedTab == LoginTab.SignUp) {
                Spacer(modifier = Modifier.height(24.dp))
                TextInput(
                    title = "表示名",
                    value = displayName,
                    onChange = onDisplayNameChange,
                    placeholder = "表示名を入力してください"
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextInput(
                title = "メールアドレス",
                value = email,
                onChange = onEmailChange,
                placeholder = "example@example.com"
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextInput(
                title = "パスワード",
                value = password,
                onChange = onPasswordChange,
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
                    onErrorMessageChange(null)
                    when (selectedTab) {
                        LoginTab.Login -> {
                            LoginViewModel.login(
                                email = email,
                                password = password,
                                context = context,
                                onSuccess = { onLoginSuccess() },
                                onError = { onErrorMessageChange(it.message ?: "ログインに失敗しました") }
                            )
                        }
                        LoginTab.SignUp -> {
                            LoginViewModel.register(
                                email = email,
                                password = password,
                                displayName = displayName,
                                context = context,
                                onSuccess = { onLoginSuccess() },
                                onError = { onErrorMessageChange(it.message ?: "登録に失敗しました") }
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


@Composable
fun LoginTabBar(selectedTab: LoginTab, onTabChange: (LoginTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LoginTabButton(
            text = "ログイン",
            isSelected = selectedTab == LoginTab.Login,
            onClick = { onTabChange(LoginTab.Login) }
        )
        LoginTabButton(
            text = "新規登録",
            isSelected = selectedTab == LoginTab.SignUp,
            onClick = { onTabChange(LoginTab.SignUp) }
        )
    }
}

@Composable
fun AgreementSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AgreementItem(
            icon = Icons.Default.Lock,
            text = "ログインすることで、利用規約とプライバシーポリシーに同意したものとみなされます。"
        )
        Spacer(modifier = Modifier.height(8.dp))
        AgreementItem(
            icon = Icons.Default.Place,
            text = "位置情報の取得許可が必要です。"
        )
    }
}

@Composable
fun AgreementItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoginTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = if (isSelected) Color.White else Color(0xFFF0F0F0),
    contentColor: Color = if (isSelected) Color.Black else Color.Gray,
    selectedBackgroundColor: Color = Color.Transparent
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = if (isSelected) selectedBackgroundColor else backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = contentColor)
    ) {
        Text(text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }

}