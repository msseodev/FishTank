package com.marine.fishtank.api

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

private const val MAGIC_VALUE = 235621

class Client {
    private var socket: Socket? = null
    private var dataInputStream: DataInputStream? = null
    private var dataOutputStream: DataOutputStream? = null

    fun connect(url: String, port: Int) {
        socket = Socket(url, port)

        dataInputStream = DataInputStream(socket?.getInputStream())
        dataOutputStream = DataOutputStream(socket?.getOutputStream())

        dataOutputStream?.writeInt(MAGIC_VALUE)
    }

    @Throws(IOException::class)
    fun send(message: String) {
        if(socket?.isConnected == true) {
            dataOutputStream?.writeUTF(message)
        }
    }

    fun disConnect() {
        socket?.close()
        socket = null
    }
}