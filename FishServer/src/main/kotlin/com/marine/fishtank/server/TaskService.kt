package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.database.DataBase
import com.marine.fishtank.server.model.Task
import com.marine.fishtank.server.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TASK_INTERVAL = 1000L * 2
private const val SERVICE_ID = Integer.MAX_VALUE - 1

private const val TAG = "TaskService"
class TaskService {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val taskManager = TaskManager()
    private var run = false

    fun start() {
        run = true
        scope.launch {
            while(run) {
                val task = taskManager.fetchTask()
                task?.let {
                    Log.i(TAG, "Executing $task")

                    when(it.type) {
                        Task.TYPE_REPLACE_WATER -> {

                        }
                        Task.TYPE_VALVE_IN_WATER -> {
                            ArduinoDevice.enableInWaterValve(
                                clientId =  SERVICE_ID,
                                open = it.data == Task.DATA_OPEN
                            )
                        }
                        Task.TYPE_VALVE_OUT_WATER -> {
                            ArduinoDevice.enableOutWaterValve(
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
                    DataBase.updateTask(task)
                }

                delay(TASK_INTERVAL)
            }
        }
    }

    fun stop() {
        run = false
    }
}