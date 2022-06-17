package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.mapper.DatabaseMapper
import com.marineseo.fishtank.fishwebserver.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private const val TASK_INTERVAL = 1000L * 2
private const val SERVICE_ID = Integer.MAX_VALUE - 1

private const val TAG = "TaskExecuteService"

@Service
class TaskExecuteService(
    private val taskService: TaskService,
    private val arduinoService: ArduinoService,
    private val mapper: DatabaseMapper
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private var run = false

    fun start() {
        run = true
        scope.launch {
            while(run) {
                val task = taskService.fetchTask()
                task?.let {
                    logger.info("Executing $task")

                    when(it.type) {
                        Task.TYPE_REPLACE_WATER -> {

                        }
                        Task.TYPE_VALVE_IN_WATER -> {
                            arduinoService.enableInWaterValve(
                                clientId =  SERVICE_ID,
                                open = it.data == Task.DATA_OPEN
                            )
                        }
                        Task.TYPE_VALVE_OUT_WATER -> {
                            arduinoService.enableOutWaterValve(
                                clientId = SERVICE_ID,
                                open = it.data == Task.DATA_OPEN
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

    fun stop() {
        run = false
    }
}