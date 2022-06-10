package com.marine.fishtank.api

import android.util.Log
import com.marine.fishtank.model.ServerPacket
import com.marine.fishtank.model.toJson

private const val TAG = "TankApi"

interface OnServerPacketListener {
    fun onServerPacket(packet: ServerPacket)
}

object TankApi: MessageListener {
    private val client = Client()
    private var listener: OnServerPacketListener? = null

    fun connect(url: String, port: Int): Boolean {
        Log.d(TAG, "Trying to connect $url:$port")
        val connectResult = client.connect(url, port)
        if(connectResult) {
            client.registerListener(this)
        }
        Log.d(TAG, "connectResult=$connectResult")
        return connectResult
    }

    fun sendCommand(packet: ServerPacket) {
        client.send(packet.toJson())
    }

    fun disConnect() {
        client.stopListen()
        client.unRegisterListener()
        client.disConnect()
    }

    fun registerServerPacketListener(listener: OnServerPacketListener) {
        this.listener = listener
        client.startListen()
    }

    fun unRegisterServerPacketListener() {
        this.listener = null
    }

    /**
     * Called by Client
     */
    override fun onServerMessage(packet: ServerPacket) {
        listener?.onServerPacket(packet)
    }
}