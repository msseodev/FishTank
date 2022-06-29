package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.mapper.DatabaseMapper
import com.marineseo.fishtank.fishwebserver.model.Task
import com.marineseo.fishtank.fishwebserver.model.Temperature
import com.marineseo.fishtank.fishwebserver.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.*
import org.springframework.stereotype.Service
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat

private const val WATER_VOLUME = 100000 // ml
private const val WATER_OUT_IN_MINUTE = 578 // ml
private const val WATER_REPLACE_RATIO_MIN = 0.5

private const val TAG = "TaskService"
private const val TASK_INTERVAL = 1000L * 3

@Service
class TaskService(
    private val arduinoService: ArduinoService,
    private val mapper: DatabaseMapper
) : ApplicationListener<ApplicationContextEvent> {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var run = false

    override fun onApplicationEvent(event: ApplicationContextEvent) {
        logger.warn("onApplicationEvent - $event")

        when (event) {
            is ContextStartedEvent -> {
                start()
            }
            is ContextRefreshedEvent -> {
                // Refresh
                stop()
                start()
            }
            is ContextClosedEvent, is ContextStoppedEvent -> {
                stop()
            }
        }
    }

    private fun start() {
        run = true
        scope.launch {
            while (run) {
                fetchTask()?.let { task ->
                    logger.info("Executing $task")

                    when (task.type) {
                        Task.TYPE_REPLACE_WATER -> {

                        }
                        Task.TYPE_VALVE_IN_WATER -> {
                            arduinoService.enableInWaterValve(
                                open = task.data == Task.DATA_OPEN
                            )
                        }
                        Task.TYPE_VALVE_OUT_WATER -> {
                            arduinoService.enableOutWaterValve(
                                open = task.data == Task.DATA_OPEN
                            )
                        }
                        Task.TYPE_LIGHT -> {
                            // TODO
                        }
                        Task.TYPE_PURIFIER -> {
                            // TODO
                        }
                    }

                    task.state = Task.STATE_FINISH
                    mapper.updateTask(task)
                }

                delay(TASK_INTERVAL)
            }
        }
    }

    private fun stop() {
        run = false
    }

    fun createReplaceWaterTask(ratio: Float) {
        if (ratio < 0 || ratio > WATER_REPLACE_RATIO_MIN) {
            // Wrong param.
            logger.error("Wrong ratio parameter! ratio=$ratio")
            return
        }

        if (recentReplaceWaterTaskExist()) {
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
        mapper.insertTask(
            Task(
                type = Task.TYPE_REPLACE_WATER,
                state = Task.STATE_STANDBY
            )
        )

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
        replaceTask?.let { task ->
            if (task.executeTime < System.currentTimeMillis() + TimeUtils.MILS_HOUR) {
                return true
            }
        }
        return false
    }

    private fun fetchTask(): Task? {
        return mapper.fetchTask(Timestamp(System.currentTimeMillis()))
    }

}