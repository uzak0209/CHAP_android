package com.example.chap.domain.repository
import com.example.chap.Models.Coordinate
import com.example.chap.Models.Post
import com.example.chap.Models.User
import com.example.chap.domain.model.PostId

interface PostRepository {
    suspend fun findById(id: PostId): Post?
    suspend fun findAllPublic(): List<Post>
    suspend fun findAllHome(): List<Post>
    suspend fun create(
        id: Long,
        type: String,
        created_at: String,
        updated_at: String,
        deleted_at: String?,
        user_id: String,
        username: String,
        user: User,
        coordinate: Coordinate,
        content: String,
        category: String,
        valid: Boolean,
        like: Int,
        tags: List<String>
    ): Post
    suspend fun delete(post: Post)
}

// API連携用の実装クラス
import com.example.chap.API.ApiClient
import com.example.chap.API.ApiEndpoints
import com.example.chap.API.LocationViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter
import android.os.Build
import androidx.annotation.RequiresApi

class PostRepositoryImpl : PostRepository {
    suspend fun getAllPostsApi(): Result<String> {
        return try {
            val response = ApiClient.request(
                url = ApiEndpoints.Posts.LIST,
                method = "POST",
                body = mapOf(
                    "lat" to LocationViewModel.location?.lat.toString(),
                    "lng" to LocationViewModel.location?.lng.toString()
                )
            )
            Result.success(response.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createPostApi(post: Post): Result<String> {
        return try {
            val coordinateMap = LocationViewModel.location?.let { loc ->
                mapOf(
                    "lat" to loc.lat.toString(),
                    "lng" to loc.lng.toString()
                )
            } ?: emptyMap()

            val formatted = getCurrentTimeISO()
            val requestBody = mapOf(
                "category" to post.category,
                "content" to post.content,
                "coordinate" to coordinateMap,
                "created_at" to formatted,
                "like" to "0",
                "tags" to post.tags,
                "type" to "post",
                "valid" to "true"
            )
            val response = ApiClient.request(
                url = ApiEndpoints.Posts.CREATE,
                method = "POST",
                body = requestBody
            )
            Result.success(response.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTimeISO(): String {
        val now = Instant.now()
        return DateTimeFormatter.ISO_INSTANT.format(now)
    }

    // 既存メソッドは未実装のまま
    override suspend fun findById(id: PostId): Post? = null
    override suspend fun findAllPublic(): List<Post> = emptyList()
    override suspend fun findAllHome(): List<Post> = emptyList()
    override suspend fun create(
        id: Long,
        type: String,
        created_at: String,
        updated_at: String,
        deleted_at: String?,
        user_id: String,
        username: String,
        user: User,
        coordinate: Coordinate,
        content: String,
        category: String,
        valid: Boolean,
        like: Int,
        tags: List<String>
    ): Post = throw NotImplementedError()
    override suspend fun delete(post: Post) {}
}