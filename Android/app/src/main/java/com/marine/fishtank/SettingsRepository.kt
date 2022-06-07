package com.marine.fishtank

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DEFAULT_SERVER_URL = "fish.marineseo.xyz"
private const val DEFAULT_SERVER_PORT = 53265

private const val DEFAULT_RTSP_URL = "rtsp://msseo:1116@$DEFAULT_SERVER_URL:8888/fishtank"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val DEFAULT_CONNECTION_SETTING = ConnectionSetting(
    serverUrl = DEFAULT_SERVER_URL,
    serverPort = DEFAULT_SERVER_PORT,
    rtspUrl = DEFAULT_RTSP_URL
)

data class ConnectionSetting(
    var serverUrl: String,
    var serverPort: Int,
    var rtspUrl: String
) {
    override fun equals(other: Any?): Boolean {
        if(other !is ConnectionSetting) return false
        return other.serverUrl == this.serverUrl
                && other.serverPort == this.serverPort
                && other.rtspUrl == this.rtspUrl
    }

    override fun hashCode(): Int {
        return serverUrl.hashCode() + serverPort + rtspUrl.hashCode()
    }
}


class SettingsRepository private constructor(
    private val context: Context
) {
    private val keyServerUrl = stringPreferencesKey("server_url")
    private val keyServerPort = intPreferencesKey("server_port")
    private val keyRtspUrl = stringPreferencesKey("rtsp_url")

    val settingFlow: Flow<ConnectionSetting> = context.dataStore.data
        .map { pref ->
            ConnectionSetting(
                serverUrl = pref[keyServerUrl] ?: DEFAULT_SERVER_URL,
                serverPort = pref[keyServerPort] ?: DEFAULT_SERVER_PORT,
                rtspUrl = pref[keyRtspUrl] ?: DEFAULT_RTSP_URL
            )
        }

    val serverUrlFlow: Flow<String> = context.dataStore.data
        .map { pref -> pref[keyServerUrl] ?: DEFAULT_SERVER_URL }
    val serverPortFlow: Flow<Int> = context.dataStore.data
        .map { pref -> pref[keyServerPort] ?: DEFAULT_SERVER_PORT }
    val rtspUrlFlow: Flow<String> = context.dataStore.data
        .map { pref -> pref[keyRtspUrl] ?: DEFAULT_RTSP_URL }

    suspend fun saveServerUrl(url :String) {
        context.dataStore.edit {
            it[keyServerUrl] = url
        }
    }

    suspend fun saveServerPort(port: Int) {
        context.dataStore.edit {
            it[keyServerPort] = port
        }
    }

    suspend fun saveRtspUrl(url: String) {
        context.dataStore.edit {
            it[keyRtspUrl] = url
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.let {
                    return it
                }

                val instance = SettingsRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }



}