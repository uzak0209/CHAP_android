package com.back.chap.screens.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import com.back.chap.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    suspend fun logOut(context: Context) {
        authRepository.logOut(context)
    }
}