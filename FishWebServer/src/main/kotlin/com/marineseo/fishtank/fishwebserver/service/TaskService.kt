package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.mapper.DatabaseMapper
import com.marineseo.fishtank.fishwebserver.model.Task
import com.marineseo.fishtank.fishwebserver.util.TimeUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.sql.Timestamp

private const val WATER_VOLUME = 100000 // ml
private const val WATER_OUT_IN_MINUTE = 578 // ml
private const val WATER_REPLACE_RATIO_MIN = 0.5

private const val TAG = "TaskService"

@Service
class TaskService(
    private val mapper: DatabaseMapper
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    
    fun createReplaceWaterTask(ratio: Float) {
        if (ratio < 0 || ratio > WATER_REPLACE_RATIO_MIN) {
            // Wrong param.
            logger.error("Wrong ratio parameter! ratio=$ratio")
            return
        }

        if(recentReplaceWaterTaskExist()) {
            logger.error("Too frequent replacement is not allowed.")
            return
        }

        // Calculate the amount of water that needs to be replaced.
        val amountOfWater = (WATER_VOLUME * ratio)
        val outTime = amountOfWater / WATER_OUT_IN_MINUTE
        val outTimeInSec = (outTime * 60).toInt()
        logger.info("Replace water=$amountOfWater, outTime=$outTimeInSec sec")

        // Create tasks.
        // For tracing(and logging), we put TYPE_REPLACE_WATER task.
        mapper.insertTask(Task(
            type = Task.TYPE_REPLACE_WATER,
            state = Task.STATE_STANDBY
        ))

        mapper.insertTask(
            Task(
                type = Task.TYPE_VALVE_IN_WATER,
                data = Task.DATA_CLOSE,
                state = Task.STATE_STANDBY
            )
        )

        mapper.insertTask(
            Task(
                type = Task.TYPE_VALVE_OUT_WATER,
                data = Task.DATA_OPEN,
                state = Task.STATE_STANDBY
            )
        )

        val finishTime = System.currentTimeMillis() + (outTimeInSec * 1000L)
        mapper.insertTask(
            Task(
                type = Task.TYPE_VALVE_OUT_WATER,
                data = Task.DATA_CLOSE,
                executeTime = finishTime,
                state = Task.STATE_STANDBY
            )
        )

        mapper.insertTask(
            Task(
                type = Task.TYPE_VALVE_IN_WATER,
                data = Task.DATA_OPEN,
                executeTime = finishTime + 1000L,
                state = Task.STATE_STANDBY
            )
        )
    }

    fun recentReplaceWaterTaskExist(): Boolean {
        val replaceTask = mapper.getLastReplaceTask()
        replaceTask?.let {task ->
            if(task.executeTime < System.currentTimeMillis() + TimeUtils.MILS_HOUR) {
                return true
            }
        }
        return false
    }

    fun fetchTask(): Task? {
        return mapper.fetchTask(Timestamp(System.currentTimeMillis()))
    }
}