package com.example.chap.models

data class Coordinate(
    val lat: Double,
    val lng: Double
)

interface Mappable {
    val coordinate: Coordinate
}

enum class LoginTab {
    Login, SignUp
}
enum class CreateKind{
    POST,THREAD,EVENT,SPOT;
}
// 投稿作成リクエスト DTO (UI -> API)(ここでのPostは投稿って意味)
data class PostCreateRequest(
    val content: String,
    val category: String,
    val coordinate: Coordinate,
    val visible: Boolean,
)

//data class CommentCreateRequest(
//    val content: String,
//    val visible: Boolean,
//    val valid: Boolean,
//    val thread_id:Long
//)
enum class PostCategory{
        ENTERTAINMENT,DISASTER,COMMUNITY;

    override fun toString(): String {
        return name.lowercase()
    }
}
// ユーザー
data class User(
    val id: Long,
    val name: String,
    val image: String?,
    val email: String, //データベースにメールがない？
    val createdAt: String,
    val password: String,
    val updatedAt: String,
)

// 投稿

data class Post(
    val id: Long,
    val userName: String,
    val userId: String,
    val userImage: String,
    val createdAt: String,
    val updatedAt: String,
    override val coordinate: Coordinate,
    val content: String,
    val category: String,
    val likes: List<String>,
    val likeCount: Long,
) : Mappable

// コメント
data class Comment(
    val id: Long,
    val userId: String,
    val userName: String,
    val image: String,
    val threadId: Long,
    val createdAt: String,
    val updatedAt: String,
    val coordinate: Coordinate,
    val content: String,
    val likeCount: Long,
    val likes: List<String>,
)

// スレッド
data class Thread(
    val id: Long,
    val userId: String,
    val createdAt: String,
    val updatedAt: String,
    val userName: String,
    override val coordinate: Coordinate,
    val category: String,
    val content: String,
    val likeCount: Long,
    val likes: List<String>,
) : Mappable

// イベント
data class Event(
    val id: Long,
    val createdAt: String,
    val updatedAt: String,
    val userName: String,
    val userId: String,
    override val coordinate: Coordinate,
    val category: String,
    val content: String,
    val likes: List<String>,
    val likeCount: Long,
) : Mappable

data class Spot(
    val id: Long,
    val createdAt: String,
    val updatedAt: String,
    val userId: String,
    val userName: String,
    override val coordinate: Coordinate,
    val content: String,
) : Mappable

data class RequestComment(
    val content:String,
    val threadId: Long
)
enum class Status{
    LOADING,LOADED,ERROR
}
data class LocationState(
    var location: Coordinate?,
    var status: Status
)