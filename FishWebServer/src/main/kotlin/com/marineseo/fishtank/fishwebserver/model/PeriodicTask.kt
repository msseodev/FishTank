package com.marineseo.fishtank.fishwebserver.model

import java.sql.Time

data class PeriodicTask(
    var id: Int = 0,
    var userId: String = "",
    var type: Int = 0,
    var data: Int = 0,
    var time: Time = Time(System.currentTimeMillis())
) {
    companion object {
        const val TABLE_NAME = "periodicTask"
        const val COL_ID = "id"
        const val COL_USER_ID = "userId"
        const val COL_TYPE = "type"
        const val COL_DATA = "data"
        const val COL_TIME = "time"
    }
}
