package com.example.chap.components.map

/**
 * Go / TypeScript 定義を Kotlin に変換したドメインモデル群。
 * ネットワーク層とのマッピングを簡易にするため文字列日時 (ISO8601) を保持。
 * 将来 kotlinx.serialization や Moshi を使う場合は @Serializable 等を追加してください。
 */

// ---- Category Enums -------------------------------------------------------

enum class PostCategory { ENTERTAINMENT, COMMUNITY, INFORMATION, DISASTER }

enum class EventCategory { ENTERTAINMENT, COMMUNITY, INFORMATION, DISASTER, FOOD, EVENT }

// ---- Primitive Value Objects ---------------------------------------------

data class Coordinate(
  val lat: Double,
  val lng: Double
)

typealias LatLng = Coordinate

// ---- Core Entities --------------------------------------------------------

data class Post(
  val id: Long,
  val userId: String,
  val coordinate: Coordinate,
  val createdAt: String,
  val deletedAt: String? = null,
  val updatedAt: String? = null,
  val content: String,
  val category: PostCategory,
  val valid: Boolean,
  val like: Int,
  val tags: List<String> = emptyList(),
  val visible: Boolean
)

data class Thread(
  val id: Long,
  val userId: String,
  val coordinate: Coordinate,
  val createdAt: String,
  val deletedAt: String? = null,
  val updatedAt: String? = null,
  val content: String,
  val category: PostCategory, // Thread も Post と同一カテゴリ扱い
  val valid: Boolean,
  val like: Int,
  val tags: List<String> = emptyList(),
  val visible: Boolean
)

data class Event(
  val id: Long,
  val userId: String,
  val coordinate: Coordinate,
  val createdAt: String,
  val deletedAt: String? = null,
  val updatedAt: String? = null,
  val content: String,
  val category: EventCategory,
  val valid: Boolean,
  val like: Int,
  val tags: List<String> = emptyList(),
  val visible: Boolean
)

data class Comment(
  val id: Long,
  val userId: String,
  val content: String,
  val createdAt: String,
  val updatedAt: String? = null,
  val deletedAt: String? = null,
  val threadId: Long
)

data class User(
  val id: String, // UUID
  val name: String
)


// ---- Permission / Status --------------------------------------------------

enum class PermissionState { GRANTED, DENIED, PROMPT, UNKNOWN }

enum class LoadStatus { LOADING, LOADED, ERROR, IDLE }

// ---- Location State -------------------------------------------------------

data class LocationState(
  val status: LoadStatus,
  val location: LatLng,
  val error: String? = null
)

// ---- Listener Interface ---------------------------------------------------

/**
 * マップ上のデータ更新イベントを受け取りたい層（ViewModel / Repository 呼び出し結果連携など）で実装。
 * 必要なメソッドだけオーバーライドできるようデフォルト実装付き。
 */
interface MapEventListener {
  fun onPostsUpdated(posts: List<Post>) {}
  fun onThreadsUpdated(threads: List<Thread>) {}
  fun onEventsUpdated(events: List<Event>) {}
  fun onCommentsUpdated(comments: List<Comment>) {}
  fun onError(error: Throwable) {}
}

// ---- Convenience Builders (Optionals) ------------------------------------

fun emptyLocationState(): LocationState =
  LocationState(status = LoadStatus.IDLE, location = LatLng(0.0, 0.0), error = null)

