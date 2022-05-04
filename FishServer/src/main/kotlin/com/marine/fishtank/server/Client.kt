package com.marine.fishtank.server

import com.google.gson.Gson
import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.Socket

private const val MAGIC_VALUE = 235621

class Client(private val socket: Socket) {
    private var dataOutputStream: DataOutputStream = DataOutputStream(socket.getOutputStream())
    private var dataInputStream: DataInputStream = DataInputStream(socket.getInputStream())
    private val clientScope = CoroutineScope(Dispatchers.IO)
    private var isRun = false

    fun startListen() {
        isRun = true
        clientScope.launch {
            while(isRun) {
                val message = dataInputStream.readUTF()
                // message should be json.
                handleMessage(message)
            }
        }
    }

    private fun handleMessage(json: String) {
        println("Message from client=$json")
        val packet = Gson().fromJson(json, FishPacket::class.java)

        when(packet.opCode) {
            OP_MEGA_LED -> {
                ArduinoDevice.enableBoardLed(
                    packet.data != 0
                )
            }

            OP_GET_HISTORY -> {

            }
            OP_GET_STATUS_ALL -> {

            }
            OP_LISTEN_STATUS -> {

            }
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
    }

    fun verifyMagicWord(): Boolean {
        val first = dataInputStream.readInt()
        return first == MAGIC_VALUE
    }
}