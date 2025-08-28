package com.example.chap.usecase.impl.post

import android.accounts.AuthenticatorException
import com.example.chap.Models.Coordinate
import com.example.chap.Models.User
import com.example.chap.domain.model.PostId
import com.example.chap.domain.repository.PostRepository
import com.example.chap.usecase.post.PostSubmitUseCase
import com.example.chap.usecase.post.PostSubmitUseCaseResult

class PostSubmitUseCaseImpl(
    private val PostRepository: PostRepository
) : PostSubmitUseCase {
    override suspend fun execute(
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
    ): PostSubmitUseCaseResult {

        return try {
            PostRepository.create(
                id = id,
                type =  type,
                created_at =  created_at,
                updated_at =  updated_at,
                deleted_at =  deleted_at,
                user_id =  user_id,
                username = username,
                user = user,
                coordinate = coordinate,
                content = content,
                category = category,
                valid = valid,
                like = like,
                tags = tags,

            )

            PostSubmitUseCaseResult.Success
        } catch (e: AuthenticatorException) {
            PostSubmitUseCaseResult.Failure.NotLoggedIn
        } catch (e: Exception) {
            PostSubmitUseCaseResult.Failure.OtherError(e)
        }
    }
}