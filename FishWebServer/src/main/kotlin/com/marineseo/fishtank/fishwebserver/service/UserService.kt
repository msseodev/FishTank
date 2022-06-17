package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.mapper.DatabaseMapper
import com.marineseo.fishtank.fishwebserver.util.MarineUtils
import org.springframework.stereotype.Service

const val TOKEN_LENGTH = 64
@Service
class UserService(
    private val mapper: DatabaseMapper
) {
    fun signIn(id: String, password: String): Boolean {
        val user = mapper.getUser(id)
        return user.password == password
    }

    fun generateUserToken(): String {
        return MarineUtils.makeRandomString(TOKEN_LENGTH)
    }
}