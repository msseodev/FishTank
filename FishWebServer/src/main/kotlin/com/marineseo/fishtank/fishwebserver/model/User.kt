package com.marineseo.fishtank.fishwebserver.model

data class User(
    val id: String,
    val name: String = "",
    val password: String,
    var token: String
)
