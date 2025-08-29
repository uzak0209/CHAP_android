package com.example.chap.Screens.PostTimeline.bindingmodel.conveter

import com.example.chap.Screens.PostTimeline.bindingmodel.PostBindingModel
import com.example.chap.Models.Post

object PostConverter {
    fun convertToBindingModel(postList: List<Post>): List<PostBindingModel> =
        postList.map { convertToBindingModel(it) }

    fun convertToBindingModel(post: Post): PostBindingModel = PostBindingModel(
        id = com.example.chap.domain.model.PostId(post.id.toString()),
        type = post.type,
        created_at = post.created_at,
        updated_at = post.updated_at,
        deleted_at = post.deleted_at,
        user_id = post.user_id,
        username = post.username,
        user = post.user,
        coordinate = post.coordinate,
        content = post.content,
        category = post.category,
        valid = post.valid,
        like = post.like,
        tags = post.tags
    )
}

