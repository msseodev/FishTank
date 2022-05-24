package com.marine.fishtank.server.model

import java.sql.Date


data class Temperature (
    val id: Int,
    val temperature: Float,
    val time: Date
)