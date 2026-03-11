package com.test.client.config

import com.test.client.handler.ClientWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketClient

@Component
class WebSocketConnectionRunner(
    private val webSocketClient: WebSocketClient,
    private val clientWebSocketHandler: ClientWebSocketHandler,
    @Value("\${websocket.server.url}") private val serverUrl: String,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        log.info("Connecting to WebSocket server: $serverUrl")
        webSocketClient.execute(clientWebSocketHandler, serverUrl).get()
        log.info("WebSocket connection established")
    }
}