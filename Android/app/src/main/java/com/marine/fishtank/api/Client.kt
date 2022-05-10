package com.marine.fishtank.api

import com.marine.fishtank.model.AppId
import com.marine.fishtank.model.ServerPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.Socket

private const val MAGIC_VALUE = 235621

class Client {
    private var socket: Socket? = null
    private var dataInputStream: DataInputStream? = null
    private var dataOutputStream: DataOutputStream? = null

    private var listener: MessageListener? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var listen = false

    fun connect(url: String, port: Int): Boolean {
        try {
            socket = Socket(url, port)

            dataInputStream = DataInputStream(socket?.getInputStream())
            dataOutputStream = DataOutputStream(socket?.getOutputStream())

            dataOutputStream?.writeInt(MAGIC_VALUE)
            dataOutputStream?.writeInt(AppId.MY_ID)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @Throws(IOException::class)
    fun send(message: String) {
        if(socket?.isConnected == true) {
            dataOutputStream?.writeUTF(message)
        }
    }

    fun startListen() {
        listen = true
        coroutineScope.launch {
            try {
                while (listen) {
                    val message = dataInputStream?.readUTF()
                    if (message != null) {
                        listener?.onServerMessage(
                            ServerPacket.createFromJson(message)
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listen = false
            }
        }
    }

    fun stopListen() {
        listen = false
    }


    fun registerListener(listener: MessageListener) {
        this.listener = listener
    }

    fun unRegisterListener() {
        this.listener = null
    }

    fun disConnect() {
        socket?.close()
        socket = null
    }
}