package com.test.common

import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime

enum class MessageType {
    WELCOME,
    WELCOME_RESPONSE,
    SERVER_PING,
    SERVER_PING_RESPONSE,
    CLIENT_PING,
    CLIENT_PING_RESPONSE,
}

data class WebSocketMessage(
    val type: MessageType,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        private val mapper: ObjectMapper = jacksonObjectMapper()

        fun fromJson(json: String): WebSocketMessage = mapper.readValue(json, WebSocketMessage::class.java)
    }

    fun toJson(): String = mapper.writeValueAsString(this)
}