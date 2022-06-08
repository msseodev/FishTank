package com.marine.fishtank.api

import com.marine.fishtank.model.ServerPacket
import com.marine.fishtank.model.toJson

interface OnServerPacketListener {
    fun onServerPacket(packet: ServerPacket)
}

object TankApi: MessageListener {
    private val client = Client()
    private var listener: OnServerPacketListener? = null

    fun connect(url: String, port: Int): Boolean {
        val connectResult = client.connect(url, port)
        if(connectResult) {
            client.registerListener(this)
        }
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