package com.example.chap.location

import com.example.chap.models.Coordinate

interface LocationProvider {
    suspend fun current(): Coordinate?
}


