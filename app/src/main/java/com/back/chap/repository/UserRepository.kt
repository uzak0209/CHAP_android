package com.back.chap.repository

import com.back.chap.models.User

interface UserRepository {
    suspend fun updateUserToDatabase(userId: Long, updates: Map<String, Any?>)
    suspend fun getUserById(userId: String): User?
    suspend fun getCurrentUser(): User?
    suspend fun updateUserImage(userId: String, imageUrl: String, name: String): Result<String>
}
