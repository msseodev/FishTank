package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.*
import com.marine.fishtank.server.database.DataBase
import com.marine.fishtank.server.model.Task
import com.marine.fishtank.server.util.Log
import com.marine.fishtank.server.util.TimeUtils

private const val WATER_VOLUME = 100000 // ml
private const val WATER_OUT_IN_MINUTE = 578 // ml
private const val WATER_REPLACE_RATIO_MIN = 0.5

private const val TAG = "TaskManager"

class TaskManager {
    fun createReplaceWaterTask(ratio: Float) {
        if (ratio < 0 || ratio > WATER_REPLACE_RATIO_MIN) {
            // Wrong param.
            Log.e(TAG, "Wrong ratio parameter! ratio=$ratio")
            return
        }

        if(recentReplaceWaterTaskExist()) {
            Log.e(TAG, "Too frequent replacement is not allowed.")
            return
        }

        // Calculate the amount of water that needs to be replaced.
        val amountOfWater = (WATER_VOLUME * ratio)
        val outTime = amountOfWater / WATER_OUT_IN_MINUTE
        val outTimeInSec = (outTime * 60).toInt()
        Log.i(TAG, "Replace water=$amountOfWater, outTime=$outTimeInSec sec")

        // Create tasks.
        // For tracing(and logging), we put TYPE_REPLACE_WATER task.
        DataBase.insertTask(Task(
            type = Task.TYPE_REPLACE_WATER,
            state = Task.STATE_STANDBY
        ))

        DataBase.insertTask(
            Task(
                type = Task.TYPE_VALVE_IN_WATER,
                data = Task.DATA_CLOSE,
                state = Task.STATE_STANDBY
            )
        )

        DataBase.insertTask(
            Task(
                type = Task.TYPE_VALVE_OUT_WATER,
                data = Task.DATA_OPEN,
                state = Task.STATE_STANDBY
            )
        )

        val finishTime = System.currentTimeMillis() + (outTimeInSec * 1000L)
        DataBase.insertTask(
            Task(
                type = Task.TYPE_VALVE_OUT_WATER,
                data = Task.DATA_CLOSE,
                executeTime = finishTime,
                state = Task.STATE_STANDBY
            )
        )

        DataBase.insertTask(
            Task(
                type = Task.TYPE_VALVE_IN_WATER,
                data = Task.DATA_OPEN,
                executeTime = finishTime + 1000L,
                state = Task.STATE_STANDBY
            )
        )
    }

    fun recentReplaceWaterTaskExist(): Boolean {
        val replaceTask = DataBase.getLastReplaceTask()
        replaceTask?.let {task ->
            if(task.executeTime < System.currentTimeMillis() + TimeUtils.MILS_HOUR) {
                return true
            }
        }
        return false
    }

    fun fetchTask(): Task? {
        return DataBase.fetchTask()
    }
}