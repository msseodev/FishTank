package com.marine.fishtank.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.ServerSocket

private const val PORT = 53265

class SocketAccepter {
    private val serverSocket = ServerSocket(PORT)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val clientList = mutableListOf<Client>()

    fun startListen() {
        while (true) {
            println("Waiting Client....")
            val socket = serverSocket.accept()

            println("Accept=${socket.inetAddress.hostAddress}")

            val client = Client(socket)
            val isVerified = client.handShake()
            if (!isVerified) {
                // Deny this client.
                println("Client(${socket.inetAddress.hostAddress} is not verified. Disconnect!")
                client.disconnect()
                continue
            }

            clientList.add(client)
            client.startListen()
        }
    }


}