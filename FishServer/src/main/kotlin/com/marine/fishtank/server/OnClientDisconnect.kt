package com.marine.fishtank.server

interface OnClientDisconnect {
    fun onClientDisconnected(client: Client)
}