package com.back.chap.repository

import android.content.Context

interface AuthRepository {
    suspend fun login(email: String, password: String, context: Context): Result<String>
    suspend fun register(email: String, password: String, displayName: String, context: Context): Result<String>

    suspend fun logOut(context: Context)
}




