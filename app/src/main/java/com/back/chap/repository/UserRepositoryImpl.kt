package com.back.chap.repository

import android.util.Base64
import com.back.chap.AppContextHolder
import com.back.chap.api.ApiClient
import com.back.chap.api.ApiEndpoints
import com.back.chap.auth.TokenManager
import com.back.chap.models.User
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor() : UserRepository {

    override suspend fun updateUserToDatabase(userId: Long, updates: Map<String, Any?>) {
        val body: Map<String, Any> = updates
            .filterValues { it != null }
            .mapValues { it.value as Any }

        ApiClient.request(
            url = ApiEndpoints.Users.edit(userId.toString()),
            method = "POST",
            body = body
        )
    }

    override suspend fun getUserById(userId: Long): User? {
        val response = ApiClient.request(
            url = ApiEndpoints.Users.get(userId.toString()),
            method = "GET"
        ) ?: return null

        val root = JSONObject(response)
        val userObj = if (root.has("user")) root.getJSONObject("user") else root

        return User(
            id = userObj.optString("id", ""),
            name = userObj.optString("name", ""),
            image = if (userObj.isNull("image")) null else userObj.optString("image", null),
            email = userObj.optString("email", ""),
            createdAt = userObj.optString("created_at", ""),
            password = userObj.optString("password", ""),
            updatedAt = userObj.optString("updated_at", ""),
            description = userObj.optString("description", ""),
            followerCount = userObj.optLong("followerCount", 0),
            followingCount = userObj.optLong("followingCount", 0),
            followers = parsefollow(userObj.optJSONArray("followers")),
            followings = parsefollow(userObj.optJSONArray("followings")),
        )
    }

    override suspend fun getCurrentUser(): User? {
        // Try API first
        val response = runCatching {
            ApiClient.request(
                url = ApiEndpoints.Auth.VERIFY,
                method = "GET"
            )
        }.getOrNull()

        if (response != null) {
            val root = JSONObject(response)
            val userObj = findUserObject(root)

            val id = extractId(userObj)
            val name = extractName(userObj)

            println("[UserRepository] /auth/me parsed id=$id name=$name")

            if (id.isNotBlank() || name.isNotBlank()) {
                return User(
                    id = id,
                    name = name,
                    image = if (userObj.isNull("image")) null else userObj.optString("image", null),
                    email = userObj.optString("email", ""),
                    createdAt = userObj.optString("created_at", ""),
                    password = userObj.optString("password", ""),
                    updatedAt = userObj.optString("updated_at", ""),
                    description = userObj.optString("description", ""),
                    followerCount = userObj.optLong("followerCount", 0),
                    followingCount = userObj.optLong("followingCount", 0),
                    followers = parsefollow(userObj.optJSONArray("followers")),
                    followings = parsefollow(userObj.optJSONArray("followings")),
                )
            }
        }

        // Fallback: decode JWT payload if API failed or missing fields
        val token = runCatching { TokenManager(AppContextHolder.appContext).getToken() }.getOrNull()
        val jwtUser = token?.let { decodeUserFromJwt(it) }
        if (jwtUser == null) {
            println("[UserRepository] JWT decode failed or missing id/name")
        } else {
            println("[UserRepository] JWT decoded id=${'$'}{jwtUser.id} name=${'$'}{jwtUser.name}")
        }
        if (jwtUser != null) return jwtUser

        return null
    }

    private fun decodeUserFromJwt(jwt: String): User? {
        val parts = jwt.split('.')
        if (parts.size < 2) return null
        return try {
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payload = JSONObject(String(payloadBytes))

            // Some JWTs embed the user object under "user"
            val claimsUser = if (payload.has("user")) payload.optJSONObject("user") else null
            val id = extractId(claimsUser ?: payload)
            val name = extractName(claimsUser ?: payload)
            if (id.isBlank() && name.isBlank()) return null

            User(
                id = id,
                name = name,
                image = null,
                email = payload.optString("email", ""),
                createdAt = "",
                password = "",
                updatedAt = "",
                description = "",
                followerCount = 0,
                followingCount = 0,
                followers = emptyList(),
                followings = emptyList(),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun findUserObject(root: JSONObject): JSONObject {
        // Prefer nested objects commonly used to wrap the user
        val candidates = listOf("user", "data", "me", "account", "profile")
        var obj = root
        for (key in candidates) {
            if (obj.has(key)) {
                val next = obj.opt(key)
                if (next is JSONObject) {
                    // If we found data and it contains user, dive deeper
                    if (key == "data" && next.has("user") && next.opt("user") is JSONObject) {
                        return next.getJSONObject("user")
                    }
                    return next
                }
            }
        }
        return obj
    }

    private fun extractId(obj: JSONObject): String {
        val keys = listOf("id", "_id", "userId", "user_id", "uid", "sub")
        for (k in keys) {
            if (obj.has(k)) return obj.opt(k).toString()
        }
        return ""
    }

    private fun extractName(obj: JSONObject): String {
        val keys = listOf("name", "displayName", "userName", "username", "nickname")
        for (k in keys) {
            if (obj.has(k)) return obj.opt(k).toString()
        }
        return ""
    }
    private fun parsefollow(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return List(array.length()) { i -> array.optString(i, "") }
    }
}
