package com.example.chap.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.chap.models.LoginTab

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