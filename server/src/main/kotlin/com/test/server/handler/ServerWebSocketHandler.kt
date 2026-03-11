package com.test.server.handler

import com.test.common.MessageType
import com.test.common.WebSocketMessage
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class ServerWebSocketHandler : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
        log.info("Client connected: ${session.id}")

        val message = WebSocketMessage(
            type = MessageType.WELCOME,
            content = "Welcome! You are connected to the server.",
        )
        session.sendMessage(TextMessage(message.toJson()))
        log.info("Sent [${message.type}] to client [${session.id}]")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val received = WebSocketMessage.fromJson(message.payload)
        log.info("Received [${received.type}] from client [${session.id}]: ${received.content}")

        when (received.type) {
            MessageType.CLIENT_PING -> {
                val response = WebSocketMessage(
                    type = MessageType.CLIENT_PING_RESPONSE,
                    content = "Received your ping.",
                )
                session.sendMessage(TextMessage(response.toJson()))
                log.info("Sent [${response.type}] to client [${session.id}]")
            }
            else -> Unit
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)
        log.info("Client disconnected: ${session.id}, status: $status")
    }

    @Scheduled(fixedDelay = 10_000)
    fun sendServerPing() {
        if (sessions.isEmpty()) return
        val message = WebSocketMessage(
            type = MessageType.SERVER_PING,
            content = "Server ping.",
        )
        sessions.values.forEach { session ->
            if (session.isOpen) {
                session.sendMessage(TextMessage(message.toJson()))
                log.info("Sent [${message.type}] to client [${session.id}]")
            }
        }
    }
}