package com.marine.fishtank.server.model


data class Task(
    val id: Int = 0,
    val userId: String = "System",
    val type: Int,
    val data: Int = 0,
    val executeTime: Long = System.currentTimeMillis(),
    val state: Int = STATE_STANDBY
) {
    companion object {
        const val TYPE_VALVE_OUT_WATER = 1
        const val TYPE_VALVE_IN_WATER = 2
        const val TYPE_PURIFIER = 3
        const val TYPE_LIGHT = 4

        const val STATE_STANDBY = 0
        const val STATE_EXECUTING = 1
        const val STATE_FINISH = 2


        const val TB_TASK = "task"
        const val COL_ID = "id"
        const val COL_USER_ID = "userId"
        const val COL_TYPE = "type"
        const val COL_DATA = "data"
        const val COL_EXECUTE_TIME = "executeTime"
        const val COL_STATE = "state"
    }

}
