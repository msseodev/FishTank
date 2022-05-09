package com.marine.fishtank.server

import com.google.gson.Gson
import com.marine.fishtank.server.arduino.ArduinoDevice
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
                    ServerPacket(clientId = id, opCode = SERVER_OP_GET_TEMPERATURE, doubleData = packet.data).toJson()
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
        Log.d(TAG, "Message from client=$json")
        val packet = ServerPacket.createFromJson(json)

        when(packet.opCode) {
            SERVER_OP_MEGA_LED -> {
                ArduinoDevice.enableBoardLed(
                    packet.clientId,
                    packet.data != 0
                )
            }
            SERVER_OP_GET_TEMPERATURE -> {
                ArduinoDevice.getTemperature(packet.clientId)
            }
            SERVER_OP_GET_HISTORY -> {

            }
            SERVER_OP_LISTEN_STATUS -> {

            }
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