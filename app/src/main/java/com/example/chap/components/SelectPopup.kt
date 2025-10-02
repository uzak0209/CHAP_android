package com.example.chap.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.R
private data class FabAction(
    val label: String,
    val iconRes: Int,
    val containerColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun AnimatedActionButton(
    index: Int,
    action: FabAction
) {
    val delayMs = index * 40
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 220, delayMillis = delayMs),
        label = "alpha"
    )
    val offsetY by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = tween(durationMillis = 220, delayMillis = delayMs),
        label = "offsetY"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.alpha(alpha).offset(y = offsetY)
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = action.label,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        FloatingActionButton(
            onClick = action.onClick,
            containerColor = action.containerColor,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                painter = painterResource(id = action.iconRes),
                contentDescription = action.label,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun SimpleModal(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ここに入力UIを配置してください。")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("作成") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("閉じる") }
        }
    )
}

/**
 * 既存画面側の FAB クリックで呼び出して即座にアクション候補を表示したい場合に使うオーバーレイ。
 * visible=true の間だけ下端右寄せで 3 つのアクション + 必要なモーダルを表示。
 * 背景タップで onDismiss()。
 */
@Composable
fun SelectPopupOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onReloadEvents: (lat: Double, lng: Double) -> Unit = { _, _ -> },
    onPostCreated: () -> Unit = {},
    onThreadCreated: () -> Unit = {},
    onEventCreated: () -> Unit = {},
    registerLocation: () -> Unit = {}
) {
    if (!visible) return

    val actionButtons = listOf(
        FabAction(
            label = "投稿作成",
            iconRes = R.drawable.outline_imagesmode_24,
            containerColor = Color(0xFF16A34A)
        ) { onDismiss(); onPostCreated() },
        FabAction(
            label = "スレッド作成",
            iconRes = R.drawable.outline_comment_24,
            containerColor = Color(0xFF7E22CE)
        ) { onDismiss(); onThreadCreated() },
        FabAction(
            label = "イベント作成",
            iconRes = R.drawable.outline_calendar_today_24,
            containerColor = Color(0xFFF97316)
        ) { onDismiss();onEventCreated() },
        FabAction(
            label = "地点登録",
            iconRes = R.drawable.location_on_24px,
            containerColor = Color(0xFFF91616)
        ) { onDismiss();registerLocation() }

    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .clickable(enabled = false) { }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                actionButtons.forEachIndexed { index, a ->
                    AnimatedActionButton(index, a)
                }
            }
        }
    }

}