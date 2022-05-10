package com.marine.fishtank.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marine.fishtank.api.TankApi
import com.marine.fishtank.api.TankApiImpl
import com.marine.fishtank.model.*
import com.marine.fishtank.model.ServerPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val SERVER_URL = "marineseo.iptime.org"
private const val SERVER_PORT = 53265
private const val TEMPERATURE_INTERVAL = 1000L * 5

class ControlViewModel : ViewModel() {
    val liveData = MutableLiveData<DataSource<TankData>>()
    val temperatureData = MutableLiveData<Double>()

    // TODO - Replace api to actual one later.
    private val tankApi: TankApi = TankApiImpl()

    private val packetListener = object: TankApi.OnServerPacketListener {
        override fun onServerPacket(packet: ServerPacket) {
            when(packet.opCode) {
                SERVER_OP_GET_TEMPERATURE -> {
                    temperatureData.postValue(packet.doubleData)
                }
            }

        }
    }

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            val connectResult = tankApi.connect(SERVER_URL, SERVER_PORT)
            withContext(Dispatchers.Main) {
                liveData.value = DataSource(if (connectResult) Status.SUCCESS else Status.ERROR, null)
            }

            tankApi.registerServerPacketListener(packetListener)
        }
    }

    fun startFetchHistory() {
        // TODO - Get FishTank history and emit data to liveData
        viewModelScope.launch(Dispatchers.IO){
            val dataList = tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_GET_HISTORY))
            withContext(Dispatchers.Main) {
                dataList.forEach {
                    liveData.value = DataSource(Status.SUCCESS, it)
                }
            }
        }
    }

    fun startListenTemperature() {
        // TODO - Listen temperature and emit data to liveData
        viewModelScope.launch(Dispatchers.IO) {
            while(true) {
                tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_GET_TEMPERATURE))
                delay(TEMPERATURE_INTERVAL)
            }
        }
    }

    fun changeWater(ratio: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            // Open out-water valve.
            tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_OUT_WATER, data = 1))
            // Wait water out time..
            delay(1000L * 5)

            tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_OUT_WATER, data = 0))
        }
    }

    fun setLight(enable: Boolean) {
        // TODO - Send light on command and result to liveData (status)
    }

    fun setHeater(enable: Boolean) {
        // TODO - Send heater on command and get result to liveData
    }

    fun setMaxWater(volume: Int) {
        // TODO - Send max fish-tank water volume.
    }

    private var preValue = 0
    fun testFunction() {
        preValue = if(preValue == 0) 1 else 0
        viewModelScope.launch(Dispatchers.IO) {
            tankApi.sendCommand(
                ServerPacket(AppId.MY_ID, SERVER_OP_MEGA_LED, preValue)
            )
        }
    }

}