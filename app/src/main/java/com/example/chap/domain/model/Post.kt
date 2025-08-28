package com.example.chap.domain.model

import com.example.chap.Models.Coordinate
import com.example.chap.Models.User
import com.example.chap.common.ddd.Entity

class Post(
    id: PostId,
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
) : Entity<PostId>(id)