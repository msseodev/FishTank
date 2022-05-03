package com.marine.fishtank.api

import android.util.Log
import com.google.gson.Gson
import com.marine.fishtank.model.FishPacket
import com.marine.fishtank.model.OP_GET_HISTORY
import com.marine.fishtank.model.OP_LISTEN_STATUS
import com.marine.fishtank.model.TankData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

const val TAG = "TankApiMock"

class TankApiMock : TankApi {
    private val listeners = mutableListOf<TankApi.OnServerPacketListener>()
    private var isListen = false

    override fun connect(url: String, port: Int): Boolean {
        Log.d(TAG, "connect")
        return true
    }

    override fun sendCommand(packet: FishPacket): List<TankData> {
        Log.d(TAG, "sendCommand $packet")

        when (packet.opCode) {
            OP_LISTEN_STATUS -> {
                startPublishMockData()
            }
            OP_GET_HISTORY -> {
                return makeMockHistoryData()
            }
        }

        return emptyList()
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

    private fun makeMockHistoryData(): List<TankData> {
        return arrayListOf(
            TankData(25.0, false, true, true, System.currentTimeMillis() - (1000L * 5 * 5)),
            TankData(24.3, false, true, true, System.currentTimeMillis() - (1000L * 5 * 4)),
            TankData(26.8, false, true, true, System.currentTimeMillis() - (1000L * 5 * 3)),
            TankData(27.2, false, true, true, System.currentTimeMillis() - (1000L * 5 * 2)),
            TankData(24.5, false, true, true, System.currentTimeMillis() - (1000L * 5 * 1))
        )
    }

    private fun startPublishMockData() {
        CoroutineScope(Dispatchers.IO).launch {
            isListen = true
            val gson = Gson()

            // Make mock data and publish every 5 sec.
            while (isListen) {
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