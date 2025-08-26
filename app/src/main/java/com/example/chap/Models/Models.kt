package com.example.chap.Models


// 緯度経度
data class Coordinate(
    val lat: Double,
    val lng: Double
)

// ユーザー
data class User(
    val id: String,
    val name: String,
    val image: String?,
    val email: String,
    val created_at: String,
    val valid: Boolean,
    val password: String,
    val login_type: String?,
    val updated_at: String,
    val deleted_at: String?
)

// 投稿
data class Post(
    val id: Long,
    val type: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val user_id: String,
    val username: String,
    val user: User,
    val coordinate: Coordinate,
    val content: String,
    val category: String,
    val valid: Boolean,
    val like: Int,
    val tags: List<String>
)

// コメント
data class Comment(
    val id: Long,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val user_id: String,
    val username: String,
    val user: User,
    val coordinate: Coordinate,
    val content: String,
    val valid: Boolean,
    val thread_id: Long,
    val thread: Thread,
    val like: Int,
    val tags: List<String>
)

// スレッド
data class Thread(
    val id: Long,
    val type: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val username: String,
    val user_id: String,
    val user: User,
    val coordinate: Coordinate,
    val category: String,
    val content: String,
    val valid: Boolean,
    val like: Int,
    val tags: List<String>
)

// イベント
data class Event(
    val id: Long,
    val type: String,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val username: String,
    val user_id: String,
    val user: User,
    val coordinate: Coordinate,
    val category: String,
    val content: String,
    val valid: Boolean,
    val like: Int,
    val tags: List<String>
)

// 投稿のいいね
data class PostLikes(
    val user_id: String,
    val post_id: Long,
    val user: User,
    val post: Post
)

// スレッドのいいね
data class ThreadLikes(
    val user_id: String,
    val thread_id: Long,
    val user: User,
    val thread: Thread
)

// イベントのいいね
data class EventLikes(
    val user_id: String,
    val event_id: Long,
    val user: User,
    val event: Event
)

// Email ログイン
data class EmailLogin(
    val user_id: String,
    val user: User,
    val email: String,
    val password: String
)

// Google ログイン
data class GoogleLogin(
    val user_id: String,
    val user: User,
    val access_token: String,
    val email: String,
    val name: String
)

// Thread と Comment IDs の紐付け
data class ThreadTable(
    val thread_id: Long,
    val thread: Thread,
    val comment_ids: List<Long>
)

// ヒートマップポイント
data class HeatmapPoint(
    val lat: Double,
    val lng: Double,
    val value: Int
)