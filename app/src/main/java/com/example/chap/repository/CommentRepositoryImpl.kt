package com.example.chap.repository

import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import com.example.chap.location.LocationProvider
import com.example.chap.models.Comment
import com.example.chap.models.RequestComment
import com.example.chap.models.Coordinate
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CommentRepositoryImpl @Inject constructor(private val locationProvider: LocationProvider) : CommentRepository {
	private val _comments = MutableStateFlow<List<Comment>>(emptyList())
	override val comments: StateFlow<List<Comment>> = _comments.asStateFlow()


    override suspend fun getCommentsByThreadID(threadID: String): Result<List<Comment>>  {
        val coordinate = locationProvider.current()
		return try {
			val response = ApiClient.request(
                url = ApiEndpoints.Comments.getCommentsByThread(threadID),
                method = "GET",
                body = mapOf(
                    "threadId" to threadID,
                    )
                )

            if (response != null) {
                val root = JSONObject(response)
                val array = root.optJSONArray("replies") ?: root.optJSONArray("comments") ?: JSONArray()

                val commentList= parseComment(array).getOrThrow()

				_comments.value = commentList
				Result.success(commentList)
			} else {
				Result.failure(Exception("No response"))
			}
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

    override suspend fun createComment(comment:RequestComment): Result<String> {
		return try {
			val response = ApiClient.request(
                url = ApiEndpoints.Comments.CREATE,
                method = "POST",
                body = mapOf(
                    "threadId" to comment.threadId,
                    "content" to comment.content,
                )
            )
			if (response != null) {
				Result.success(response)
			} else {
				Result.failure(Exception("No response"))
			}
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	fun parseComment(replies: JSONArray): Result<List<Comment>> {
    return try {

        val comments = mutableListOf<Comment>()
        for (i in 0 until replies.length()) {
            val obj = replies.optJSONObject(i) ?: continue

            // coordinate
            val coordObj = obj.optJSONObject("coordinate")
            val coordinate = Coordinate(
                lat = coordObj?.optDouble("lat", 0.0) ?: 0.0,
                lng = coordObj?.optDouble("lng", 0.0) ?: 0.0
            )

            // tags
            val tagsArr = obj.optJSONArray("tags")
            val tags = mutableListOf<String>()
            if (tagsArr != null) {
                for (j in 0 until tagsArr.length()) {
                    tags.add(tagsArr.optString(j))
                }
            }

            val comment = Comment(
                id = obj.optString("id", obj.optString("comment_id", "")),
                createdAt = obj.optString("created_at", obj.optString("createdAt", "")),
                updatedAt = obj.optString("updated_at", obj.optString("updatedAt", "")),
                userId = obj.optString("user_id", obj.optString("userId", "")),
                userName = obj.optString("username", obj.optString("userName", "")),
                coordinate = coordinate,
                content = obj.optString("content", ""),
                threadId = obj.optString("thread_id", obj.optString("threadId", "")),
                likeCount = obj.optLong("likeCount", 0),
				likes = parseLikes(obj.optJSONArray("likes")),
                image = obj.optString("image", ""),
            )

            comments.add(comment)
        }

        Result.success(comments)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
}
private fun parseLikes(array: JSONArray?): List<String> {
    if (array == null) return emptyList()
    return List(array.length()) { i -> array.optString(i, "") }
}
