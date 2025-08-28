package com.example.chap.usecase.post

sealed interface PostSubmitUseCaseResult {
    object Success : PostSubmitUseCaseResult
    sealed interface Failure : PostSubmitUseCaseResult {
        object EmptyContent : Failure
        object NotLoggedIn : Failure
        data class OtherError(val throwable: Throwable) : Failure
    }
}