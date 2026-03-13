package com.test.client.config

import com.test.client.handler.ClientWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.WebSocketClient

private val CLIENT_ID_REGEX = Regex("^[a-zA-Z0-9_-]+$")

@Component
class WebSocketConnectionRunner(
    private val webSocketClient: WebSocketClient,
    private val clientWebSocketHandler: ClientWebSocketHandler,
    @Value("\${websocket.server.url}") private val serverUrl: String,
    @Value("\${client.id}") private val clientId: String,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        require(clientId.matches(CLIENT_ID_REGEX)) {
            "Invalid client.id '$clientId': only letters, digits, underscore(_), and hyphen(-) are allowed."
        }
        log.info("Connecting as '$clientId' to WebSocket server: $serverUrl")
        webSocketClient.execute(clientWebSocketHandler, serverUrl).get()
        log.info("WebSocket connection established")

        Thread({ readConsoleInput() }, "stdin-reader").also { it.isDaemon = true }.start()
    }

    private fun readConsoleInput() {
        val reader = System.`in`.bufferedReader()
        while (true) {
            val line = reader.readLine() ?: break
            val text = line.trim()
            if (text.isNotEmpty()) {
                clientWebSocketHandler.sendChat(text)
            }
        }
    }
}