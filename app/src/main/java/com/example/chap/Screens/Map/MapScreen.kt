package com.example.chap.Screens.Map

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chap.API.LocationViewModel
import com.example.chap.API.PostViewModelFactory
import com.example.chap.R
import com.example.chap.components.CreatePostDialog
import com.example.chap.components.SelectPopupOverlay
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.BottomDestination
import com.example.chap.components.map.LocationState
import com.example.chap.components.map.LoadStatus
import com.example.chap.components.map.PostCategory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chap.API.PostViewModel
import com.example.chap.components.CreatePostDialog
import com.example.chap.components.ToggleDimension
import com.example.chap.domain.repository.PostRepositoryImpl
import com.example.chap.libs.GetLocation
import com.example.chap.libs.LOCATION_PERMISSION_REQUEST_CODE
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.locationcomponent.location


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen() {
    // Compose で ViewModel の位置情報を監視
    var is3D by remember { mutableStateOf(false) }
    var styleLoaded by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var showCreatePost by remember { mutableStateOf(false) }
    val postViewModel: PostViewModel = viewModel(factory = PostViewModelFactory(PostRepositoryImpl()))

    // 位置情報を取得（既存の GetLocation を利用）
    LaunchedEffect(Unit) {
        if (this is ComponentActivity) {
            GetLocation(this,  LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // Mapbox カメラ状態
    val viewportState = rememberMapViewportState {
        println("Camera position: ${LocationViewModel.locationState.location}")
        setCameraOptions {
            zoom(16.5)
            center(
                Point.fromLngLat(
                    LocationViewModel.locationState.location?.lng ?: 0.0,
                    LocationViewModel.locationState.location?.lat ?: 0.0
                )
            )
            pitch(0.0)
            bearing(0.0)
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar { dest ->
                when(dest){
                    BottomDestination.Home -> { /* TODO navigate home */ }
                    BottomDestination.Map -> { /* current */ }
                    BottomDestination.Event -> { /* TODO */ }
                    BottomDestination.Thread -> { /* TODO */ }
                }
            }
        }
    ) { innerPadding ->
        // innerPadding を適用して画面本体を表示
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = viewportState,
                    style = { Style.STANDARD }
                ) {
                    MapEffect(Unit) { mapView ->
                        if (mapView.getMapboxMap().style == null && !styleLoaded) {
                            mapView.getMapboxMap().loadStyleUri(Style.STANDARD) { _ ->
                                styleLoaded = true
                            }
                        } else if (mapView.getMapboxMap().style != null) {
                            styleLoaded = true
                        }
                    }
                    MapEffect(LocationViewModel.locationState.location) { mapView ->
                        val plugin = mapView.location
                        plugin.updateSettings { enabled = true; pulsingEnabled = true }
                    }
                }
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    onClick = {
                        is3D = !is3D
                        ToggleDimension(viewportState, is3D)
                    }
                ) { Text(text = if (is3D) "2D" else "3D") }
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    onClick = { showPopup = true }
                ) { Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                CreatePostDialog(
                    isOpen = showCreatePost,
                    onClose = { showCreatePost = false },
                    selectedCategoryFilter = null,
                )
                SelectPopupOverlay(
                    visible = showPopup,
                    onDismiss = { showPopup = false },
                    onPostCreated = {showCreatePost = true}
                )
                if (!styleLoaded) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("地図スタイル読み込み中…", color = Color.DarkGray)
                    }
                }
            }
        }
    }
}
