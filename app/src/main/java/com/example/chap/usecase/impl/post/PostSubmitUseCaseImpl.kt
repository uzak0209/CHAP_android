package com.example.chap.usecase.impl.post

import com.example.chap.domain.repository.PostRepository

class PostYweetUseCaseImpl(
    private val PostRepository: PostRepository
) : PostYweetUseCase {
    override suspend fun execute(
        content: String,
        attachmentList: List<File>
    ): PostYweetUseCaseResult {
        if (content == "" && attachmentList.isEmpty()) {
            return PostYweetUseCaseResult.Failure.EmptyContent
        }

        return try {
            PostRepository.create(
                content = content,
                attachmentList = emptyList()
            )

            PostYweetUseCaseResult.Success
        } catch (e: AuthenticatorException) {
            PostYweetUseCaseResult.Failure.NotLoggedIn
        } catch (e: Exception) {
            PostYweetUseCaseResult.Failure.OtherError(e)
        }
    }
}