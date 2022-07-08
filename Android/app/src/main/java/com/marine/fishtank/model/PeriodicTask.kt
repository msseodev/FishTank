package com.marine.fishtank.model

import android.content.Context
import com.marine.fishtank.R
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_LIGHT
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_PURIFIER
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_REPLACE_WATER
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_VALVE_IN_WATER
import com.marine.fishtank.model.PeriodicTask.Companion.TYPE_VALVE_OUT_WATER

data class PeriodicTask(
    var id: Int = 0,
    var userId: String = "",
    var type: Int = 0,
    var data: Int = 0,
    var time: String = ""
) {
    companion object {
        const val TABLE_NAME = "periodicTask"
        const val COL_ID = "id"
        const val COL_USER_ID = "userId"
        const val COL_TYPE = "type"
        const val COL_DATA = "data"
        const val COL_TIME = "time"

        const val TYPE_REPLACE_WATER = 0
        const val TYPE_VALVE_OUT_WATER = 1
        const val TYPE_VALVE_IN_WATER = 2
        const val TYPE_PURIFIER = 3
        const val TYPE_LIGHT = 4
    }
}

fun PeriodicTask.typeAsString(context: Context): String {
    return when(type) {
        TYPE_REPLACE_WATER -> context.getString(R.string.replace_water)
        TYPE_VALVE_OUT_WATER -> context.getString(R.string.out_valve)
        TYPE_VALVE_IN_WATER -> context.getString(R.string.in_valve)
        TYPE_PURIFIER -> context.getString(R.string.purifier)
        TYPE_LIGHT -> context.getString(R.string.light)
        else -> "UNKNOWN"
    }
}