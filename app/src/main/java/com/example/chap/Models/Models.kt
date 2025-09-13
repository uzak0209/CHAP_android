package com.example.chap.Models

data class Coordinate(
    val lat: Double,
    val lng: Double
)
enum class CreateKind{
    POST,THREAD,EVENT;
}
// 投稿作成リクエスト DTO (UI -> API)
data class PostCreateRequest(
    val content: String,
    val category: String,
    val tags: List<String>,
    val coordinate: Coordinate,
    val visible: Boolean,
    val valid: Boolean
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
    val coordinate: Coordinate,
    val content: String,
    val valid: Boolean,
    val thread_id: Long,
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
    val eventdate: Long,
    val created_at: String,
    val updated_at: String,
    val deleted_at: String?,
    val username: String,
    val user_id: String,
    val coordinate: Coordinate,
    val category: String,
    val content: String,
    val valid: Boolean,
    val like: Int,
    val tags: List<String>
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


data class RequestComment(
    val content:String,
    val like:Int,
    val tags:List<String>,
    val valid:Boolean,
    val thread_id: Long
)
// ヒートマップポイント