package com.marine.fishtank.server.model

data class User(
    val id: String,
    val name: String = "",
    val password: String
)
