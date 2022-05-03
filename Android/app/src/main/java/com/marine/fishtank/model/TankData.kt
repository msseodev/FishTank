package com.marine.fishtank.model

data class TankData(
    val temperature: Double,
    val waterChange: Boolean,
    val isLightOn: Boolean,
    val isPurifierOn: Boolean,
    val dateTime: Long
)
