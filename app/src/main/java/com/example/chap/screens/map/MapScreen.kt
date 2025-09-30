package com.example.chap.screens.map

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.chap.models.CreateKind
import com.example.chap.components.CreateDialog
import com.example.chap.components.SelectPopupOverlay
import com.example.chap.components.ToggleDimension
import com.example.chap.components.map.SlidBar
import com.example.chap.components.map.PostPopup
import com.example.chap.components.map.ThreadMarker
import com.example.chap.components.map.ThreadPopup
import com.example.chap.components.map.EventMarker
import com.example.chap.components.map.EventPopup
import com.example.chap.libs.GetLocation
import com.example.chap.libs.LOCATION_PERMISSION_REQUEST_CODE
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotationGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.chap.models.Post
import com.example.chap.models.Thread
import com.example.chap.models.Event as EventModel
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Menu
import com.example.chap.screens.event.EventViewModel
import com.example.chap.screens.post.PostViewModel
import com.example.chap.screens.thread.ThreadViewModel
import com.example.chap.ui.theme.BrandBlue
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import kotlinx.coroutines.launch


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
    var is3D by remember { mutableStateOf(true) }
    var styleLoaded by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var createKind by remember { mutableStateOf(CreateKind.POST) }
    
    // ViewModelからデータを監視
    val posts by postViewModel.posts.collectAsState()
    val threads by threadViewModel.threads.collectAsState()
    val events by eventViewModel.events.collectAsState()
    
    // デバッグログ
    LaunchedEffect(posts.size) {
        println("[MapScreen] Posts count: ${posts.size}")
        posts.forEach { post ->
            println("[MapScreen] Post ID: ${post.id}, Content: ${post.content}, Lat: ${post.coordinate.lat}, Lng: ${post.coordinate.lng}")
        }
    }
    
    // 選択されたアイテムとポップアップ表示状態
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var selectedThread by remember { mutableStateOf<Thread?>(null) }
    var selectedEvent by remember { mutableStateOf<EventModel?>(null) }

    // 位置情報を取得（既存の GetLocation を利用）
    LaunchedEffect(Unit) {
        if (this is ComponentActivity) {
            GetLocation(this, LOCATION_PERMISSION_REQUEST_CODE)
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
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) {
                    MapboxMap(
                        modifier = Modifier.fillMaxSize(),
                        mapViewportState = viewportState,
                        style = { Style.STANDARD }
                    ) {
                        MapEffect(Unit) { mapView ->
                            val mbMap = mapView.mapboxMap
                            if (!styleLoaded) {
                                mbMap.loadStyleUri(Style.STANDARD) { 
                                    // スタイルロード後にピンアイコンを登録
                                    try {
                                        mbMap.style?.addImage("pin-purple", createMapPin(android.graphics.Color.parseColor("#9C27B0")))
                                        mbMap.style?.addImage("pin-red", createMapPin(android.graphics.Color.parseColor("#F44336")))
                                        mbMap.style?.addImage("pin-green", createMapPin(android.graphics.Color.parseColor("#4CAF50")))
                                        mbMap.style?.addImage("pin-blue", createMapPin(android.graphics.Color.parseColor("#2196F3")))
                                        mbMap.style?.addImage("pin-purple-light", createMapPin(android.graphics.Color.parseColor("#BA68C8")))
                                        mbMap.style?.addImage("pin-red-light", createMapPin(android.graphics.Color.parseColor("#EF5350")))
                                        mbMap.style?.addImage("pin-green-light", createMapPin(android.graphics.Color.parseColor("#66BB6A")))
                                        mbMap.style?.addImage("pin-blue-light", createMapPin(android.graphics.Color.parseColor("#42A5F5")))
                                        mbMap.style?.addImage("pin-purple-dark", createMapPin(android.graphics.Color.parseColor("#7B1FA2")))
                                        mbMap.style?.addImage("pin-red-dark", createMapPin(android.graphics.Color.parseColor("#D32F2F")))
                                        mbMap.style?.addImage("pin-green-dark", createMapPin(android.graphics.Color.parseColor("#388E3C")))
                                        mbMap.style?.addImage("pin-blue-dark", createMapPin(android.graphics.Color.parseColor("#1976D2")))
                                        println("[MapScreen] Map pin icons registered")
                                    } catch (e: Exception) {
                                        println("[MapScreen] Failed to register pins: ${e.message}")
                                    }
                                    styleLoaded = true
                                }
                            }
                        }
                        MapEffect(LocationViewModel.locationState.location) { mapView ->
                            val plugin = mapView.location
                            plugin.updateSettings { enabled = true; pulsingEnabled = true }
                        }
                        // Disable map gestures while popup overlay is visible OR drawer is open OR item is selected
                        MapEffect(Triple(showPopup, drawerState.currentValue, selectedPost != null || selectedThread != null || selectedEvent != null)) { mapView ->
                            runCatching { mapView.gestures }
                                .getOrNull()
                                ?.updateSettings {
                                    val allowMapGestures = !showPopup && 
                                                          drawerState.currentValue != DrawerValue.Open &&
                                                          selectedPost == null && 
                                                          selectedThread == null && 
                                                          selectedEvent == null
                                    scrollEnabled = allowMapGestures
                                    pinchToZoomEnabled = allowMapGestures
                                    rotateEnabled = allowMapGestures
                                    quickZoomEnabled = allowMapGestures
                                    pitchEnabled = allowMapGestures
                                }
                        }
                        // Permanently disable ScaleBar after style is loaded
                        MapEffect(styleLoaded) { mapView ->
                            if (styleLoaded) {
                                runCatching { mapView.scalebar }
                                    .getOrNull()
                                    ?.updateSettings { enabled = false }
                            }
                        }
                        
                        // 投稿マーカーをピンアイコンで表示
                        if (posts.isNotEmpty() && styleLoaded) {
                            PointAnnotationGroup(
                                annotations = posts.map { post ->
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(post.coordinate.lng, post.coordinate.lat))
                                        .withIconImage(when (post.category.lowercase()) {
                                            "entertainment" -> "pin-purple"
                                            "disaster" -> "pin-red"
                                            "community" -> "pin-green"
                                            else -> "pin-blue"
                                        })
                                        .withIconSize(0.4)
                                        .withIconAnchor(com.mapbox.maps.plugin.annotation.generated.IconAnchor.BOTTOM)
                                },
                                onClick = { annotation ->
                                    // マーカークリック時の処理
                                    val clickedPost = posts.find { post ->
                                        annotation.point.latitude() == post.coordinate.lat &&
                                        annotation.point.longitude() == post.coordinate.lng
                                    }
                                    selectedPost = clickedPost
                                    println("[MapScreen] Clicked post: ${clickedPost?.content}")
                                    true
                                }
                            )
                        }
                        
                        // スレッドマーカーをピンアイコンで表示
                        if (threads.isNotEmpty() && styleLoaded) {
                            PointAnnotationGroup(
                                annotations = threads.map { thread ->
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(thread.coordinate.lng, thread.coordinate.lat))
                                        .withIconImage(when (thread.category.lowercase()) {
                                            "entertainment" -> "pin-purple-light"
                                            "disaster" -> "pin-red-light"
                                            "community" -> "pin-green-light"
                                            else -> "pin-blue-light"
                                        })
                                        .withIconSize(0.4)
                                        .withIconAnchor(com.mapbox.maps.plugin.annotation.generated.IconAnchor.BOTTOM)
                                },
                                onClick = { annotation ->
                                    val clickedThread = threads.find { thread ->
                                        annotation.point.latitude() == thread.coordinate.lat &&
                                        annotation.point.longitude() == thread.coordinate.lng
                                    }
                                    selectedThread = clickedThread
                                    true
                                }
                            )
                        }
                        
                        // イベントマーカーをピンアイコンで表示
                        if (events.isNotEmpty() && styleLoaded) {
                            PointAnnotationGroup(
                                annotations = events.map { event ->
                                    PointAnnotationOptions()
                                        .withPoint(Point.fromLngLat(event.coordinate.lng, event.coordinate.lat))
                                        .withIconImage(when (event.category.lowercase()) {
                                            "entertainment" -> "pin-purple-dark"
                                            "disaster" -> "pin-red-dark"
                                            "community" -> "pin-green-dark"
                                            else -> "pin-blue-dark"
                                        })
                                        .withIconSize(0.4)
                                        .withIconAnchor(com.mapbox.maps.plugin.annotation.generated.IconAnchor.BOTTOM)
                                },
                                onClick = { annotation ->
                                    val clickedEvent = events.find { event ->
                                        annotation.point.latitude() == event.coordinate.lat &&
                                        annotation.point.longitude() == event.coordinate.lng
                                    }
                                    selectedEvent = clickedEvent
                                    true
                                }
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingActionButton(
                            modifier = Modifier,
                            containerColor = BrandBlue,
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) { Icon(Icons.Default.Menu, contentDescription = "Open navigation", tint = Color.White) }
                        FloatingActionButton(
                            modifier = Modifier,
                            containerColor = BrandBlue,
                            onClick = {
                                is3D = !is3D
                                ToggleDimension(viewportState, is3D)
                            }
                        ) { Text(text = if (is3D) "2D" else "3D", color = Color.White) }
                        FloatingActionButton(
                            modifier = Modifier,
                            containerColor = BrandBlue,
                            onClick = {
                                returnMyLocation(viewportState, scope)
                            }
                        ) { Icon(Icons.Default.LocationSearching, contentDescription = "Return to my location", tint = Color.White) }
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
                    
                    // 選択された投稿のポップアップ
                    selectedPost?.let { post ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { selectedPost = null },
                            contentAlignment = Alignment.Center
                        ) {
                            PostPopup(
                                post = post,
                                onDismiss = { selectedPost = null }
                            )
                        }
                    }
                    
                    // 選択されたスレッドのポップアップ
                    selectedThread?.let { thread ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { selectedThread = null },
                            contentAlignment = Alignment.Center
                        ) {
                            ThreadPopup(
                                thread = thread,
                                onDismiss = { selectedThread = null }
                            )
                        }
                    }
                    
                    // 選択されたイベントのポップアップ
                    selectedEvent?.let { event ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { selectedEvent = null },
                            contentAlignment = Alignment.Center
                        ) {
                            EventPopup(
                                event = event,
                                onDismiss = { selectedEvent = null }
                            )
                        }
                    }
                    
                    if (!styleLoaded) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("地図スタイル読み込み中…", color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 地図ピンアイコンを生成する関数（画像の形状に準拠）
 * @param color ピンの色
 * @return ビットマップ画像
 */
fun createMapPin(color: Int): Bitmap {
    val width = 100
    val height = 140
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        this.color = color
    }
    
    val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 6f
        this.color = android.graphics.Color.WHITE
    }
    
    val centerX = width / 2f
    val radius = 35f
    val circleY = 40f
    
    // ピンの外形パスを作成
    val pinPath = Path().apply {
        // 上部の円形部分を楕円として追加
        val ovalTop = RectF(centerX - radius, circleY - radius, centerX + radius, circleY + radius)
        addOval(ovalTop, Path.Direction.CW)
        
        // 下部の尖った部分（涙型）
        moveTo(centerX - radius, circleY)
        
        // 左側のカーブ
        cubicTo(
            centerX - radius, circleY + radius * 0.8f,  // コントロールポイント1
            centerX - radius * 0.3f, height - 20f,      // コントロールポイント2
            centerX, height - 10f                        // 終点（先端）
        )
        
        // 右側のカーブ
        cubicTo(
            centerX + radius * 0.3f, height - 20f,      // コントロールポイント1
            centerX + radius, circleY + radius * 0.8f,  // コントロールポイント2
            centerX + radius, circleY                    // 終点
        )
        
        close()
    }
    
    // 枠線を先に描画（下レイヤー）
    canvas.drawPath(pinPath, strokePaint)
    // 塗りつぶしを後に描画（上レイヤー）
    canvas.drawPath(pinPath, paint)
    
    // 中央の穴（オプション：画像のようにドーナツ型にする場合）
    val holePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        this.color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(centerX, circleY, radius * 0.4f, holePaint)
    
    return bitmap
}

/**
 * 地図の視点を現在地に戻す関数
 * @param viewportState Mapboxのビューポート状態
 * @param scope コルーチンスコープ
 */
fun returnMyLocation(
    viewportState: MapViewportState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val currentLocation = LocationViewModel.locationState.location
    
    if (currentLocation == null) {
        println("[ReturnMyLocation] 現在地が取得できていません")
        return
    }
    
    println("[ReturnMyLocation] カメラを現在地に移動: lat=${currentLocation.lat}, lng=${currentLocation.lng}")
    
    // カメラを現在地にアニメーションで移動
    scope.launch {
        viewportState.flyTo(
            cameraOptions = com.mapbox.maps.CameraOptions.Builder()
                .center(Point.fromLngLat(currentLocation.lng, currentLocation.lat))
                .zoom(16.5)
                .pitch(0.0)
                .bearing(0.0)
                .build(),
            animationOptions = MapAnimationOptions.mapAnimationOptions {
                duration(1000) // 1秒のアニメーション
            }
        )
    }
}
