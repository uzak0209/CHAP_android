package com.back.chap.repository

import android.content.Context
import com.back.chap.api.ApiClient
import com.back.chap.api.ApiEndpoints
import com.back.chap.auth.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

// Helper: download, compress and upload an image, return public URL or null
private suspend fun fetchCompressAndUploadImage(remoteUrl: String): String? {
    return try {
        val client = OkHttpClient()

        // 1) Download the image bytes
        println("[AuthRepository] Step 1: Downloading image from $remoteUrl")
        val getReq = Request.Builder().url(remoteUrl).get().build()
        val getResp = withContext(Dispatchers.IO) { client.newCall(getReq).execute() }
        if (!getResp.isSuccessful) {
            println("[AuthRepository] Failed to download image: ${getResp.code}")
            return null
        }
        val originalMime = getResp.header("Content-Type") ?: "image/jpeg"
        val originalBytes = withContext(Dispatchers.IO) { getResp.body?.bytes() }
        if (originalBytes == null) return null
        println("[AuthRepository] Downloaded ${originalBytes.size} bytes")

        // 2) Compress via lambda
        println("[AuthRepository] Step 2: Compressing image via lambda")
        val compressionUrl = "https://${ApiEndpoints.Image.COMPRESSION}"
        val compressionRequest = Request.Builder()
            .url(compressionUrl)
            .post(originalBytes.toRequestBody(originalMime.toMediaTypeOrNull()))
            .addHeader("Content-Type", originalMime)
            .build()

        val compressionResp = withContext(Dispatchers.IO) { client.newCall(compressionRequest).execute() }
        if (!compressionResp.isSuccessful) {
            println("[AuthRepository] Compression failed: ${compressionResp.code}")
            return null
        }
        val compressedBytes = withContext(Dispatchers.IO) { compressionResp.body?.bytes() }
        val compressedMime = compressionResp.header("Content-Type") ?: originalMime
        if (compressedBytes == null) return null
        println("[AuthRepository] Compressed to ${compressedBytes.size} bytes")

        // 3) Request upload URL from backend
        println("[AuthRepository] Step 3: Requesting upload URL from backend")
        val filename = "login_${System.currentTimeMillis()}.jpg"
        val getUrlResponse = ApiClient.request(
            url = ApiEndpoints.Image.GETUPLOADURL,
            method = "POST",
            body = mapOf("filename" to filename)
        ) ?: return null

        val json = JSONObject(getUrlResponse)
        val uploadUrl = json.optString("imageUrl", json.optString("uploadUrl", json.optString("url", "")))
        if (uploadUrl.isBlank()) {
            println("[AuthRepository] No upload URL in response")
            return null
        }
        println("[AuthRepository] Got upload URL: $uploadUrl")

        // 4) PUT compressed bytes to obtained URL
        println("[AuthRepository] Step 4: Uploading to R2")
        val uploadRequest = Request.Builder()
            .url(uploadUrl)
            .put(compressedBytes.toRequestBody(compressedMime.toMediaTypeOrNull()))
            .addHeader("Content-Type", compressedMime)
            .build()
        val uploadResp = withContext(Dispatchers.IO) { client.newCall(uploadRequest).execute() }
        if (!uploadResp.isSuccessful) {
            println("[AuthRepository] Upload failed: ${uploadResp.code}")
            return null
        }
        println("[AuthRepository] Upload successful: ${uploadResp.code}")

        // 5) Construct public URL from path
        val urlObj = java.net.URL(uploadUrl)
        val pathname = urlObj.path
        val publicImageUrl = "https://r2.chap-app.jp$pathname"
        println("[AuthRepository] Public image URL: $publicImageUrl")
        publicImageUrl
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

class AuthRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository
) : AuthRepository {
    // ViewModel で API 呼び出し処理をまとめる
    override suspend fun login(email: String, password: String, context: Context): Result<String> {
        return try {
            val response = ApiClient.request(
                url = ApiEndpoints.Auth.LOGIN,
                method = "POST",
                body = mapOf("email" to email, "password" to password)
            )
            val json = JSONObject(response ?: "")
            val token = json.optString("token", "")
            
            if (token.isNotBlank()) {
                TokenManager(context).saveToken(token)
                ApiClient.token = token // 即時反映

                // After successful login, optionally fetch an initial image from a URL,
                // compress/upload it and save the resulting public URL locally.
                try {
                    println("[AuthRepository] Starting login image upload flow")

                    // Get userId from UserRepository
                    val currentUser = userRepository.getCurrentUser()
                    val userId = currentUser?.id
                    val userName = currentUser?.name?: "blank"
                    println("[AuthRepository] Got userId from getCurrentUser: $userId")

                    val remoteImageUrl = "https://picsum.photos/200/300"
                    val publicImageUrl = fetchCompressAndUploadImage(remoteImageUrl)
                    if (publicImageUrl != null && userId != null) {
                        println("[AuthRepository] Step 5: Updating user profile with image URL")
                        // Save to backend user profile via UserRepository
                        userRepository.updateUserImage(userId, publicImageUrl,userName)
                            .onSuccess { response ->
                                println("[AuthRepository] Updated user image on server: $response")
                            }
                            .onFailure { e ->
                                println("[AuthRepository] Failed to update user image: ${e.message}")
                            }
                    } else {
                        println("[AuthRepository] Image upload failed or userId is blank. publicImageUrl=$publicImageUrl, userId=$userId")
                    }
                } catch (ie: Exception) {
                    // Non-fatal: image upload failure should not block login
                    println("[AuthRepository] Exception during image upload: ${ie.message}")
                    ie.printStackTrace()
                }
            }
            Result.success(response ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        context: Context
    ): Result<String> {
        return try {
            val response = ApiClient.request(
                url = ApiEndpoints.Auth.REGISTER,
                method = "POST",
                body = mapOf("email" to email, "password" to password, "name" to displayName)
            )
            val json = JSONObject(response ?: "")
            val token = json.optString("token", "")
            if (token.isNotBlank()) {
                TokenManager(context).saveToken(token)
                ApiClient.token = token // 即時反映

                // After successful login, optionally fetch an initial image from a URL,
                // compress/upload it and save the resulting public URL locally.
                try {
                    println("[AuthRepository] Starting login image upload flow")

                    // Get userId from UserRepository
                    val currentUser = userRepository.getCurrentUser()
                    val userId = currentUser?.id
                    val userName = currentUser?.name?: "blank"
                    println("[AuthRepository] Got userId from getCurrentUser: $userId")

                    val remoteImageUrl = "https://picsum.photos/200/300"
                    val publicImageUrl = fetchCompressAndUploadImage(remoteImageUrl)
                    if (publicImageUrl != null && userId != null) {
                        println("[AuthRepository] Step 5: Updating user profile with image URL")
                        // Save to backend user profile via UserRepository
                        userRepository.updateUserImage(userId, publicImageUrl, userName)
                            .onSuccess { response ->
                                println("[AuthRepository] Updated user image on server: $response")
                            }
                            .onFailure { e ->
                                println("[AuthRepository] Failed to update user image: ${e.message}")
                            }
                    } else {
                        println("[AuthRepository] Image upload failed or userId is blank. publicImageUrl=$publicImageUrl, userId=$userId")
                    }
                } catch (ie: Exception) {
                    // Non-fatal: image upload failure should not block login
                    println("[AuthRepository] Exception during image upload: ${ie.message}")
                    ie.printStackTrace()
                }
            }
            Result.success(response ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logOut(context: Context) {
        // TokenManagerを使ってJWTトークンを削除
        TokenManager(context).clearToken()
        // ApiClientのメモリ上のトークンもクリア
        ApiClient.token = null
    }

    
}