package com.example.chap

import Logo
import android.os.Bundle
import android.Manifest
import android.R.attr.enabled
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.components.TextInput
import com.example.chap.ui.theme.CHAPTheme
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.locationcomponent.location

private val PrimaryColor = Color(0xFF4A4AFF)
private val BackgroundGray = Color(0xFFF8F8F8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            UserLocationMap()
//            CHAPTheme {
//                Surface(modifier = Modifier.fillMaxSize()) {
////                  LoginScreen()
//
//                }
//            }
        }
    }
}
@Composable
fun MapScreen(){

}
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(LoginTab.Login) }

    MainBody(
        email = email,
        password = password,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        selectedTab = selectedTab,
        onTabChange = { selectedTab = it }
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
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTabChange: (LoginTab) -> Unit
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
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(60.dp))
            Button(
                onClick = { /* Handle login */ },
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

            Spacer(modifier = Modifier.height(40.dp))

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
fun AgreementItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
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
@Composable
fun UserLocationMap() {
    val context = LocalContext.current
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var permissionRequested by remember { mutableStateOf(false) }
    var styleLoaded by remember { mutableStateOf(false) }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val hasLocationPermission = permissions.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) lastLocation = loc
            }
        }
    }

    LaunchedEffect(hasLocationPermission, permissionRequested) {
        if (!hasLocationPermission && !permissionRequested) {
            permissionRequested = true
            launcher.launch(permissions)
        } else if (hasLocationPermission) {
            fused.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) lastLocation = loc
            }
        }
    }

    val viewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(2.0)
            center(Point.fromLngLat(-98.0, 39.5))
            pitch(0.0)
            bearing(0.0)
        }
    }

    LaunchedEffect(lastLocation) {
        lastLocation?.let { loc ->
            viewportState.setCameraOptions {
                center(Point.fromLngLat(loc.longitude, loc.latitude))
                zoom(14.0)
            }
        }
    }

    // 背景色 (スタイル未読込時の黒画面緩和)
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState,
            // 正しい DSL でスタイル適用 (STANDARD か streets 等)
            style = { (Style.STANDARD) }
        ) {
            MapEffect(Unit) { mapView ->
                // まだ style が null なら読み込み指示 (一度だけ)
                if (mapView.getMapboxMap().style == null && !styleLoaded) {
                    mapView.getMapboxMap().loadStyleUri(Style.STANDARD) { _ ->
                        styleLoaded = true
                    }
                } else if (mapView.getMapboxMap().style != null) {
                    styleLoaded = true
                }
            }
            MapEffect(lastLocation) { mapView ->
                val plugin = mapView.location
                plugin.updateSettings {
                    enabled = true
                    pulsingEnabled = true
                }
            }
        }
    if (!styleLoaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("地図スタイル読み込み中…", color = Color.DarkGray)
            }
        }
    }
}