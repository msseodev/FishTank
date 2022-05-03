package com.marine.fishtank.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.marine.fishtank.api.TankApi
import com.marine.fishtank.api.TankApiMock
import com.marine.fishtank.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SERVER_IP = ""
private const val SERVER_PORT = 13422

class ControlViewModel : ViewModel() {
    val liveData = MutableLiveData<DataSource<TankData>>()

    // TODO - Replace api to actual one later.
    private val tankApi: TankApi = TankApiMock()

    private val packetListener = object: TankApi.OnServerPacketListener {
        override fun onServerPacket(rawData: String) {
            val gson = Gson()
            val tankData = gson.fromJson(rawData, TankData::class.java)

            liveData.postValue(DataSource(Status.SUCCESS, tankData))
        }
    }

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            val connectResult = tankApi.connect(SERVER_IP, SERVER_PORT)
            withContext(Dispatchers.Main) {
                liveData.value = DataSource(if (connectResult) Status.SUCCESS else Status.ERROR, null)
            }

            tankApi.registerServerPacketListener(packetListener)
        }
    }

    fun startFetchHistory() {
        // TODO - Get FishTank history and emit data to liveData
        viewModelScope.launch(Dispatchers.IO){
            val dataList = tankApi.sendCommand(FishPacket(OP_GET_HISTORY, 1))
            withContext(Dispatchers.Main) {
                dataList.forEach {
                    liveData.value = DataSource(Status.SUCCESS, it)
                }
            }
        }

    }

    fun startListenTank() {
        // TODO - Listen tank status and emit data to liveData
        tankApi.sendCommand(FishPacket(OP_LISTEN_STATUS, 1))
    }

    fun changeWater(ratio: Double) {
        // TODO - impl change water and result to liveData (status)
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

}