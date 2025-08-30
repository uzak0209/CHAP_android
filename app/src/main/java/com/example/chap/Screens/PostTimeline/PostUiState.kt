package com.example.chap.Screens.PostTimeline

import com.example.chap.Screens.PostTimeline.bindingmodel.PostBindingModel
import com.example.chap.domain.model.PostId
import com.example.chap.Models.User
import com.example.chap.Models.Coordinate

data class PostUiState(
    val bindingModel: PostBindingModel,
    val isLoading: Boolean,
){
    companion object {
        fun empty(): PostUiState = PostUiState(
            bindingModel = PostBindingModel(
                id = PostId("0"),
                type = "",
                created_at = "",
                updated_at = "",
                deleted_at = null,
                user_id = "",
                username = "",
                user = User(
                    id = "", name = "", image = null, email = "", created_at = "", valid = true,
                    password = "", login_type = null, updated_at = "", deleted_at = null
                ),
                coordinate = Coordinate(0.0, 0.0),
                content = "",
                category = "",
                valid = true,
                like = 0,
                tags = emptyList()
            ),
            isLoading = false
        )
    }

    val canPost: Boolean
        get() = bindingModel.content.isNotBlank()
}
