package com.back.chap.location

import com.back.chap.models.Coordinate

interface LocationProvider {
    suspend fun current(): Coordinate?
}


