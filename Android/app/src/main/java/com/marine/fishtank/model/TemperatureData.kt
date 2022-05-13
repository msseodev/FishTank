package com.marine.fishtank.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class TemperatureData(
    var temperature: Double = 0.0,
    var dateTime: Long = 0
): Parcelable
