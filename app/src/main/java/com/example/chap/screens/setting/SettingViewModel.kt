package com.example.chap.screens.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.chap.models.LocationState
import com.example.chap.models.Status
import com.example.chap.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    suspend fun logOut(context: Context) {
        authRepository.logOut(context)
    }
}