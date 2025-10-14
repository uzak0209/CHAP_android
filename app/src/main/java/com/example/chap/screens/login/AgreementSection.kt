package com.example.chap.screens.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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