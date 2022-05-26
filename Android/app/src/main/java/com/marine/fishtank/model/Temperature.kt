package com.marine.fishtank.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Temperature (
    val id: Int = 0,
    val temperature: Float,
    val time: Long
): Parcelable