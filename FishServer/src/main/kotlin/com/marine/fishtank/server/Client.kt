package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.database.DataBase
import com.marine.fishtank.server.model.*
import com.marine.fishtank.server.util.Log
import com.marine.fishtank.server.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.sql.Date
import java.text.SimpleDateFormat

private const val MAGIC_VALUE = 235621
private const val TAG = "Client"

class Client(private val socket: Socket,
             private val disconnectCallback: OnClientDisconnect) {
    private val taskManager = TaskManager()

    private var dataOutputStream: DataOutputStream = DataOutputStream(socket.getOutputStream())
    private var dataInputStream: DataInputStream = DataInputStream(socket.getInputStream())
    private val clientScope = CoroutineScope(Dispatchers.IO)
    private var isRun = false
    private var id = 0

    fun startListen() {
        isRun = true

        clientScope.launch {
            while (isRun) {
                val message = dataInputStream.readUTF()
                // message should be json.
                handleMessage(message)
            }
        }
    }

    private fun handleMessage(json: String) {
        try {
            Log.d(TAG, "Message from client=$json")
            val packet = ServerPacket.createFromJson(json)

            when (packet.opCode) {
                SERVER_OP_MEGA_LED -> {
                    ArduinoDevice.enableBoardLed(
                        packet.clientId,
                        packet.data != 0
                    )
                }
                SERVER_OP_READ_TEMPERATURE -> {
                    val temperature = ArduinoDevice.getTemperature(packet.clientId)
                    // Send back to client.
                    dataOutputStream.writeUTF(
                        ServerPacket(
                            clientId = id,
                            opCode = SERVER_OP_READ_TEMPERATURE,
                            temperatureList = arrayListOf(
                                Temperature(
                                    temperature = temperature,
                                    time = System.currentTimeMillis()
                                )
                            )
                        ).toJson()
                    )
                }
                SERVER_OP_DB_TEMPERATURE -> {
                    val daysInMils = TimeUtils.MILS_DAY * packet.data
                    val from = Date(System.currentTimeMillis() - daysInMils)
                    val until = Date(System.currentTimeMillis())
                    val temperatures = DataBase.fetchTemperature(from, until)

                    // for logging
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    Log.d(
                        TAG,
                        "Fetching from ${formatter.format(from)} until ${formatter.format(until)} tempSize=${temperatures.size}"
                    )

                    dataOutputStream.writeUTF(
                        ServerPacket(
                            clientId = id,
                            opCode = SERVER_OP_DB_TEMPERATURE,
                            temperatureList = temperatures
                        ).toJson()
                    )
                }
                SERVER_OP_IN_WATER -> {
                    ArduinoDevice.enableInWaterValve(packet.clientId, packet.data != 0)
                }
                SERVER_OP_OUT_WATER -> {
                    ArduinoDevice.enableOutWaterValve(packet.clientId, packet.data != 0)
                }
                SERVER_OP_HEATER -> {
                    ArduinoDevice.enableHeater(packet.clientId, packet.data != 0)
                }
                SERVER_OP_LIGHT -> {
                    ArduinoDevice.enableLight(packet.clientId, packet.data != 0)
                }
                SERVER_OP_PURIFIER_1 -> {
                    ArduinoDevice.enablePurifier1(packet.clientId, packet.data != 0)
                }
                SERVER_OP_PURIFIER_2 -> {
                    ArduinoDevice.enablePurifier2(packet.clientId, packet.data != 0)
                }
                SERVER_OP_READ_IN_WATER -> {
                    val state = ArduinoDevice.isInWaterValveOpen(packet.clientId)
                    dataOutputStream.writeUTF(
                        ServerPacket(
                            clientId = id,
                            opCode = packet.opCode,
                            pinState = state
                        ).toJson()
                    )
                }
                SERVER_OP_READ_OUT_WATER -> {
                    val state = ArduinoDevice.isOutWaterValveOpen(packet.clientId)
                    dataOutputStream.writeUTF(
                        ServerPacket(
                            clientId = id,
                            opCode = packet.opCode,
                            pinState = state
                        ).toJson()
                    )
                }
                SERVER_OP_WATER_REPLACE -> {
                    taskManager.createReplaceWaterTask(packet.data * 0.01f)
                }
                SERVER_OP_SIGN_IN -> {
                    val user = packet.obj as User
                    val logInResult = DataBase.logIn(user.id, user.password)
                    Log.d(TAG, "LogInResult=$logInResult id=${user.id} password=${user.password}")
                    dataOutputStream.writeUTF(
                        ServerPacket(
                            clientId = id,
                            opCode = packet.opCode,
                            obj = logInResult
                        ).toJson()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            isRun = false
            dataOutputStream.close()
            dataInputStream.close()
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        disconnectCallback.onClientDisconnected(this)
    }

    fun handShake(): Boolean {
        val first = dataInputStream.readInt()
        val verified = first == MAGIC_VALUE
        if (!verified) return false

        this.id = dataInputStream.readInt()
        return true
    }
}