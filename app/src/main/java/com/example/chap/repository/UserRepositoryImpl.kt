package com.example.chap.repository

import com.example.chap.api.ApiClient
import com.example.chap.api.ApiEndpoints
import com.example.chap.models.User
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
            id = userObj.optLong("id", userId),
            name = userObj.optString("name", ""),
            image = if (userObj.isNull("image")) null else userObj.optString("image", null),
            email = userObj.optString("email", ""),
            created_at = userObj.optString("created_at", ""),
            valid = userObj.optBoolean("valid", true),
            password = userObj.optString("password", ""),
            login_type = if (userObj.isNull("login_type")) null else userObj.optString("login_type", null),
            updated_at = userObj.optString("updated_at", ""),
            deleted_at = if (userObj.isNull("deleted_at")) null else userObj.optString("deleted_at", null)
        )
    }

    override suspend fun getCurrentUser(): User? {
        val response = ApiClient.request(
            url = ApiEndpoints.Auth.VERIFY,
            method = "GET"
        ) ?: return null

        val root = JSONObject(response)
        val userObj = if (root.has("user")) root.getJSONObject("user") else root

        return User(
            id = userObj.optLong("id", 0L),
            name = userObj.optString("name", ""),
            image = if (userObj.isNull("image")) null else userObj.optString("image", null),
            email = userObj.optString("email", ""),
            created_at = userObj.optString("created_at", ""),
            valid = userObj.optBoolean("valid", true),
            password = userObj.optString("password", ""),
            login_type = if (userObj.isNull("login_type")) null else userObj.optString("login_type", null),
            updated_at = userObj.optString("updated_at", ""),
            deleted_at = if (userObj.isNull("deleted_at")) null else userObj.optString("deleted_at", null)
        )
    }
}
