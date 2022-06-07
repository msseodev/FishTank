package com.marine.fishtank.server

import com.marine.fishtank.server.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.ServerSocket

private const val PORT = 53265
private const val TAG = "SocketAcceptor"

class SocketAcceptor: OnClientDisconnect {
    private val serverSocket = ServerSocket(PORT)
    private val clientList = mutableListOf<Client>()

    fun startListen() {
        while (true) {
            Log.d(TAG, "Waiting Client....")
            val socket = serverSocket.accept()

            Log.d(TAG,"Accept=${socket.inetAddress.hostAddress}")

            val client = Client(socket, this)
            val isVerified = client.handShake()
            if (!isVerified) {
                // Deny this client.
                Log.d(TAG,"Client(${socket.inetAddress.hostAddress} is not verified. Disconnect!")
                client.disconnect()
                continue
            }

            clientList.add(client)
            client.startListen()
        }
    }

    override fun onClientDisconnected(client: Client) {
        clientList.remove(client)
    }
}