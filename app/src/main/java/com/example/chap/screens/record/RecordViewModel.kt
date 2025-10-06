package com.example.chap.screens.record

import androidx.lifecycle.ViewModel
import com.example.chap.location.LocationProvider
import com.example.chap.repository.EventRepositoryImpl
import com.example.chap.repository.PostRepositoryImpl
import com.example.chap.repository.ThreadRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val postRepository: PostRepositoryImpl,
    private val threadRepository: ThreadRepositoryImpl,
    private val eventRepository: EventRepositoryImpl,
    private val locationProvider: LocationProvider,
) : ViewModel() {

}