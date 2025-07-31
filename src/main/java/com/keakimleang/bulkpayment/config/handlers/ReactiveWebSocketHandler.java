package com.keakimleang.bulkpayment.config.handlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @NonNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        String sessionId;

        if (query != null && query.startsWith("user_session=")) {
            sessionId = query.split("=")[1];
        } else {
            sessionId = null;
        }

        if (sessionId != null) {
            sessions.put(sessionId, session); // store by sessionId
            System.out.println("WebSocket connected for sessionId: " + sessionId);
        } else {
            System.out.println("Missing userId in WebSocket URI");
        }

        return session.receive().then()
                .doFinally(signal -> {
                    if (sessionId != null) {
                        sessions.remove(sessionId);
                        System.out.println("WebSocket disconnected for userId: " + sessionId);
                    }
                });
    }

    // Utility to send logoutMessage to a session
    public static void sendLogoutMessage(String logoutMessage, String oldSessionId) {
        WebSocketSession session = sessions.get(oldSessionId);
        if (session != null && session.isOpen()) {
            session.send(Mono.just(session.textMessage(logoutMessage))).subscribe();
        }
    }

}
