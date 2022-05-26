package com.marine.fishtank.server.model

import java.util.*

data class Temperature (
    val id: Int = 0,
    val temperature: Float,
    val time: Long
)