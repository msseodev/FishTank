package com.marine.fishtank.api

import com.marine.fishtank.model.ServerPacket
import com.marine.fishtank.model.toJson

class TankApiImpl: TankApi, MessageListener {
    private val client = Client()
    private var listener: TankApi.OnServerPacketListener? = null

    override fun connect(url: String, port: Int): Boolean {
        val connectResult = client.connect(url, port)
        if(connectResult) {
            client.registerListener(this)
        }
        return connectResult
    }

    override fun sendCommand(packet: ServerPacket) {
        client.send(packet.toJson())
    }

    override fun disConnect() {
        client.stopListen()
        client.unRegisterListener()
        client.disConnect()
    }

    override fun registerServerPacketListener(listener: TankApi.OnServerPacketListener) {
        this.listener = listener
        client.startListen()
    }

    override fun unRegisterServerPacketListener() {
        this.listener = null
    }

    /**
     * Called by Client
     */
    override fun onServerMessage(packet: ServerPacket) {
        listener?.onServerPacket(packet)
    }
}