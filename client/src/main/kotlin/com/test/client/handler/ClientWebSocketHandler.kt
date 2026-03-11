package com.test.client.handler

import com.test.common.MessageType.*
import com.test.common.WebSocketMessage
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ClientWebSocketHandler : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile
    private var session: WebSocketSession? = null

    override fun afterConnectionEstablished(session: WebSocketSession) {
        this.session = session
        log.info("Connected to server: ${session.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val received = WebSocketMessage.fromJson(message.payload)
        log.info("Received [${received.type}] from server: ${received.content}")

        when (received.type) {
            WELCOME -> processWelcome(session)
            SERVER_PING -> processServerPing(session)
            else -> Unit
        }
    }

    private fun processWelcome(session: WebSocketSession) {
        val response = WebSocketMessage(
            type = WELCOME_RESPONSE,
            content = "Thank you!",
        )
        session.sendMessage(TextMessage(response.toJson()))
        log.info("Sent [${response.type}] to server")
    }

    private fun processServerPing(session: WebSocketSession) {
        val response = WebSocketMessage(
            type = SERVER_PING_RESPONSE,
            content = "Pong.",
        )
        session.sendMessage(TextMessage(response.toJson()))
        log.info("Sent [${response.type}] to server")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        this.session = null
        log.info("Disconnected from server, status: $status")
    }

    @Scheduled(fixedDelay = 20_000)
    fun sendClientPing() {
        val currentSession = session ?: return
        if (!currentSession.isOpen) return
        val message = WebSocketMessage(
            type = CLIENT_PING,
            content = "Client ping.",
        )
        currentSession.sendMessage(TextMessage(message.toJson()))
        log.info("Sent [${message.type}] to server")
    }
}