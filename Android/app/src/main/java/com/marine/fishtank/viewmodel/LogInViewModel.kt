package com.marine.fishtank.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.marine.fishtank.SettingsRepository
import com.marine.fishtank.api.OnServerPacketListener
import com.marine.fishtank.api.TankApi
import com.marine.fishtank.model.SERVER_OP_SIGN_IN
import com.marine.fishtank.model.ServerPacket
import com.marine.fishtank.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class SignInResult(
    val result: Boolean,
    val reason: String
)

class LogInViewModel(application: Application) : AndroidViewModel(application) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val settingsRepository = SettingsRepository.getInstance(context = application)

    val signInResult = MutableLiveData<SignInResult>()
    val connectResult = MutableLiveData<Boolean>()

    private val tankApi: TankApi by lazy {
        TankApi.apply {
            registerServerPacketListener(packetListener)
        }
    }

    fun connectToServer() {
        scope.launch {
            settingsRepository.settingFlow.collect { connectionSetting ->
                connectResult.postValue(
                    TankApi.connect(connectionSetting.serverUrl, connectionSetting.serverPort)
                )
            }
        }
    }

    private val packetListener = object : OnServerPacketListener {
        override fun onServerPacket(packet: ServerPacket) {
            when (packet.opCode) {
                SERVER_OP_SIGN_IN -> {
                    if (packet.obj is Boolean) {
                        // sign-in success
                        signInResult.postValue(
                            SignInResult(
                                packet.obj, ""
                            )
                        )
                    }
                }
            }
        }
    }

    fun signIn(userId: String, password: String) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_SIGN_IN, obj = User(id = userId, password = password)))
    }
}