# WebSocket Test

Spring Boot + Kotlin 기반의 WebSocket 멀티 모듈 프로젝트입니다.
Server와 Client가 각각 독립적인 모듈로 구성되어 있으며, 주기적으로 메시지를 주고받습니다.

## 기술 스택

- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 4.0.3
- **Java**: 21
- **Build**: Gradle (Kotlin DSL)
- **Protocol**: WebSocket (Spring WebSocket)
- **Serialization**: Jackson 3.x (tools.jackson)

---

## 프로젝트 구조

```
WebSocket_test/
├── common/                             # 공통 모듈 (메시지 타입 정의)
│   └── src/main/kotlin/com/test/common/
│       └── WebSocketMessage.kt         # MessageType enum, WebSocketMessage data class
│
├── server/                             # WebSocket 서버 (port: 8080)
│   └── src/main/kotlin/com/test/server/
│       ├── ServerApplication.kt
│       ├── config/
│       │   └── WebSocketConfig.kt      # WebSocket 엔드포인트 설정 (/ws)
│       └── handler/
│           └── ServerWebSocketHandler.kt
│
└── client/                             # WebSocket 클라이언트 (port: 8081)
    └── src/main/kotlin/com/test/client/
        ├── ClientApplication.kt
        ├── config/
        │   ├── WebSocketClientConfig.kt    # StandardWebSocketClient Bean
        │   └── WebSocketConnectionRunner.kt # 앱 시작 시 서버 자동 연결
        └── handler/
            └── ClientWebSocketHandler.kt
```

---

## 메시지 타입

모든 메시지는 JSON 형식으로 전송되며, `common` 모듈에 정의된 타입을 사용합니다.

```json
{
  "type": "SERVER_PING",
  "content": "Server ping.",
  "timestamp": "2024-01-01T00:00:00"
}
```

| MessageType           | 방향              | 설명                              |
|-----------------------|-------------------|-----------------------------------|
| `WELCOME`             | Server → Client   | 클라이언트 최초 접속 시 서버 환영 메시지 |
| `WELCOME_RESPONSE`    | Client → Server   | WELCOME에 대한 클라이언트 응답      |
| `SERVER_PING`         | Server → Client   | 서버가 10초 간격으로 전송하는 ping  |
| `SERVER_PING_RESPONSE`| Client → Server   | SERVER_PING에 대한 클라이언트 응답  |
| `CLIENT_PING`         | Client → Server   | 클라이언트가 20초 간격으로 전송하는 ping |
| `CLIENT_PING_RESPONSE`| Server → Client   | CLIENT_PING에 대한 서버 응답       |

---

## 메시지 흐름

```
[최초 접속]
Server ──WELCOME──────────────────────► Client
Server ◄──WELCOME_RESPONSE──────────── Client

[이후 10초마다]
Server ──SERVER_PING──────────────────► Client
Server ◄──SERVER_PING_RESPONSE───────── Client

[이후 20초마다]
Server ◄──CLIENT_PING────────────────── Client
Server ──CLIENT_PING_RESPONSE────────► Client
```

**처리 규칙:**
- Server: `CLIENT_PING` 수신 시에만 `CLIENT_PING_RESPONSE` 응답, 나머지는 로그 출력
- Client: `WELCOME`, `SERVER_PING` 수신 시에만 응답, 나머지는 로그 출력

---

## 실행 방법

### 사전 조건

- Java 21 이상

### 1. 서버 실행

```bash
./gradlew :server:bootRun
```

서버가 `http://localhost:8080` 에서 시작됩니다.
WebSocket 엔드포인트: `ws://localhost:8080/ws`

### 2. 클라이언트 실행 (별도 터미널)

```bash
./gradlew :client:bootRun
```

클라이언트가 `http://localhost:8081` 에서 시작되고, 자동으로 서버에 연결합니다.

### 3. 빌드만 수행

```bash
# 전체 빌드
./gradlew build

# 모듈별 빌드
./gradlew :common:build
./gradlew :server:build
./gradlew :client:build
```

### 4. 실행 가능한 JAR 생성

```bash
./gradlew :server:bootJar
./gradlew :client:bootJar
```

```bash
java -jar server/build/libs/server-0.0.1-SNAPSHOT.jar
java -jar client/build/libs/client-0.0.1-SNAPSHOT.jar
```

---

## 설정

### Server (`server/src/main/resources/application.yaml`)

```yaml
spring:
  application:
    name: websocket-server

server:
  port: 8080
```

### Client (`client/src/main/resources/application.yaml`)

```yaml
spring:
  application:
    name: websocket-client

server:
  port: 8081

websocket:
  server:
    url: ws://localhost:8080/ws
```

클라이언트의 서버 접속 URL은 `websocket.server.url` 설정으로 변경할 수 있습니다.