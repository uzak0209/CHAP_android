package com.example.chap.repository

import com.example.chap.models.User

interface UserRepository {
    suspend fun updateUserToDatabase(userId: Long, updates: Map<String, Any?>)
    suspend fun getUserById(userId: Long): User?
    suspend fun getCurrentUser(): User?
}
