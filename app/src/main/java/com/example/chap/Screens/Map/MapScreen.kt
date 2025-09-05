package com.example.chap.Screens.Map

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chap.API.LocationViewModel
import com.example.chap.Models.CreateKind
import com.example.chap.components.CreateDialog
import com.example.chap.components.SelectPopupOverlay
import com.example.chap.components.ToggleDimension
import com.example.chap.components.ui.AppBottomBar
import com.example.chap.components.ui.BottomDestination
import com.example.chap.libs.GetLocation
import com.example.chap.libs.LOCATION_PERMISSION_REQUEST_CODE
import com.example.chap.components.map.LocationState
import com.example.chap.components.map.LoadStatus
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chap.API.PostViewModel
import com.example.chap.API.ThreadViewModel
import com.example.chap.API.EventViewModel
import com.example.chap.components.ToggleDimension
import com.example.chap.components.map.SubmitCategory
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.locationcomponent.location


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MapScreen(
    onNavigateHome: () -> Unit,
    onNavigateEvent: () -> Unit,
    onNavigateThread: () -> Unit,
    postViewModel: PostViewModel,
    threadViewModel: ThreadViewModel,
    eventViewModel: EventViewModel
) {
    // Compose で ViewModel の位置情報を監視
    var is3D by remember { mutableStateOf(false) }
    var styleLoaded by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var createKind by remember { mutableStateOf(CreateKind.POST) }

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
                    BottomDestination.Home -> onNavigateHome()
                    BottomDestination.Map -> { /* current */ }
                    BottomDestination.Event -> { onNavigateEvent() }
                    BottomDestination.Thread -> { onNavigateThread() }
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
                        val mbMap = mapView.mapboxMap
                        if (!styleLoaded) {
                            mbMap.loadStyleUri(Style.STANDARD) { styleLoaded = true }
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

                CreateDialog(
                    isOpen = showCreate,
                    onClose = { showCreate = false },
                    selectedKind = createKind,
                    postViewModel = postViewModel,
                    threadViewModel = threadViewModel,
                    eventViewModel = eventViewModel
                )

                SelectPopupOverlay(
                    visible = showPopup,
                    onDismiss = { showPopup = false },
                    onPostCreated = { showPopup = false; createKind = CreateKind.POST; showCreate = true },
                    onThreadCreated = { showPopup = false; createKind = CreateKind.THREAD; showCreate = true },
                    onEventCreated = { showPopup = false; createKind = CreateKind.EVENT; showCreate = true }
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
