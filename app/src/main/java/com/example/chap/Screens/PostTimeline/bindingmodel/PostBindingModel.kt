package com.example.chap.Screens.PostTimeline.bindingmodel

import com.example.chap.Models.Coordinate
import com.example.chap.domain.model.PostId

// UI表示専用 (必要最低限 + 今後拡張用のフィールド保持)
data class PostBindingModel(
    val id: PostId,
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
    val tags: List<String>,
)
