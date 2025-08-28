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

    suspend fun delete(
        post: Post
    )
}