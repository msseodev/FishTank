package com.marine.fishtank.api

import android.util.Log
import com.google.gson.Gson
import com.marine.fishtank.model.FishPacket
import com.marine.fishtank.model.OP_LISTEN_STATUS
import com.marine.fishtank.model.TankData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

const val TAG = "TankApiMock"
class TankApiMock: TankApi {
    private val listeners = mutableListOf<TankApi.OnServerPacketListener>()
    private var isListen = false

    override fun connect(url: String, port: Int): Boolean {
        Log.d(TAG, "connect")
        return true
    }

    override fun sendCommand(packet: FishPacket): TankData {
        Log.d(TAG, "sendCommand $packet")

        when(packet.opCode) {
            OP_LISTEN_STATUS -> {
                startPublishMockData()
            }
        }

        return TankData(25.0, false, false, false, System.currentTimeMillis())
    }

    override fun registerServerPacketListener(listener: TankApi.OnServerPacketListener) {
        listeners.add(listener)
    }

    override fun unRegisterServerPacketListener(listener: TankApi.OnServerPacketListener) {
        listeners.remove(listener)
    }

    override fun startListen() {
        Log.d(TAG, "startListen")
    }

    override fun stopListen() {
        Log.d(TAG, "stopListen")
    }

    override fun disConnect() {
        Log.d(TAG, "disConnect")
    }

    private fun startPublishMockData() {
        CoroutineScope(Dispatchers.IO).launch {
            isListen = true
            val gson = Gson()

            // Make mock data and publish every 5 sec.
            while(isListen) {
                val mockData = TankData(
                    Random.nextDouble(23.0, 28.0),
                    false,
                    true,
                    true,
                    System.currentTimeMillis()
                )
                val jsonString = gson.toJson(mockData)
                listeners.forEach {
                    it.onServerPacket(jsonString)
                }

                delay(1000L * 5)
            }
        }
    }
}