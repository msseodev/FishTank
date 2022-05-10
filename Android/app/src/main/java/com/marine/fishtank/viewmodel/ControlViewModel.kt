package com.marine.fishtank.viewmodel

import android.util.Log
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
import kotlin.math.round


private const val TAG = "ControlViewModel"
private const val SERVER_URL = "marineseo.iptime.org"
private const val SERVER_PORT = 53265
private const val TEMPERATURE_INTERVAL = 1000L * 5

// 어항 물 용량
private const val TANK_WATER_VOLUME = 100

class ControlViewModel : ViewModel() {
    val liveTankData = MutableLiveData<DataSource<TankData>>()
    val temperatureData = MutableLiveData<Double>()
    val initData = MutableLiveData<DataSource<String>>()

    private val tankApi: TankApi = TankApiImpl()

    private val packetListener = object: TankApi.OnServerPacketListener {
        override fun onServerPacket(packet: ServerPacket) {
            // packet sent by server.
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
                initData.value = DataSource(if (connectResult) Status.SUCCESS else Status.ERROR, "Init Error!")
            }

            tankApi.registerServerPacketListener(packetListener)
        }
    }

    fun startFetchHistory() {
        viewModelScope.launch(Dispatchers.IO){
            val dataList = tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_GET_HISTORY))
            withContext(Dispatchers.Main) {
                dataList.forEach {
                    liveTankData.value = DataSource(Status.SUCCESS, it)
                }
            }
        }
    }

    fun startListenTemperature() {
        viewModelScope.launch(Dispatchers.IO) {
            while(true) {
                tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_GET_TEMPERATURE))
                delay(TEMPERATURE_INTERVAL)
            }
        }
    }

    fun changeWater(ratio: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            // Before draining we have to close in-water solenoid valve.
            // in-water solenoid valve is NO(normally open). data=1 make close.
            tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_IN_WATER, data = 1))

            // Open out-water valve.
            tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_OUT_WATER, data = 1))
            // Wait some delay.
            delay(1000L * 2)

            val pumpOperationTime = calculateWaterPumpTime(ratio)
            Log.d(TAG, "Pump operation time=$pumpOperationTime seconds.")

            // Start water-pump!
            tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_WATER_PUMP, data = 1))
            delay(pumpOperationTime * 1000L)

            // Stop water-pump!
            tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_WATER_PUMP, data = 0))
            delay(1000L * 2)

            // Close out-water valve. Water draining is done!
            tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_OUT_WATER, data = 0))

            // Now we have to supply water. We assume that in-water has ball-top valve.
            tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_IN_WATER, data = 0))
        }
    }

    fun enableLight(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_LIGHT, data = if(enable) 1 else 0))
    }

    fun enableHeater(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_HEATER, data = if(enable) 1 else 0))
    }

    fun enablePurifier(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_PURIFIER_1, data = if(enable) 1 else 0))
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_PURIFIER_2, data = if(enable) 1 else 0))
    }

    private fun calculateWaterPumpTime(ratio: Double): Int {
        // TODO - WaterPump 의 분당 출수량을 측정하여 ratio 에 따라 동작시간을 계산하여 리턴
        // https://www.coupang.com/vp/products/1749478023?vendorItemId=70967688005&sourceType=MyCoupang_my_orders_list_product_title&isAddedCart=
        // 스펙상 토출량: 분당 2L
        val targetOutVolume = TANK_WATER_VOLUME * ratio
        return round((targetOutVolume / 2.0) * 60).toInt()
    }

    private var preValue = 0
    fun toggleBoardLed() {
        preValue = if(preValue == 0) 1 else 0
        viewModelScope.launch(Dispatchers.IO) {
            tankApi.sendCommand(
                ServerPacket(AppId.MY_ID, SERVER_OP_MEGA_LED, preValue)
            )
        }
    }

}