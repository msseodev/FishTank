package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.arduino.ArduinoListener
import com.marine.fishtank.server.model.*
import com.marine.fishtank.server.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.Socket

private const val MAGIC_VALUE = 235621
private const val TAG = "Client"

class Client(private val socket: Socket): ArduinoListener {
    private var dataOutputStream: DataOutputStream = DataOutputStream(socket.getOutputStream())
    private var dataInputStream: DataInputStream = DataInputStream(socket.getInputStream())
    private val clientScope = CoroutineScope(Dispatchers.IO)
    private var isRun = false
    private var id = 0

    override fun onMessage(packet: FishPacket) {
        // Message from Arduino device!
        when(packet.opCode) {
            OP_GET_TEMPERATURE -> {
                // Send back to client.
                dataOutputStream.writeUTF(
                    ServerPacket(clientId = id, opCode = SERVER_OP_GET_TEMPERATURE, doubleData = packet.data.toDouble()).toJson()
                )
            }
        }
    }

    fun startListen() {
        isRun = true

        // Listen Arduino message
        ArduinoDevice.registerListener(id, this)

        clientScope.launch {
            while(isRun) {
                val message = dataInputStream.readUTF()
                // message should be json.
                handleMessage(message)
            }
        }
    }

    private suspend fun handleMessage(json: String) {
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
                SERVER_OP_GET_TEMPERATURE -> {
                    ArduinoDevice.getTemperature(packet.clientId)
                }
                SERVER_OP_IN_WATER -> {
                    ArduinoDevice.enableInWaterValve(packet.clientId, packet.data != 0)
                }
                SERVER_OP_OUT_WATER -> {
                    ArduinoDevice.enableOutWaterValve(packet.clientId, packet.data != 0)
                }
                SERVER_OP_WATER_PUMP -> {
                    ArduinoDevice.enableWaterPump(packet.clientId, packet.data != 0)
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
                SERVER_OP_GET_HISTORY -> {

                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    fun disconnect() {
        ArduinoDevice.unRegisterListener(id)
        try {
            isRun = false
            dataOutputStream.close()
            dataInputStream.close()
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handShake(): Boolean {
        val first = dataInputStream.readInt()
        val verified = first == MAGIC_VALUE
        if(!verified) return false

        this.id = dataInputStream.readInt()
        return true
    }
}