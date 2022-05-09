package com.marine.fishtank.api

import com.marine.fishtank.model.TankData
import com.marine.fishtank.model.ServerPacket
import com.marine.fishtank.model.toJson

class TankApiImpl(): TankApi, MessageListener {
    private val client = Client()
    private var listener: TankApi.OnServerPacketListener? = null

    override fun connect(url: String, port: Int): Boolean {
        // TODO - impl TCP socket connection
        client.connect(url, port)

        client.registerListener(this)
        return true
    }

    override fun sendCommand(packet: ServerPacket): List<TankData> {
        // TODO - impl send packet and get response
        client.send(packet.toJson())

        return emptyList()
    }

    override fun startListen() {

    }

    override fun stopListen() {
        // TODO - impl stop listen form socket
    }

    override fun disConnect() {
        // TODO - impl disconnect.
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