package com.example.chap.Screens.Map

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.chap.components.map.SlidBar
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
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.gestures.gestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.collectAsState
import com.example.chap.ui.theme.BrandBlue
import kotlinx.coroutines.launch
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


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
    var showPopup by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var createKind by remember { mutableStateOf(CreateKind.POST) }
    val posts by postViewModel.posts.collectAsState()
    val threads by threadViewModel.threads.collectAsState()
    val events by eventViewModel.events.collectAsState()

    // 位置情報を取得（既存の GetLocation を利用）
    LaunchedEffect(Unit) {
        postViewModel.getAllPosts()
        threadViewModel.getAllThreads()
        eventViewModel.getAllEvents()
        if (this is ComponentActivity) {
            GetLocation(this, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // Mapbox カメラ状態
    val currentLocation = LatLng(
        LocationViewModel.locationState.location?.lat ?: 35.6762,
        LocationViewModel.locationState.location?.lng ?: 139.6503
    )

    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(currentLocation, 16f)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // No-op: scale bar is disabled permanently after style load
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            SlidBar(
                onNavigateHome = onNavigateHome,
                onNavigateEvent = onNavigateEvent,
                onNavigateThread = onNavigateThread,
            )
        }
    ) {
        Scaffold{ innerPadding ->
            // innerPadding を適用して画面本体を表示
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize())
                {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            mapType = MapType.NORMAL,
                            isMyLocationEnabled = true,
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = true,
                        ),
                        onMapClick = { latLng ->
                            // 地図クリック時の処理
                            println("Map clicked at: ${latLng.latitude}, ${latLng.longitude}")
                        }
                    ) {
                        posts.forEach { post ->
                            if (post.coordinate.lat != 0.0 || post.coordinate.lng != 0.0) {
                                val position = LatLng(post.coordinate.lat, post.coordinate.lng)
                                Marker(
                                    state = MarkerState(position = position),
                                    title = "POST: ${post.content}",
                                    snippet = "Category: ${post.category}\nDate: ${post.updated_at}",
                                    icon = BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_BLUE
                                    ),
                                    onClick = { marker ->
                                        println("Post marker clicked: ${post.id}")
                                        false
                                    }
                                )
                            }
                        }
                        threads.forEach { thread ->
                            if (thread.coordinate.lat != 0.0 || thread.coordinate.lng != 0.0) {
                                val position =
                                    LatLng(thread.coordinate.lat, thread.coordinate.lng)
                                Marker(
                                    state = MarkerState(position = position),
                                    title = "THREAD: ${thread.content}",
                                    snippet = "Category: ${thread.category}\nDate: ${thread.updated_at}",
                                    icon = BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_ORANGE
                                    ),
                                    onClick = { marker ->
                                        println("Thread marker clicked: ${thread.id}")
                                        onNavigateThread()
                                        false
                                    }
                                )
                            }
                        }

                        // Events のマーカーを表示
                        events.forEach { event ->
                            if (event.coordinate.lat != 0.0 || event.coordinate.lng != 0.0) {
                                val position = LatLng(event.coordinate.lat, event.coordinate.lng)
                                Marker(
                                    state = MarkerState(position = position),
                                    title = "EVENT: ${event.content}",
                                    snippet = "Category: ${event.category}\nDate: ${event.updated_at}",
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                                    onClick = { marker ->
                                        println("Event marker clicked: ${event.id}")
                                        false
                                    }
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingActionButton(
                            modifier = Modifier,
                            containerColor = BrandBlue,
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) { Icon(Icons.Default.Menu, contentDescription = "Open navigation", tint = Color.White) }
                    }
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        onClick = { showPopup = true },
                        containerColor = BrandBlue,
                    ) { Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White) }

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
                        onPostCreated = {
                            showPopup = false; createKind = CreateKind.POST; showCreate = true
                        },
                        onThreadCreated = {
                            showPopup = false; createKind = CreateKind.THREAD; showCreate = true
                        },
                        onEventCreated = {
                            showPopup = false; createKind = CreateKind.EVENT; showCreate = true
                        }
                    )
                }
            }
        }
    }
}
