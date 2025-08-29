package com.example.chap.Screens.Map

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.example.chap.API.LocationViewModel
import com.example.chap.Models.Post
import com.example.chap.libs.GetLocation
import com.example.chap.libs.LOCATION_PERMISSION_REQUEST_CODE
import com.example.chap.R
import com.example.chap.components.SelectPopupOverlay
import com.example.chap.components.CreatePostDialog
import com.example.chap.components.map.LocationState as MapLocationState
import com.example.chap.components.map.LoadStatus as MapLoadStatus
import com.example.chap.components.map.LatLng as MapLatLng
import com.example.chap.store.PostsViewModel
import com.example.chap.store.PostsRepository
import com.example.chap.store.PostCreateRequest
import com.example.chap.store.Post
import com.example.chap.store.AroundRequest
import com.example.chap.components.ToggleDimension
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.locationcomponent.location


@Composable
fun MapScreen() {
    // Compose で ViewModel の位置情報を監視
    var is3D by remember { mutableStateOf(false) }
    val lastLocation by remember { derivedStateOf { LocationViewModel.location } }
    var styleLoaded by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var showCreatePost by remember { mutableStateOf(false) }

    // 簡易 Repository (本番は差し替え)
    val postsRepo = remember {
        object : PostsRepository {
            private var id = 1
            private val items = mutableListOf<Post>()
            override suspend fun fetchPosts(): List<Post> = items.toList()
            override suspend fun fetchAround(req: AroundRequest): List<Post> = items.toList()
            override suspend fun create(post: PostCreateRequest): Post {
                val p = Post(
                    id = id++, userId = 0, content = post.content, category = post.category,
                    tags = post.tags, coordinate = post.coordinate, like = 0,
                    createdTime = null, updatedAt = null, visible = post.visible, valid = post.valid
                ); items.add(0, p); return p
            }
            override suspend fun fetchPost(id: Int): Post = items.first { it.id == id }
            override suspend fun update(id: Int, patch: Map<String, Any?>): Post { return fetchPost(id) }
            override suspend fun delete(id: Int) { items.removeAll { it.id == id } }
        }
    }
    val postsViewModel = remember { PostsViewModel(postsRepo) }

    // 位置情報を取得（既存の GetLocation を利用）
    LaunchedEffect(Unit) {
        if (this is ComponentActivity) {
            GetLocation(this,  LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // Mapbox カメラ状態
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(16.5)
            center(
                Point.fromLngLat(
                    lastLocation?.lng ?: 0.0,
                    lastLocation?.lat ?: 0.0
                )
            )
            pitch(0.0)
            bearing(0.0)
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 55.dp),

                        ) {
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_home_24),
                                contentDescription = "Home",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(32.dp))

                        IconButton(onClick = { /* TODO */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_map_24),
                                contentDescription = "Map",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_flag_24),
                                contentDescription = "Event",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(32.dp))
                        IconButton(onClick = { /* TODO */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_mode_comment_24),
                                contentDescription = "Thread",
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        // innerPadding を適用して画面本体を表示
        Surface(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize())
        {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = viewportState,
                    style = { Style.STANDARD }
                ) {
                    // スタイルロード完了管理
                    MapEffect(Unit) { mapView ->
                        if (mapView.getMapboxMap().style == null && !styleLoaded) {
                            mapView.getMapboxMap().loadStyleUri(Style.STANDARD) { _ ->
                                styleLoaded = true
                            }
                        } else if (mapView.getMapboxMap().style != null) {
                            styleLoaded = true
                        }
                    }

                    // 位置情報プラグイン設定
                    MapEffect(lastLocation) { mapView ->
                        val plugin = mapView.location
                        plugin.updateSettings {
                            enabled = true
                            pulsingEnabled = true
                        }
                    }
                }
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    onClick = {
                        is3D = !is3D
                        ToggleDimension(viewportState,is3D)
                    }
                ) {
                    Text(text = if (is3D) "2D" else "3D")
                }
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    onClick = { showPopup = true}
                ){
                    Text( text = "+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold )
                }
                SelectPopupOverlay(
                    visible = showPopup,
                    onDismiss = { showPopup = false },
                    onRequestCreatePost = { showCreatePost = true }
                )
                if (showCreatePost) {
                    CreatePostDialog(
                        isOpen = showCreatePost,
                        onClose = { showCreatePost = false },
                        locationState = MapLocationState(
                            status = if (lastLocation != null) MapLoadStatus.LOADED else MapLoadStatus.IDLE,
                            location = MapLatLng(lastLocation?.lat ?: 0.0, lastLocation?.lng ?: 0.0)
                        ),
                        selectedCategoryFilter = null,
                        viewModel = postsViewModel
                    )
                }
                // スタイル未読込時の表示
                if (!styleLoaded) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("地図スタイル読み込み中…", color = Color.DarkGray)
                    }
                }
            }
        }
    }
}
@Composable
fun CreatePostForm(){

}
@Composable
fun CreateThreadForm(){

}
@Composable
fun CreateEventForm(){

}